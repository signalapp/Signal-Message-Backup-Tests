@file:OptIn(ExperimentalStdlibApi::class)

import com.thedeanda.lorem.LoremIpsum
import okio.ByteString.Companion.toByteString
import org.signal.libsignal.messagebackup.AccountEntropyPool
import org.signal.libsignal.messagebackup.BackupKey
import org.signal.libsignal.messagebackup.MessageBackupKey
import org.signal.libsignal.protocol.ServiceId
import org.thoughtcrime.securesms.backup.v2.proto.*
import java.awt.Color
import java.awt.GradientPaint
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilterOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

/**
 * Generates encrypted local backups compatible with Signal Android's local backup format.
 * These can be placed at /sdcard/signal-quickstart/SignalBackups/ for use with the
 * quickstart restore process.
 */
object LocalBackupGenerator {

  private val secureRandom = SecureRandom()
  private val lipsum = LoremIpsum.getInstance()

  fun generate(aep: String, aci: String, outputDir: File, messageCount: Int) {
    println("Generating local backup...")
    println("  AEP: ${aep.take(8)}...")
    println("  ACI: $aci")
    println("  Output: ${outputDir.absolutePath}")
    println("  Messages: $messageCount")

    // Derive encryption keys from AEP
    val backupKey: BackupKey = AccountEntropyPool.deriveBackupKey(aep)
    val aciServiceId = ServiceId.Aci(UUID.fromString(aci))
    val backupId: ByteArray = backupKey.deriveBackupId(aciServiceId)
    val metadataKey: ByteArray = backupKey.deriveLocalBackupMetadataKey()
    val messageBackupKey = MessageBackupKey(backupKey, backupId)
    val aesKey: ByteArray = messageBackupKey.aesKey
    val hmacKey: ByteArray = messageBackupKey.hmacKey

    // Create directory structure
    val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
    val signalBackupsDir = File(outputDir, "SignalBackups")
    val snapshotDir = File(signalBackupsDir, "signal-backup-$timestamp")
    snapshotDir.mkdirs()
    val filesDir = File(signalBackupsDir, "files")
    filesDir.mkdirs()

    // Generate backup content frames (with attachment files written to filesDir)
    val frames = generateBackupContent(messageCount, filesDir)
    println("  Generated ${frames.size} frames")

    // Write encrypted main file
    val mainFile = File(snapshotDir, "main")
    writeEncryptedMain(mainFile, frames, aesKey, hmacKey)
    println("  Wrote main file: ${mainFile.length()} bytes")

    // Write metadata file
    val metadataFile = File(snapshotDir, "metadata")
    writeMetadata(metadataFile, backupId, metadataKey)
    println("  Wrote metadata file: ${metadataFile.length()} bytes")

    println("Done! Backup at: ${snapshotDir.absolutePath}")
    println()
    println("To use with quickstart restore, copy to device:")
    println("  adb push ${outputDir.absolutePath}/SignalBackups /sdcard/signal-quickstart/")
  }

  /**
   * Represents a generated attachment file written to the files directory.
   */
  private data class AttachmentInfo(
    val plaintextHash: ByteArray,
    val localKey: ByteArray,
    val plaintextSize: Int,
    val width: Int,
    val height: Int,
    val contentType: String
  )

