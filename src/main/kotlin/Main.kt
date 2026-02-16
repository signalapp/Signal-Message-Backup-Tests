@file:Suppress("UNCHECKED_CAST")
@file:OptIn(ExperimentalStdlibApi::class)

import com.squareup.wire.Message
import org.signal.libsignal.messagebackup.ComparableBackup
import org.signal.libsignal.messagebackup.MessageBackup
import org.signal.libsignal.messagebackup.ValidationError
import org.thoughtcrime.securesms.backup.v2.proto.BackupInfo
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import tests.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

const val OUTPUT_DIR = "test-cases"

val ALL_TEST_CASES = listOf(
  StandardFramesTestCase,
  AccountDataTestCase,
  AndroidAccountDataTestCase,
  RecipientSelfTestCase,
  RecipientContactsTestCase,
  RecipientGroupsTestCase,
  RecipientDistributionListTestCase,
  RecipientCallLinkTestCase,
  StickerPackTestCase,
  AdHocCallTestCase,
  NotificationProfileTestCase,
  ChatFolderTestCase,
  ChatTestCase,
  ChatItemSimpleUpdatesTestCase,
  ChatItemContactMessageTestCase,
  ChatItemLearnedProfileUpdateTestCase,
  ChatItemProfileChangeUpdateTestCase,
  ChatItemSessionSwitchoverUpdateTestCase,
  ChatItemThreadMergeUpdateTestCase,
  ChatItemExpirationTimerUpdateTestCase,
  ChatItemPaymentNotificationTestCase,
  ChatItemGiftBadgeTestCase,
  ChatItemIndividualCallUpdateTestCase,
  ChatItemGroupCallUpdateTestCase,
  ChatItemStandardMessageTextOnlyTestCase,
  ChatItemStandardMessageGroupTextOnlyTestCase,
  ChatItemStandardMessageFormattedTextTestCase,
  ChatItemStandardMessageSmsTestCase,
  ChatItemStandardMessageStandardAttachmentsTestCase,
  ChatItemStandardMessageStandardAttachmentsIncrementalMacTestCase,
  ChatItemStandardMessageSpecialAttachmentsTestCase,
  ChatItemStandardMessageLongTextTestCase,
  ChatItemStandardMessageWithEditsTestCase,
  ChatItemStandardMessageWithLinkPreviewTestCase,
  ChatItemStandardMessageWithQuoteTestCase,
  ChatItemStickerMessageTestCase,
  ChatItemRemoteDeleteTestCase,
  ChatItemViewOnceTestCase,
  ChatItemGroupChangeChatUpdateTestCase,
  ChatItemGroupChangeChatMultipleUpdateTestCase,
  ChatItemDirectStoryReplyTestCase,
  ChatItemDirectStoryReplyWithEditsTestCase,
  ChatItemPollSingleVoteTestCase,
  ChatItemPollMultipleVoteTestCase,
  ChatItemPollTerminateUpdateTestCase,
  ChatItemPinMessageTestCase,
  ChatItemPinMessageUpdateTestCase
)

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    generateTests()
    return
  }

  if (args.size == 1 && args[0] == "generate-tests") {
    generateTests()
    return
  }

  if (args.size == 1 && args[0] in setOf("help", "--help")) {
    printHelp()
    return
  }

  if (args.size == 2 && args[0] == "print") {
    prettyPrintBackup(path = args[1])
    return
  }

  if (args[0] == "generate-local-backup") {
    generateLocalBackup(args.drop(1))
    return
  }

  printHelp()
}

private fun generateTests() {
  val startTime = System.currentTimeMillis()
  println("Initializing...")
  init()
  runAllTests()
  println("Complete! Took ${System.currentTimeMillis() - startTime} ms.")
}

private fun prettyPrintBackup(path: String) {
  val fileStream = FileInputStream(path)
  val frames = readAllFramesFromStream(fileStream)
  val prettyPrinted = frames.joinToString("\n\n") { it.prettyPrint() }
  println(prettyPrinted)
}

private fun generateLocalBackup(args: List<String>) {
  var credentialsPath: String? = null
  var outputDir: String = "local-backup-output"
  var messageCount: Int = 500

  val iter = args.iterator()
  while (iter.hasNext()) {
    when (val arg = iter.next()) {
      "--credentials" -> credentialsPath = iter.next()
      "--output" -> outputDir = iter.next()
      "--messages" -> messageCount = iter.next().toInt()
      else -> {
        if (credentialsPath == null && !arg.startsWith("--")) {
          credentialsPath = arg
        } else {
          System.err.println("Unknown argument: $arg")
          printHelp()
          return
        }
      }
    }
  }

  if (credentialsPath == null) {
    System.err.println("Error: credentials file path is required")
    println()
    printHelp()
    return
  }

  val credentialsFile = File(credentialsPath)
  if (!credentialsFile.exists()) {
    System.err.println("Error: credentials file not found: $credentialsPath")
    return
  }

  val json = credentialsFile.readText()
  val aep = Regex(""""accountEntropyPool"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
  val aci = Regex(""""aci"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)

  if (aep == null) {
    System.err.println("Error: 'accountEntropyPool' not found in credentials file. Make sure your credentials were exported with AEP support.")
    return
  }

  if (aci == null) {
    System.err.println("Error: 'aci' not found in credentials file.")
    return
  }

  val output = File(outputDir)
  LocalBackupGenerator.generate(aep, aci, output, messageCount)
}

