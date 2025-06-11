@file:Suppress("UNCHECKED_CAST")
@file:OptIn(ExperimentalStdlibApi::class)

import SeededRandom.seededShuffled
import com.squareup.wire.Message
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.signal.libsignal.zkgroup.ServerSecretParams
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation
import org.signal.libsignal.zkgroup.receipts.ReceiptSerial
import org.signal.libsignal.zkgroup.receipts.ServerZkReceiptOperations
import org.thoughtcrime.securesms.backup.v2.proto.*
import org.thoughtcrime.securesms.backup.v2.proto.misc.CompressedRistretto
import org.thoughtcrime.securesms.backup.v2.proto.misc.MaskedAmount
import org.thoughtcrime.securesms.backup.v2.proto.misc.Receipt
import java.security.SecureRandom
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.days

/**
 * Generates a list of snapshots, ensuring that a snapshot exists for each possible value of each individual generator.
 *
 * @param snapshotCount If set to a value > 0, it will generate exactly that many snapshots. Otherwise, it will generate
 *                      the minimum number required to cover the minSizes of all registered generators.
 */
fun permute(snapshotCount: Int = -1, generateSnapshot: PermutationScope.() -> Unit): List<List<Message<*, *>>> {
  val permutationScope = PermutationScope()

  val snapshots: MutableList<List<Message<*, *>>> = mutableListOf()

  if (snapshotCount > 0) {
    repeat(snapshotCount) {
      permutationScope.generateSnapshot()
      snapshots += permutationScope.next()
    }
  } else {
    while (permutationScope.hasNext()) {
      permutationScope.generateSnapshot()
      snapshots += permutationScope.next()
    }
  }

  return snapshots
}

/**
 * Like [permute], but assumes that each snapshot only has a single element, and returns a list of that element type.
 */
private fun <T> permuteSingle(snapshotCount: Int = -1, init: PermutationScope.() -> Unit): List<T> {
  val snapshots = permute(snapshotCount) { init() }
  return snapshots.map {
    if (it.isEmpty()) throw IllegalStateException("No frames in snapshot! Did you forget to add to `frames`?")
    it[0] as T
  }
}

/**
 * Given a list of generators, returns a new list of generators that are null prefixed/suffixed to ensure that only
 * one generator will output a value at any given time. This is to aid in generating permutations for oneof fields.
 *
 * Example:
 *
 * ```kotlin
 * val (gen1, gen2) = oneOf(Generators.list("a", "b"), Generators.list("c", "d"))
 *
 * // The generators are now effectively:
 * // gen1: ("a", "b", null, null)
 * // gen2: (null, null, "c", "d")
 * ```
 *
 * When you destructure things like this, the typing can get a bit weird/annoying, so [someOneOf] was made to more
 * easily force the correct typing.
 *
 * ```kotlin
 * val (gen1, gen2) = oneOf(Generators.list("a", "b"), Generators.list("c", "d"))
 * // ...
 * frames += MyModel(myField = someOneOf(gen1))
 */
fun oneOf(vararg generators: Generator<Any?>): List<Generator<Any?>> {
  val sizes = generators.map { it.minSize }
  val totalSize = sizes.sum()
  val output: MutableList<Generator<Any?>> = mutableListOf()
  var prefixNullCount = 0

  for (i in 0 until sizes.size) {
    val prefixNulls = NullGenerator<Any>(prefixNullCount)
    val suffixNulls = NullGenerator<Any>(totalSize - prefixNullCount - sizes[i])
    output.add(Generators.merge(prefixNulls, generators[i], suffixNulls))
    prefixNullCount += sizes[i]
  }

  return output
}

class PermutationScope : Iterator<List<Message<*, *>>> {
  val frames: MutableList<Message<*, *>> = mutableListOf()

  private val registeredGenerators: MutableList<Generator<*>> = mutableListOf()

  private var generatorIndex = 0
  private var snapshotIndex = 0
  private var targetSnapshotCount = 1

  override fun hasNext(): Boolean {
    return snapshotIndex < targetSnapshotCount
  }

  override fun next(): List<Message<*, *>> {
    generatorIndex = 0
    snapshotIndex++

    return frames.toList().also {
      frames.clear()
    }
  }

  fun <T> somePermutation(init: PermutationScope.() -> Unit): T {
    val snapshots = permuteSingle<T> { init() }
    return some(ListGenerator(snapshots))
  }

