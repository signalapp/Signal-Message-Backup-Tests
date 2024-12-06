@file:Suppress("UNCHECKED_CAST")

import com.squareup.wire.Message
import org.signal.libsignal.messagebackup.ComparableBackup
import org.signal.libsignal.messagebackup.MessageBackup
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
  ChatItemStandardMessageFormattedTextTestCase,
  ChatItemStandardMessageSmsTestCase,
  ChatItemStandardMessageStandardAttachmentsTestCase,
  ChatItemStandardMessageSpecialAttachmentsTestCase,
  ChatItemStandardMessageLongTextTestCase,
  ChatItemStandardMessageWithEditsTestCase,
  ChatItemStandardMessageWithLinkPreviewTestCase,
  ChatItemStandardMessageWithQuoteTestCase,
  ChatItemStickerMessageTestCase,
  ChatItemRemoteDeleteTestCase,
  ChatItemViewOnceTestCase,
  ChatItemGroupChangeChatUpdateTestCase,
  ChatItemGroupChangeChatMultipleUpdateTestCase
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

private fun printHelp() {
  println("Usage:")
  println("  If no command is supplied, this will generate tests.")
  println("")
  println("Commands:")
  println("  generate-tests\t\tGenerates test cases")
  println("  print\t\tPrints out a readable plaintext version of a plaintext, ungzipped backup file")
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
    ComparableBackup.readUnencrypted(MessageBackup.Purpose.REMOTE_BACKUP, binary.inputStream(), binary.size.toLong())

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