  private fun generateBackupContent(messageCount: Int, filesDir: File): List<com.squareup.wire.Message<*, *>> {
    val random = Random(42) // deterministic for reproducibility
    val frames = mutableListOf<com.squareup.wire.Message<*, *>>()

    val now = System.currentTimeMillis()

    // BackupInfo header (always first)
    frames += BackupInfo(
      version = 1,
      backupTimeMs = now,
      mediaRootBackupKey = randomBytes(32).toByteString(),
      currentAppVersion = "Signal-Android 7.0.0",
      firstAppVersion = "Signal-Android 7.0.0"
    )

    // Account data
    frames += Frame(
      account = AccountData(
        profileKey = randomBytes(32).toByteString(),
        givenName = "Test",
        familyName = "User",
        avatarUrlPath = "",
        accountSettings = AccountData.AccountSettings(
          readReceipts = true,
          sealedSenderIndicators = true,
          typingIndicators = true,
          linkPreviews = true,
          notDiscoverableByPhoneNumber = false,
          preferContactAvatars = false,
          universalExpireTimerSeconds = 0,
          displayBadgesOnProfile = false,
          keepMutedChatsArchived = false,
          hasSetMyStoriesPrivacy = true,
          hasViewedOnboardingStory = true,
          storiesDisabled = false,
          storyViewReceiptsEnabled = false,
          hasSeenGroupStoryEducationSheet = true,
          hasCompletedUsernameOnboarding = true,
          phoneNumberSharingMode = AccountData.PhoneNumberSharingMode.NOBODY
        )
      )
    )

    // Self recipient (ID 1)
    frames += Frame(
      recipient = Recipient(
        id = 1,
        self = Self(avatarColor = AvatarColor.A100)
      )
    )

    // Release notes recipient (ID 2)
    frames += Frame(
      recipient = Recipient(
        id = 2,
        releaseNotes = ReleaseNotes()
      )
    )

    // My Story (ID 3)
    frames += Frame(
      recipient = Recipient(
        id = 3,
        distributionList = DistributionListItem(
          distributionId = UUID.fromString("00000000-0000-0000-0000-000000000000").toByteArray().toByteString(),
          distributionList = DistributionList(
            name = "",
            privacyMode = DistributionList.PrivacyMode.ALL
          )
        )
      )
    )

    // Note to Self chat (recipientId = 1, which is Self)
    val noteToSelfChatId = 1L
    frames += Frame(
      chat = Chat(
        id = noteToSelfChatId,
        recipientId = 1,
        expireTimerVersion = 1
      )
    )

    // Pre-generate a pool of image attachments (~20% of messages will have one)
    val attachmentCount = messageCount / 5
    println("  Generating $attachmentCount image attachments...")
    val attachments = (0 until attachmentCount).map { i ->
      generateImageAttachment(random, filesDir, i)
    }
    var attachmentIndex = 0

    // Generate messages spread across the last 2 years
    val twoYearsMs = 365L * 2 * 24 * 60 * 60 * 1000
    val startTimestamp = now - twoYearsMs
    val averageGap = twoYearsMs / messageCount

    var timestamp = startTimestamp

    for (i in 0 until messageCount) {
      // Spread messages across 2 years with some randomness
      timestamp += averageGap + random.nextLong(-averageGap / 3, averageGap / 3)
      // Don't go past "now"
      if (timestamp > now) timestamp = now - random.nextLong(0, 60_000)

      val hasAttachment = attachmentIndex < attachments.size && random.nextFloat() < 0.2f
      val text = if (hasAttachment && random.nextFloat() < 0.5f) null else generateMessageText(random)

      val messageAttachments = if (hasAttachment) {
        val att = attachments[attachmentIndex++]
        listOf(
          MessageAttachment(
            pointer = FilePointer(
              contentType = att.contentType,
              width = att.width,
              height = att.height,
              locatorInfo = FilePointer.LocatorInfo(
                key = randomBytes(64).toByteString(),
                plaintextHash = att.plaintextHash.toByteString(),
                size = att.plaintextSize,
                localKey = att.localKey.toByteString()
              )
            ),
            flag = MessageAttachment.Flag.NONE,
            wasDownloaded = true,
            clientUuid = randomBytes(16).toByteString()
          )
        )
      } else {
        emptyList()
      }

      frames += Frame(
        chatItem = ChatItem(
          chatId = noteToSelfChatId,
          authorId = 1,
          dateSent = timestamp,
          outgoing = ChatItem.OutgoingMessageDetails(
            sendStatus = listOf(
              SendStatus(
                recipientId = 1,
                timestamp = timestamp,
                delivered = SendStatus.Delivered(sealedSender = true)
              )
            )
          ),
          standardMessage = StandardMessage(
            text = if (text != null) Text(body = text) else null,
            attachments = messageAttachments
          )
        )
      )
    }

    return frames
  }