  fun <T> someNullablePermutation(init: PermutationScope.() -> Unit): T? {
    val snapshots = permuteSingle<T> { init() }
    return some(ListGenerator(snapshots).nullable())
  }

  fun someString(): String = some(Generators.strings())

  fun someNonEmptyString(): String = some(Generators.nonEmptyStrings())

  fun someNullableString(): String? = some(Generators.strings().nullable())

  fun someEmoji(): String = some(Generators.emoji())

  fun someBoolean(): Boolean = some(Generators.booleans())

  fun someNullableBoolean(): Boolean? = some(Generators.booleans().nullable())

  fun someInt(lower: Int = Int.MIN_VALUE, upper: Int = Int.MAX_VALUE): Int = some(Generators.ints(lower, upper))

  fun someNullableInt(): Int? = some(Generators.ints().nullable())

  fun somePositiveInt(): Int = some(Generators.ints(0))

  fun somePositiveLong(): Long = some(Generators.longs(0))

  fun someFloat(lower: Float = Float.MIN_VALUE, upper: Float = Float.MAX_VALUE): Float = some(Generators.floats(lower, upper))

  fun someBytes(size: Int): ByteArray = some(Generators.bytes(size))

  fun someNullableBytes(size: Int): ByteArray? = some(Generators.bytes(size).nullable())

  fun someUrl(): String = some(Generators.urls())

  fun someTimestamp(lower: Long = 1659383695000, upper: Long = 1911844456000): Long = some(Generators.timestamps(lower, upper))

  fun someNonZeroTimestamp(lower: Long = 1659383695000, upper: Long = 1911844456000): Long = some(Generators.nonZeroTimestamps(lower, upper))

  fun someIncrementingTimestamp(lower: Long = 1659383695000, upper: Long = 1911844456000): Long = some(Generators.incrementingTimestamps(lower, upper))

  fun someDecrementingTimestamp(lower: Long = 1659383695000, upper: Long = 1911844456000): Long = some(Generators.decrementingTimestamps(lower, upper))

  fun someExpirationTimerMs(): Long = some(Generators.expirationTimersMs())

  /**
   * Expiration timer versions can be any positive Int. (Negative values are
   * never used, and 0 is reserved as a default value for legacy clients who
   * are unaware of this field in any protos; no client capable of Backups
   * will ever use a value of 0.)
   */
  fun someExpirationTimerVersion(): Int = some(Generators.ints(lower = 1, upper = Int.MAX_VALUE))

  fun someE164(): Long = some(Generators.e164s())

  fun someUuid(): UUID = some(Generators.uuids())

  fun someColor(): Int = some(Generators.colors())

  fun someNullableUsername(): String? = some(Generators.usernames().nullable())

  fun <T> someEnum(clazz: Class<T>, excluding: T? = null): T = excluding?.let { some(Generators.enum(clazz, excluding)) } ?: some(Generators.enum(clazz))
  fun <T> someEnum(clazz: Class<T>, vararg excluding: T): T = some(Generators.enum(clazz, *excluding))
  inline fun <reified T> someEnum(clazz: Class<T>, excluding: List<T>): T = some(Generators.enum(clazz, *excluding.toTypedArray()))

  fun someCallLinkRootKey(): ByteArray = some(Generators.callLinkRootKey())

  fun <T> some(generator: Generator<T>): T {
    if (snapshotIndex == 0) {
      registeredGenerators.add(generator)
      targetSnapshotCount = max(targetSnapshotCount, generator.minSize)
    }

    return registeredGenerators[generatorIndex++].next() as T
  }

  fun <T> someOneOf(generator: Generator<Any?>): T? = some(generator as Generator<T?>)
}

fun <T> Generator<T>.nullable(): Generator<T?> {
  return Generators.merge(NullGenerator(1), this as Generator<T?>)
}

fun <T> Generator<T>.plus(vararg extraElements: T): Generator<T> {
  return Generators.merge(this, Generators.list(*extraElements))
}

fun <T> Generator<T>.asList(vararg sizes: Int): Generator<List<T>> {
  val target = this

  return object : Generator<List<T>> {
    private var sizeIndex = 0

    override val minSize: Int = sizes.size
    override fun next(): List<T> = (0 until sizes[sizeIndex]).map { target.next() }.also { sizeIndex = (sizeIndex + 1) % sizes.size }
  }
}

fun <T, R> Generator<T>.map(transform: (T) -> R): Generator<R> {
  val target = this
  return object : Generator<R> {
    override val minSize: Int = target.minSize
    override fun next(): R = transform(target.next())
  }
}

