@file:Suppress("UNCHECKED_CAST")

import com.squareup.wire.Message
import org.signal.libsignal.messagebackup.ComparableBackup
import org.signal.libsignal.messagebackup.MessageBackup
import tests.*
import java.io.ByteArrayOutputStream
import java.io.File
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
  ChatItemStandardMessageStandardAttachmentsTestCase,
  ChatItemStandardMessageSpecialAttachmentsTestCase,
  ChatItemStandardMessageLongTextTestCase,
  ChatItemStandardMessageWithEditsTestCase,
  ChatItemStandardMessageWithQuoteTestCase,
  ChatItemStickerMessageTestCase,
  ChatItemRemoteDeleteTestCase
)

fun main() {
  val startTime = System.currentTimeMillis()

  println("Initializing...")
  init()
  runAllTests()
  println("Complete! Took ${System.currentTimeMillis() - startTime} ms.")
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

    val json = snapshot.joinToString("\n\n") { it.prettyPrint() }
    File("$OUTPUT_DIR/$baseFileName.txtproto").writeText(json.prefixWithWarningComment())

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