  /**
   * Generates a random colored PNG image, encrypts it, and writes it to the files directory.
   * Returns the attachment metadata needed for the backup frame.
   */
  private fun generateImageAttachment(random: Random, filesDir: File, index: Int): AttachmentInfo {
    // Generate a simple image with gradient
    val width = listOf(320, 480, 640, 800, 1024).random(random)
    val height = listOf(240, 320, 480, 600, 768).random(random)
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    val g2d = image.createGraphics()
    val color1 = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    val color2 = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    g2d.paint = GradientPaint(0f, 0f, color1, width.toFloat(), height.toFloat(), color2)
    g2d.fillRect(0, 0, width, height)
    g2d.dispose()

    // Encode as JPEG (smaller than PNG)
    val imageBytes = ByteArrayOutputStream().use { baos ->
      ImageIO.write(image, "JPEG", baos)
      baos.toByteArray()
    }

    // Compute plaintext hash
    val plaintextHash = MessageDigest.getInstance("SHA-256").digest(imageBytes)

    // Generate a 64-byte local backup key (first 32 = AES, second 32 = HMAC)
    val localKey = randomBytes(64)

    // Compute media name: hex(SHA256(plaintextHash + localKey))
    val mediaNameDigest = MessageDigest.getInstance("SHA-256")
    mediaNameDigest.update(plaintextHash)
    mediaNameDigest.update(localKey)
    val mediaName = mediaNameDigest.digest().toHexString()

    // Create subdirectory based on first 2 chars of media name
    val subDir = File(filesDir, mediaName.substring(0, 2))
    subDir.mkdirs()

    // Encrypt: pad plaintext, then encrypt with AttachmentCipher format
    val paddedSize = getPaddedSize(imageBytes.size.toLong())
    val padding = ByteArray((paddedSize - imageBytes.size).toInt())

    val outFile = File(subDir, mediaName)
    outFile.outputStream().use { fileOut ->
      // AttachmentCipherOutputStream format:
      // [16b IV] [AES/CBC/PKCS5Padding ciphertext] [32b HMAC]
      val aesKeyBytes = localKey.copyOfRange(0, 32)
      val macKeyBytes = localKey.copyOfRange(32, 64)

      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKeyBytes, "AES"))
      val iv = cipher.iv

      val mac = Mac.getInstance("HmacSHA256")
      mac.init(SecretKeySpec(macKeyBytes, "HmacSHA256"))
      mac.update(iv)

      // Write IV
      fileOut.write(iv)

      // Encrypt padded plaintext
      val ciphertext1 = cipher.update(imageBytes)
      if (ciphertext1 != null) {
        mac.update(ciphertext1)
        fileOut.write(ciphertext1)
      }

      val ciphertext2 = cipher.update(padding)
      if (ciphertext2 != null) {
        mac.update(ciphertext2)
        fileOut.write(ciphertext2)
      }

      val ciphertextFinal = cipher.doFinal()
      val macBytes = mac.doFinal(ciphertextFinal)