fun <T> T.asGenerator(): Generator<T> {
  return Generators.single(this)
}

/**
 * A generator is a class that generates an infinite sequence of values of type [T].
 *
 * While it should always produce a value, the generator specifies a [minSize], indicating the minimum number of values
 * it should generate before being considered "complete".
 */
interface Generator<T> {
  val minSize: Int
  fun next(): T
}

private class ListGenerator<T>(val list: List<T>) : Generator<T> {
  private var index = 0

  override val minSize: Int = list.size
  override fun next(): T = list[index++ % list.size]
}

private class NullGenerator<T>(override val minSize: Int) : Generator<T?> {
  override fun next(): T? = null
}

private class UuidGenerator : Generator<UUID> {
  override val minSize: Int = 1
  override fun next(): UUID = SeededRandom.uuid()
}

private class ByteGenerator(private val numBytes: Int, override val minSize: Int = 1) : Generator<ByteArray> {
  override fun next(): ByteArray = SeededRandom.bytes(numBytes)
}

private class IncrementingTimestampGenerator(lower: Long = 1659383695000, upper: Long = 1911844456000) : Generator<Long> {
  private var onDeck = SeededRandom.long(lower = lower, upper = upper)

  override val minSize: Int = 2
  override fun next(): Long = onDeck.also { onDeck += 1 }
}

private class DecrementingTimestampGenerator(lower: Long, upper: Long) : Generator<Long> {
  private var onDeck = SeededRandom.long(lower = lower, upper = upper)

  override val minSize: Int = 2
  override fun next(): Long = onDeck.also { onDeck -= 1 }
}

private class CallLinkRootKeyGenerator : Generator<ByteArray> {
  override val minSize: Int = 16

  override fun next(): ByteArray {
    var result = SeededRandom.bytes(this.minSize)
    while (this.hasRepeatedChunk(result)) {
      result = SeededRandom.bytes(this.minSize)
    }
    return result
  }

  // See https://github.com/signalapp/ringrtc-private/blob/cdde6532a836d085a7a8318abc65646ac7b86783/src/rust/src/lite/call_links/root_key.rs#L31
  fun hasRepeatedChunk(value: ByteArray): Boolean {
    for ((l, r) in value.asList().chunked(2)) {
      var word = l.toInt() + r.toInt().shl(8)
      if (word % 0x1111 == 0) {
        return true
      }
    }
    return false
  }
}