private fun printHelp() {
  println("Usage:")
  println("  If no command is supplied, this will generate tests.")
  println("")
  println("Commands:")
  println("  generate-tests\t\tGenerates test cases")
  println("  print <path>\t\t\tPrints out a readable plaintext version of a plaintext, ungzipped backup file")
  println("  generate-local-backup\t\tGenerates an encrypted local backup for quickstart restore")
  println("")
  println("generate-local-backup options:")
  println("  <credentials-path>\t\tPath to quickstart credentials JSON (required)")
  println("  --credentials <path>\t\tAlternate way to specify credentials path")
  println("  --output <dir>\t\tOutput directory (default: local-backup-output)")
  println("  --messages <count>\t\tNumber of Note to Self messages (default: 500)")
  println("")
  println("Example:")
  println("  generate-local-backup /sdcard/signal-quickstart/prod_credentials.json --messages 100")
}

private fun init() {
  File(OUTPUT_DIR).mkdir()
  File(OUTPUT_DIR).listFiles { _, name -> name.endsWith(".txtproto") }?.forEach { it.delete() }
  File(OUTPUT_DIR).listFiles { _, name -> name.endsWith(".binproto") }?.forEach { it.delete() }
}

private fun runAllTests() {
  ALL_TEST_CASES.forEach { test ->
    runTest(
      testName = test.baseFileName,
      init = {
        test.initialize()
        with(test) {
          execute()
        }
      }
    )
  }
}

private fun runTest(testName: String, init: PermutationScope.() -> Unit) {
  println("Generating $testName...")
  val snapshots = permute { init() }

  // We keep a separate index because we don't want to increment it if the snapshot produces invalid output
  var index = 0

  snapshots.forEach { snapshot ->
    val binary = framesToBytes(snapshot)

    // Implicitly validates the backup. Throws exception on error.
    try {
      ComparableBackup.readUnencrypted(MessageBackup.Purpose.REMOTE_BACKUP, binary.inputStream(), binary.size.toLong())
    } catch (e: ValidationError) {
      throw RuntimeException("Failed to validate ${testName}_${index.minDigits(2)}!", e)
    }

    // For one-off tests with no permutations, it's nice to not have a trailing number
    val baseFileName = if (snapshots.size == 1) {
      testName
    } else {
      "${testName}_${index.minDigits(2)}"
    }

    File("$OUTPUT_DIR/$baseFileName.binproto").writeBytes(binary)

    val prettyPrinted = snapshot.joinToString("\n\n") { it.prettyPrint() }
    File("$OUTPUT_DIR/$baseFileName.txtproto").writeText(prettyPrinted.prefixWithWarningComment())

    index++
  }
}

private fun framesToBytes(frames: List<Message<*, *>>): ByteArray {
  val output = ByteArrayOutputStream()
  PlainTextBackupWriter(output).use { writer ->
    frames.forEach { writer.write(it) }
  }
  return output.toByteArray()
}

private fun String.prefixWithWarningComment(): String {
  return "// This file was auto-generated! It's only meant to show you what's in the .binproto. Do not edit!\n\n$this"
}

private fun Int.minDigits(minDigits: Int): String {
  return this.toString().padStart(minDigits, '0')
}

private class PlainTextBackupWriter(private val outputStream: OutputStream) : AutoCloseable {
  fun write(frame: Message<*, *>) {
    val frameBytes: ByteArray = frame.encode()

    outputStream.writeVarInt32(frameBytes.size)
    outputStream.write(frameBytes)
  }

  override fun close() {
    outputStream.close()
  }
}

private fun readAllFramesFromStream(inputStream: InputStream): List<Message<*, *>> {
  val frames = mutableListOf<Message<*, *>>()
  PlainTextBackupReader(inputStream).use { reader ->
    while (reader.hasNext()) {
      frames += reader.next()
    }
  }

  return frames
}

private class PlainTextBackupReader(private val inputStream: InputStream) : Iterator<Message<*, *>>, AutoCloseable {

  private var hasReadHeader = false
  private var next: Message<*, *>? = readNext()

  override fun hasNext(): Boolean {
    return next != null
  }

  override fun next(): Message<*, *> {
    return next!!.also { next = readNext() }
  }

  override fun close() {
    inputStream.close()
  }

  private fun readNext(): Message<*, *>? {
    val length = inputStream.readVarInt32()
    if (length <= 0) {
      return null
    }

    val frameBytes = inputStream.readNBytesOrThrow(length)

    return if (!hasReadHeader) {
      hasReadHeader = true
      BackupInfo.ADAPTER.decode(frameBytes)
    } else {
      return Frame.ADAPTER.decode(frameBytes)
    }
  }
}