      fileOut.write(ciphertextFinal)
      fileOut.write(macBytes)
    }

    return AttachmentInfo(
      plaintextHash = plaintextHash,
      localKey = localKey,
      plaintextSize = imageBytes.size,
      width = width,
      height = height,
      contentType = "image/jpeg"
    )
  }

  private fun generateMessageText(random: Random): String {
    return when (random.nextInt(12)) {
      0 -> lipsum.getWords(1, 3)
      1 -> lipsum.getWords(1, 2) + "?"
      2 -> lipsum.getWords(1, 2) + "!"
      3, 4 -> lipsum.getWords(3, 8)
      5, 6 -> lipsum.getWords(5, 15)
      7 -> lipsum.getWords(15, 30)
      8 -> lipsum.getParagraphs(1, 1)
      // Note-to-self style: reminders, links, lists
      9 -> "TODO: " + lipsum.getWords(3, 8)
      10 -> "Remember: " + lipsum.getWords(2, 6)
      else -> listOf(
        "Don't forget to pick up groceries",
        "Meeting at 3pm tomorrow",
        "Call dentist",
        "Birthday gift ideas:\n- Book\n- Headphones\n- Gift card",
        "WiFi password: Signal4Ever!",
        "Flight confirmation: ABC123",
        "Apartment viewing Saturday 10am",
        "Recipe: 2 cups flour, 1 cup sugar, 3 eggs, 1 tsp vanilla",
        "Book recommendation: The Pragmatic Programmer",
        "Parking spot: Level 3, Row B, #42",
        "Grocery list:\n- Milk\n- Eggs\n- Bread\n- Cheese\n- Apples",
        "Project deadline: March 15",
        "New address: 123 Main St, Apt 4B",
        "Insurance policy #: 987654321",
        "Track package: 1Z999AA10123456784",
        "Dr. appointment on Thursday at 2:30",
        "Interesting article to read later",
        "Backup code: 8A3F-2B7C-9D1E",
        "Gym schedule:\nMon - Chest\nWed - Back\nFri - Legs",
        "Movie to watch: Everything Everywhere All at Once"
      ).random(random)
    }
  }

  // --- Encryption ---

  private fun writeEncryptedMain(
    file: File,
    frames: List<com.squareup.wire.Message<*, *>>,
    aesKey: ByteArray,
    hmacKey: ByteArray
  ) {
    file.outputStream().use { fileOut ->
      // Write 16-byte random IV
      val iv = ByteArray(16).also { secureRandom.nextBytes(it) }
      fileOut.write(iv)
      fileOut.flush()

      // Create cipher
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
        init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), IvParameterSpec(iv))
      }

      // Create MAC, update with IV
      val mac = Mac.getInstance("HmacSHA256").apply {
        init(SecretKeySpec(hmacKey, "HmacSHA256"))
        update(iv)
      }

      // Wrap fileOut in a non-closing stream so the GZIP->Cipher->MAC chain can close()
      // without closing the underlying file stream (we still need to append the MAC after)
      val nonClosingOut = NonClosingOutputStream(fileOut)

      // Stream chain: PaddedGzip -> Cipher -> MAC -> NonClosingFile
      val macStream = MacOutputStream(nonClosingOut, mac)
      val cipherStream = CipherOutputStream(macStream, cipher)
      val gzipStream = PaddedGzipOutputStream(cipherStream)

      // Write frames
      for (frame in frames) {
        val encoded = frame.encode()
        gzipStream.writeVarInt32(encoded.size)
        gzipStream.write(encoded)
      }

      // Close gzip (finishes compression, adds padding, flushes cipher)
      // The NonClosingOutputStream prevents the underlying file stream from closing
      gzipStream.close()

      // Append MAC
      val macBytes = macStream.mac.doFinal()
      fileOut.write(macBytes)
    }
  }

  private fun writeMetadata(file: File, backupId: ByteArray, metadataKey: ByteArray) {
    // Encrypt backup ID with AES/CTR
    val iv = ByteArray(12).also { secureRandom.nextBytes(it) }
    // AES/CTR with 12-byte IV needs padding to 16 bytes for IvParameterSpec
    val ivForCipher = ByteArray(16)
    iv.copyInto(ivForCipher)

    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(metadataKey, "AES"), IvParameterSpec(ivForCipher))
    val encryptedBackupId = cipher.doFinal(backupId)

    val metadata = LocalMetadata(
      version = 1,
      backupId = LocalMetadata.EncryptedBackupId(
        iv = iv.toByteString(),
        encryptedId = encryptedBackupId.toByteString()
      )
    )

    file.writeBytes(metadata.encode())
  }

  // --- Helper classes ---

  /**
   * Wraps an OutputStream to prevent close() from propagating.
   * This allows stream chains (GZIP -> Cipher -> MAC) to close without closing the underlying file.
   */
  private class NonClosingOutputStream(private val wrapped: OutputStream) : FilterOutputStream(wrapped) {
    override fun close() {
      flush()
      // Don't close wrapped stream
    }
  }

  /**
   * Calculates a MAC as data is written through.
   */
  private class MacOutputStream(wrapped: OutputStream, val mac: Mac) : FilterOutputStream(wrapped) {
    override fun write(byte: Int) {
      out.write(byte)
      mac.update(byte.toByte())
    }

    override fun write(data: ByteArray) {
      write(data, 0, data.size)
    }

    override fun write(data: ByteArray, offset: Int, length: Int) {
      out.write(data, offset, length)
      mac.update(data, offset, length)
    }
  }

  /**
   * GZIPs content and adds padding using the same size-bucket algorithm as Signal Android.
   */
  private class PaddedGzipOutputStream private constructor(
    private val sizeObserver: SizeObservingOutputStream
  ) : GZIPOutputStream(sizeObserver) {

    constructor(outputStream: OutputStream) : this(SizeObservingOutputStream(outputStream))

    override fun finish() {
      super.finish()

      val totalLength = sizeObserver.size
      val paddedSize = getPaddedSize(totalLength)
      val paddingToAdd = (paddedSize - totalLength).toInt()

      sizeObserver.write(ByteArray(paddingToAdd))
    }

    private class SizeObservingOutputStream(val wrapped: OutputStream) : FilterOutputStream(wrapped) {
      var size: Long = 0L
        private set

      override fun write(b: Int) {
        wrapped.write(b)
        size++
      }

      override fun write(b: ByteArray) {
        wrapped.write(b)
        size += b.size
      }

      override fun write(b: ByteArray, off: Int, len: Int) {
        wrapped.write(b, off, len)
        size += len
      }
    }
  }

  // --- Utility functions ---

  private fun randomBytes(length: Int): ByteArray {
    return ByteArray(length).also { secureRandom.nextBytes(it) }
  }

  private fun UUID.toByteArray(): ByteArray {
    val buffer = java.nio.ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(this.mostSignificantBits)
    buffer.putLong(this.leastSignificantBits)
    return buffer.array()
  }

  private fun getPaddedSize(size: Long): Long {
    return max(541L, floor(1.05.pow(ceil(ln(size.toDouble()) / ln(1.05)))).toLong())
  }
}