object Generators {
  fun strings(): Generator<String> = Generators.list("", SeededRandom.string(), SeededRandom.string())
  fun nonEmptyStrings(): Generator<String> = Generators.list(SeededRandom.string(), SeededRandom.string())
  fun emoji(): Generator<String> = Generators.list("\uD83D\uDC80", "👍", "👎", "\uD83D\uDC4D\uD83C\uDFFE", "\uD83D\uDC69\u200D\uD83D\uDCBB")
  fun usernames(): Generator<String> = Generators.list("${SeededRandom.lipsum.firstName.lowercase()}.${SeededRandom.int(10, 10000)}", "${SeededRandom.lipsum.firstName.lowercase()}.${SeededRandom.int(10, 10000)}", "${SeededRandom.lipsum.firstName.lowercase()}.0${SeededRandom.int(1, 10)}")
  fun booleans(): Generator<Boolean> {
    val first = SeededRandom.boolean()
    val second = !first
    return Generators.list(first, second)
  }
  fun ints(lower: Int = Int.MIN_VALUE, upper: Int = Int.MAX_VALUE): Generator<Int> = Generators.list(SeededRandom.int(lower, upper), SeededRandom.int(lower, upper), SeededRandom.int(lower, upper))
  fun longs(lower: Long = Long.MIN_VALUE, upper: Long = Long.MAX_VALUE): Generator<Long> = Generators.list(0L, SeededRandom.long(lower, upper), SeededRandom.long(lower, upper))
  fun nonZeroLongs(lower: Long = Long.MIN_VALUE, upper: Long = Long.MAX_VALUE): Generator<Long> = Generators.list(SeededRandom.long(lower, upper), SeededRandom.long(lower, upper), SeededRandom.long(lower, upper))
  fun floats(lower: Float = Float.MIN_VALUE, upper: Float = Float.MAX_VALUE): Generator<Float> = Generators.list(0f, SeededRandom.float(lower, upper), SeededRandom.float(lower, upper))
  fun bytes(numBytes: Int, minSize: Int = 1): Generator<ByteArray> = ByteGenerator(numBytes, minSize)
  fun <T> enum(clazz: Class<T>, excluding: T? = null): Generator<T> = ListGenerator(clazz.enumConstants.filterNot { it == excluding }.toList())
  fun <T> enum(clazz: Class<T>, vararg excluding: T): Generator<T> = ListGenerator(clazz.enumConstants.filterNot { excluding.contains(it) }.toList())
  fun urls(): Generator<String> = Generators.list("", "https://signal.org/" + SeededRandom.string(), "https://signal.org/" + SeededRandom.string())
  fun nonEmptyUrls(): Generator<String> = Generators.list("https://signal.org/" + SeededRandom.string(), "https://signal.org/" + SeededRandom.string())
  fun timestamps(lower: Long = 1659383695000, upper: Long = 1911844456000): Generator<Long> = Generators.list(0L, SeededRandom.long(lower, upper), SeededRandom.long(lower, upper))
  fun nonZeroTimestamps(lower: Long = 1659383695000, upper: Long = 1911844456000): Generator<Long> = Generators.list(SeededRandom.long(lower, upper), SeededRandom.long(lower, upper))
  fun incrementingTimestamps(lower: Long = 1659383695000, upper: Long = 1911844456000): Generator<Long> = IncrementingTimestampGenerator(lower, upper)
  fun decrementingTimestamps(lower: Long = 1659383695000, upper: Long = 1911844456000): Generator<Long> = DecrementingTimestampGenerator(lower, upper)
  fun e164s(): Generator<Long> = Generators.list(seededRandomE164(), seededRandomE164())
  fun uuids(): Generator<UUID> = UuidGenerator()
  fun cdnNumbers(): Generator<Int> = Generators.list(0, 2, 3)
  fun emails(): Generator<String> = Generators.list("${SeededRandom.lipsum.firstName}@${SeededRandom.lipsum.lastName}.com", "${SeededRandom.string()}@${SeededRandom.string()}.org")
  fun blurHashes(): Generator<String> = Generators.list("LfLh6Voa9NIW?wNF-ooL-;WAX8oy", "LGG*f,-i.l-o?G\$~?Zt7pHN1=tE3", "LdIOX?NE9Y4T~pRPRjE1X9f5jrt6", "LJR,66e.~Cxu%LoLM|S2%3WWIosm", "LIM:}RB8?-^L.d4]O.nkK_ruI?od")
  fun picoMobs(): Generator<String> = Generators.list(SeededRandom.string(18, 25, "123456789"), SeededRandom.string(18, 25, "123456789"))
  fun colors(): Generator<Int> = Generators.list(seededRandomColor(), seededRandomColor(), seededRandomColor())
  fun names(): Generator<String> = Generators.list(List(3) { SeededRandom.lipsum.name })
  fun firstNames(count: Int = 3): Generator<String> = Generators.list(List(count) { SeededRandom.lipsum.firstName })
  fun lastNames(count: Int = 3): Generator<String> = Generators.list(List(count) { SeededRandom.lipsum.lastName })
  fun textBody(minWords: Int = 1, maxWords: Int = min(10, minWords)): Generator<String> = Generators.list(List(3) { SeededRandom.lipsum.getWords(1, 10) })
  fun titles(): Generator<String> = Generators.list(List(3) { SeededRandom.lipsum.getTitle(2, 3) })

  /**
   * Expiration timers are 64-bit values that should be second-aligned
   * (divisible by 1000), and whose value-as-seconds should fit into 32-bits.
   */
  fun expirationTimersMs(): Generator<Long> = Generators.list(0L, SeededRandom.long(0, Int.MAX_VALUE.toLong()) * 1000, SeededRandom.long(0, Int.MAX_VALUE.toLong()) * 1000)

  fun receiptCredentialPresentation(): Generator<ReceiptCredentialPresentation> {
    // libsignal requires a SecureRandom for zkgroup operations
    val derivedRandom = SecureRandom.getInstance("SHA1PRNG")
    derivedRandom.setSeed(SeededRandom.long())
    val serverParams = ServerSecretParams.generate(derivedRandom)
    val clientOps = ClientZkReceiptOperations(serverParams.publicParams)

    val serial = ReceiptSerial(SeededRandom.bytes(ReceiptSerial.SIZE))
    val context = clientOps.createReceiptCredentialRequestContext(derivedRandom, serial)
    val response = ServerZkReceiptOperations(serverParams).issueReceiptCredential(derivedRandom, context.request, 0, 1)
    val credential = clientOps.receiveReceiptCredential(context, response)
    val presentation = clientOps.createReceiptCredentialPresentation(derivedRandom, credential)

    return presentation.asGenerator()
  }

