@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage
import org.thoughtcrime.securesms.backup.v2.proto.Text

/**
 * Permutations of a standard message with edits/revisions.
 */
object ChatItemStandardMessageWithEditsTestCase : TestCase("chat_item_standard_message_with_edits") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val originalMessageDateSent = someNonZeroTimestamp()

    val revisionGenerator = Generators.lists(listOf(1, 3)) {
      Generators.permutation<ChatItem>(snapshotCount = 4) {
        val dateSent = someDecrementingTimestamp(
          lower = originalMessageDateSent - 100_000,
          upper = originalMessageDateSent - 1
        )

        frames += ChatItem(
          chatId = StandardFrames.chatAlice.chat!!.id,
          authorId = StandardFrames.recipientAlice.recipient!!.id,
          dateSent = dateSent,
          incoming = ChatItem.IncomingMessageDetails(
            dateReceived = dateSent - 10,
            dateServerSent = dateSent - 1,
            read = someBoolean(),
            sealedSender = someBoolean()
          ),
          standardMessage = StandardMessage(
            text = Text(
              body = some(Generators.textBody())
            )
          )
        )
      }
    }

    val revisions = some(revisionGenerator)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient!!.id,
        dateSent = originalMessageDateSent,
        incoming = ChatItem.IncomingMessageDetails(
          dateReceived = originalMessageDateSent - 10,
          dateServerSent = originalMessageDateSent - 1,
          read = someBoolean(),
          sealedSender = someBoolean()
        ),
        standardMessage = StandardMessage(
          text = Text(
            body = some(Generators.textBody())
          )
        ),
        revisions = revisions
      )
    )
  }
}