/**
 * Simple protobuf representation for local backup metadata.
 * We generate this inline rather than compiling a .proto for it.
 */
private class LocalMetadata(val version: Int, val backupId: EncryptedBackupId) {
  class EncryptedBackupId(val iv: okio.ByteString, val encryptedId: okio.ByteString) {
    fun encode(): ByteArray {
      val out = ByteArrayOutputStream()
      // field 1: bytes iv
      out.write(0x0A) // tag: field 1, wire type 2 (length-delimited)
      out.writeVarInt32(iv.size)
      out.write(iv.toByteArray())
      // field 2: bytes encryptedId
      out.write(0x12) // tag: field 2, wire type 2 (length-delimited)
      out.writeVarInt32(encryptedId.size)
      out.write(encryptedId.toByteArray())
      return out.toByteArray()
    }
  }

  fun encode(): ByteArray {
    val out = ByteArrayOutputStream()
    // field 1: uint32 version
    if (version != 0) {
      out.write(0x08) // tag: field 1, wire type 0 (varint)
      out.writeVarInt32(version)
    }
    // field 2: message backupId
    val backupIdBytes = backupId.encode()
    out.write(0x12) // tag: field 2, wire type 2 (length-delimited)
    out.writeVarInt32(backupIdBytes.size)
    out.write(backupIdBytes)
    return out.toByteArray()
  }
}