  fun <T> list(vararg items: T): Generator<T> = ListGenerator(items.toList())
  fun <T> list(items: List<T>): Generator<T> = ListGenerator(items)
  fun <T> randomizedList(items: List<T>): Generator<T> = ListGenerator(items.seededShuffled())
  fun <T> single(item: T): Generator<T> = Generators.list(item)

  fun <T> permutation(snapshotCount: Int = -1, init: PermutationScope.() -> Unit): Generator<T> {
    val snapshots = permuteSingle<T> { init() }
    return ListGenerator(snapshots)
  }

  fun <T> merge(vararg generators: Generator<T>): Generator<T> {
    val allItems: List<T> = mutableListOf<T>().apply {
      generators.forEach { generator ->
        repeat(generator.minSize) {
          add(generator.next())
        }
      }
    }

    return ListGenerator(allItems)
  }

  fun wallpaperFilePointer(): Generator<FilePointer> {
    return Generators.permutation {
      val locatorInfo = FilePointer.LocatorInfo(
        key = someBytes(16).toByteString(),
        size = somePositiveInt(),
        plaintextHash = someBytes(32).toByteString(),
        mediaTierCdnNumber = some(Generators.cdnNumbers().nullable()),
        localKey = null
      )
      val contentType: String = some(Generators.list("image/jpeg", "image/png"))

      frames += FilePointer(
        invalidAttachmentLocator = FilePointer.InvalidAttachmentLocator(),
        locatorInfo = locatorInfo,
        contentType = contentType,
        incrementalMac = null,
        incrementalMacChunkSize = null,
        fileName = null,
        width = null,
        height = null,
        caption = null,
        blurHash = some(Generators.blurHashes().nullable())
      )
    }
  }

  fun avatarFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("image/jpeg", "image/png")
  )

  fun longTextFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = false,
    includeCaption = false,
    includeBlurHash = false,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("text/x-signal-plain")
  )

  fun voiceMessageFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = false,
    includeCaption = false,
    includeBlurHash = false,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("audio/mp3")
  )

  fun gifFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("video/mp4")
  )

  fun borderlessFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("image/png")
  )

  fun quoteFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = false,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = false,
    contentTypeGenerator = "image/jpeg".asGenerator()
  )

  fun linkPreviewFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = false,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("image/jpeg", "image/png")
  )

  fun viewOnceFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = false,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = true,
    includeIncrementalMac = true,
    contentTypeGenerator = Generators.list("image/jpeg", "image/png", "image/gif", "video/mp4")
  )

  fun stickerFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = true,
    includeCaption = false,
    includeBlurHash = false,
    includeIncrementalMac = false,
    contentTypeGenerator = Generators.list("image/png", "image/apng", "image/webp")
  )

  fun bodyAttachmentFilePointer(): Generator<FilePointer> = filePointerInternal(
    includeFileName = true,
    includeMediaSize = true,
    includeCaption = true,
    includeBlurHash = true,
    includeIncrementalMac = true,
    contentTypeGenerator = Generators.list("image/jpeg", "image/png", "image/gif", "audio/mp3", "video/mp4")
  )

  fun smsAttachmentFilePointer(): Generator<FilePointer> {
    return Generators.permutation {
      val contentType = some(Generators.list("image/jpeg", "image/png", "audio/mp3", "video/mp4", "application/pdf"))
      val blurHashSupported = contentType.startsWith("image") || contentType.startsWith("video")

      val locatorInfo = FilePointer.LocatorInfo(
        key = someBytes(16).toByteString(),
        plaintextHash = someBytes(32).toByteString(),
        size = somePositiveInt(),
        mediaTierCdnNumber = some(Generators.cdnNumbers().nullable()),
        localKey = null
      )

      frames += FilePointer(
        invalidAttachmentLocator = FilePointer.InvalidAttachmentLocator(),
        locatorInfo = locatorInfo,
        contentType = contentType,
        incrementalMac = null,
        incrementalMacChunkSize = null,
        fileName = some(Generators.nonEmptyStrings().nullable()),
        width = some(Generators.ints(0, 4096).nullable()),
        height = some(Generators.ints(0, 4096).nullable()),
        caption = null,
        blurHash = if (blurHashSupported) some(Generators.blurHashes().nullable()) else null
      )
    }
  }

  private fun filePointerInternal(
    includeFileName: Boolean,
    includeMediaSize: Boolean,
    includeCaption: Boolean,
    includeBlurHash: Boolean,
    includeIncrementalMac: Boolean,
    contentTypeGenerator: Generator<String>
  ): Generator<FilePointer> {
    // As an optimization during restore clients may discard attachment locators
    // that are assumed to have expired from the CDN. To that end, ensure that
    // all attachment locators in the tests are "valid" per that optimization.
    //
    // At the time of writing, transit-tier CDN media lives for 45d.
    val attachmentLocatorUploadMaxTimestamp: Long = StandardFrames.BACKUP_TIME_MS
    val attachmentLocatorUploadMinTimestamp: Long = attachmentLocatorUploadMaxTimestamp - 45.days.inWholeMilliseconds

    return Generators.permutation {
      val previouslyDownloadedAndValidated = someBoolean()
      val includeTransitTierInfo = someBoolean()

      val potentialKey = someBytes(16)
      val potentialDigest = someBytes(16)
      val potentialPlaintextHash = someBytes(32)
      val potentialSize = somePositiveInt()
      val potentialMediaTierCdnNumber = some(Generators.cdnNumbers().nullable())

      var plaintextHash: ByteArray? = null
      var digest: ByteArray? = null
      var mediaTierCdnNumber: Int? = null

      if (previouslyDownloadedAndValidated) {
        plaintextHash = potentialPlaintextHash
        mediaTierCdnNumber = potentialMediaTierCdnNumber
      } else if (includeTransitTierInfo) {
        digest = potentialDigest
      }

      val potentialTransitCdnKey = some(Generators.nonEmptyStrings())
      val potentialTransitCdnNumber = some(Generators.cdnNumbers())
      val potentialTransitUploadTimestamp = someNonZeroTimestamp(
        lower = attachmentLocatorUploadMinTimestamp,
        upper = attachmentLocatorUploadMaxTimestamp
      )

      var transitCdnKey: String? = null
      var transitCdnNumber: Int? = null
      var transitUploadTimestamp: Long? = null
      if (includeTransitTierInfo) {
        transitCdnKey = potentialTransitCdnKey
        transitCdnNumber = potentialTransitCdnNumber
        transitUploadTimestamp = potentialTransitUploadTimestamp
      }

      val hasSomePointer = previouslyDownloadedAndValidated || includeTransitTierInfo

      var key: ByteArray = byteArrayOf()
      var size = 0
      if (hasSomePointer) {
        key = potentialKey
        size = potentialSize
      }

      val locatorInfo = FilePointer.LocatorInfo(
        key = key.toByteString(),
        encryptedDigest = digest?.toByteString(),
        plaintextHash = plaintextHash?.toByteString(),
        size = size,
        transitCdnKey = transitCdnKey,
        transitCdnNumber = transitCdnNumber,
        transitTierUploadTimestamp = transitUploadTimestamp,
        mediaTierCdnNumber = mediaTierCdnNumber,
        localKey = null
      )

      val potentialIncrementalMac = some(Generators.bytes(16).nullable())
      val incrementalMac = if (includeIncrementalMac && hasSomePointer) potentialIncrementalMac else null
      val incrementalMacChunkSize: Int? = some(Generators.list(1024, 2048))

      val contentType = some(contentTypeGenerator)
      val blurHashSupported = contentType.startsWith("image") || contentType.startsWith("video")

      frames += FilePointer(
        invalidAttachmentLocator = FilePointer.InvalidAttachmentLocator(),
        locatorInfo = locatorInfo,
        contentType = contentType,
        incrementalMac = incrementalMac?.toByteString(),
        incrementalMacChunkSize = if (incrementalMac != null) {
          incrementalMacChunkSize
        } else {
          null
        },
        fileName = if (includeFileName) some(Generators.nonEmptyStrings().nullable()) else null,
        width = if (includeMediaSize) some(Generators.ints(0, 4096).nullable()) else null,
        height = if (includeMediaSize) some(Generators.ints(0, 4096).nullable()) else null,
        caption = if (includeCaption) someNullableString() else null,
        blurHash = if (includeBlurHash && blurHashSupported) some(Generators.blurHashes().nullable()) else null
      )
    }
  }

  fun callLinkRootKey(): Generator<ByteArray> = CallLinkRootKeyGenerator()

  fun sendStatus(recipientIdGenerator: Generator<Long>): Generator<SendStatus> {
    val (
      pendingGenerator,
      sentGenerator,
      deliveredGenerator,
      readGenerator,
      viewedGenerator,
      skippedGenerator,
      failedGenerator
    ) = oneOf(
      Generators.list(SendStatus.Pending()),
      Generators.permutation {
        frames += SendStatus.Sent(
          sealedSender = someBoolean()
        )
      },
      Generators.permutation {
        frames += SendStatus.Delivered(
          sealedSender = someBoolean()
        )
      },
      Generators.permutation {
        frames += SendStatus.Read(
          sealedSender = someBoolean()
        )
      },
      Generators.permutation {
        frames += SendStatus.Viewed(
          sealedSender = someBoolean()
        )
      },
      Generators.list(SendStatus.Skipped()),
      Generators.permutation {
        frames += SendStatus.Failed(
          reason = someEnum(SendStatus.Failed.FailureReason::class.java)
        )
      }
    )

    return Generators.permutation {
      frames += SendStatus(
        recipientId = some(recipientIdGenerator),
        timestamp = someTimestamp(),
        pending = someOneOf(pendingGenerator),
        sent = someOneOf(sentGenerator),
        delivered = someOneOf(deliveredGenerator),
        read = someOneOf(readGenerator),
        viewed = someOneOf(viewedGenerator),
        skipped = someOneOf(skippedGenerator),
        failed = someOneOf(failedGenerator)
      )
    }
  }

  /**
   * Generates a pair of generators that can be used for setting incoming/outgoing message details.
   * For outgoing statuses, one will be made for each of the outgoing recipients.
   */
  fun incomingOutgoingDetails(vararg outgoingRecipients: Recipient): Pair<Generator<ChatItem.IncomingMessageDetails?>, Generator<ChatItem.OutgoingMessageDetails?>> {
    val sendStatusGenerators = outgoingRecipients.map { outgoingRecipient ->
      Generators.sendStatus(
        recipientIdGenerator = Generators.single(outgoingRecipient.id)
      )
    }

    val (incoming, outgoing) = oneOf(
      Generators.permutation {
        frames += ChatItem.IncomingMessageDetails(
          dateReceived = someIncrementingTimestamp(),
          dateServerSent = someIncrementingTimestamp(),
          read = someBoolean(),
          sealedSender = someBoolean()
        )
      },
      Generators.permutation {
        frames += ChatItem.OutgoingMessageDetails(
          sendStatus = sendStatusGenerators.map { some(it) }
        )
      }
    )

    return incoming as Generator<ChatItem.IncomingMessageDetails?> to outgoing as Generator<ChatItem.OutgoingMessageDetails?>
  }

  fun reactions(maxReactions: Int, vararg authors: Recipient): Generator<List<Reaction>> {
    return Generators.lists((0..maxReactions).toList()) {
      Generators.permutation<Reaction> {
        frames += Reaction(
          emoji = someEmoji(),
          authorId = some(Generators.list(authors.map { it.id })),
          sentTimestamp = someIncrementingTimestamp(),
          sortOrder = some(
            Generators.merge(
              Generators.list(1, 2),
              Generators.timestamps()
            )
          )
        )
      }
    }
  }

  /**
   * Returns a generator that generates a list of T. The size of the lists it outputs will be determined by [sizes].
   * It will use a fresh generator for each list, generated by [generatorFactory].
   *
   * Note that this is different from [Generator.asList], which does  _not_ reset the generator for each list, instead
   * using the same generator throughout.
   */
  fun <T> lists(sizes: List<Int>, generatorFactory: () -> Generator<T>): Generator<List<T>> {
    return object : Generator<List<T>> {
      var sizeIndex = 0

      override val minSize: Int = sizes.size

      override fun next(): List<T> {
        val generator = generatorFactory()
        return (0 until sizes[sizeIndex])
          .map { generator.next() }
          .also { sizeIndex = (sizeIndex + 1) % sizes.size }
      }
    }
  }

  fun mobileCoinReceipts(): Generator<ByteArray> {
    return object : Generator<ByteArray> {
      override val minSize: Int = 1

      override fun next(): ByteArray {
        return Receipt(
          public_key = CompressedRistretto(
            data_ = ByteArray(32) { 0 }.toByteString()
          ),
          masked_amount_v2 = MaskedAmount(
            commitment = CompressedRistretto(
              data_ = ByteArray(32) { 0 }.toByteString()
            ),
            masked_value = SeededRandom.long(1, 1000)
          )
        ).encode()
      }
    }
  }

  /**
   * IMPORTANT: Assumes the input text is at least 10 characters long, for simplicity.
   * The text is passed in as an arg to ensure that.
   */
  fun bodyRanges(inputText: String, mentionAcis: List<ByteString> = emptyList()): Generator<List<BodyRange>> {
    check(inputText.length >= 10)

    val (mentionGenerator, styleGenerator) = oneOf(
      Generators.list(mentionAcis),
      Generators.enum(BodyRange.Style::class.java, excluding = BodyRange.Style.NONE) as Generator<Any?>
    )

    // Mentions and styles can occupy identical or disjoint ranges, but cannot
    // partially overlap. (I.e., styles cannot be applied to part-but-not-all
    // of a mention.)
    val mentionRangeGenerator = Generators.list(
      Pair(6, 2),
      Pair(8, 2)
    )

    // Includes intentional doubles, so we get multiple styles with the same
    // exact ranges.
    val styleRangeGenerator = Generators.list(
      Pair(0, 1),
      Pair(0, 4),
      Pair(0, 4),
      Pair(1, 5),
      Pair(2, 2),
      Pair(6, 2),
      Pair(6, 2)
    )

    return Generators.permutation<BodyRange> {
      val style: BodyRange.Style? = someOneOf(styleGenerator)
      val styleRange = some(styleRangeGenerator)

      val mentionRange = some(mentionRangeGenerator)
      val mention: ByteString? = someOneOf(mentionGenerator)

      val range: Pair<Int, Int> = if (style != null) {
        styleRange
      } else {
        mentionRange
      }

      frames += BodyRange(
        start = range.first,
        length = range.second,
        style = style,
        mentionAci = mention
      )
    }.asList(0, 1, 2, 3, 4, 5).map { it.distinct() }
  }

  fun identityKeys(): Generator<ByteArray> {
    return object : Generator<ByteArray> {
      override val minSize: Int = 3

      private val keys: List<ByteArray> = listOf(
        "BTHioSz61vk56IrCsF7FpCtd5tNxaOi5cp0rEMJZ+Pwc",
        "BZIZF+IBHFzbhGpJEMjAUX5r7Nn1XHV4EXmlim9OoCEc",
        "Bd7i9lwcmlGg9WZ6VqyhjEPlZoTmcZ3/+pBFkdyyJ5cf",
        "Bd1GCTHRzKOVLe144aHdvgVM92s/CQicyk3MzdTThQke",
        "BU08gfd+p10S5voc+WqJNOEZTo2QMV61sBraYjCs4MVs",
        "BWtizh6hq9p0cbqJn1aUpvkNEwteGh3IYp3pO8qnc9U7",
        "BXe0xqvqiIAJapWSz4RUygZnm7Qb5dcjW5Y8TGiCD5Q8",
        "BetOG3RdxwxtPdCwGYSmLUmf7xIlsc0mjVmRYx0SQs19",
        "BX/VF+lxdxrb+9Pa0eD8q7f5yh91+Z5XOr/bjIixAMFc",
        "BbCgl7aN4F8LBRB8mDLbptPKhzeamcJbYfg//SjynWhA",
        "BXKspvBGxezIMpBvedbLVk1Yhl60RQHcKGGJAaijMOdB"
      ).map { base64Decode(it) }

      private var index = 0

      override fun next(): ByteArray {
        return keys[index++ % keys.size]
      }
    }
  }

  /** Generates a random e164 in the protected test number range */
  private fun seededRandomE164(): Long {
    return when (SeededRandom.int(0, 3)) {
      0 -> "1${SeededRandom.int(100, 1000)}55501${SeededRandom.int(10, 100)}"
      1 -> "447700900${SeededRandom.int(100, 1000)}"
      2 -> "3363998${SeededRandom.int(1000, 10_000)}"
      else -> throw AssertionError("Unreachable")
    }.toLong()
  }

  private fun seededRandomColor(): Int {
    return ("FF" + SeededRandom.string(from = 6, until = 7, characterSet = "0123456789ABCDEF")).hexToInt()
  }
}

private operator fun <T> List<T>.component6(): T = this[5]
private operator fun <T> List<T>.component7(): T = this[6]
