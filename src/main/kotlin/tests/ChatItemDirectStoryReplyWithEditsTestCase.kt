@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import TestCase
import nullable
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing remote deleted messages.
 */
object ChatItemDirectStoryReplyWithEditsTestCase : TestCase("chat_item_direct_story_reply_with_edits") {

  // We always want to read the latest value inside a generator, so to prevent capturing
  // a snapshot, we keep this as member state
  private var originalMessageDateSent: Long = 0L

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (textReply, emoji) = oneOf(
      Generators.permutation {
        val text = some(Generators.textBody(minWords = 10, maxWords = 15))
        frames += DirectStoryReplyMessage.TextReply(
          text = Text(
            body = text,
            bodyRanges = some(Generators.bodyRanges(text))
          ),
          longText = some(Generators.longTextFilePointer().nullable())
        )
      },
      Generators.emoji()as Generator<Any?>
    )

    originalMessageDateSent = someNonZeroTimestamp()

    val revisionGenerator = Generators.lists(listOf(1, 3)) {
      Generators.permutation<ChatItem>(snapshotCount = 4) {
        val dateSent = someIncrementingTimestamp(
          lower = originalMessageDateSent - 100_000,
          upper = originalMessageDateSent - 10_000
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
          directStoryReplyMessage = DirectStoryReplyMessage(
            textReply = DirectStoryReplyMessage.TextReply(
              text = Text(
                body = some(Generators.textBody())
              )
            )
          )
        )
      }
    }

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
        revisions = some(revisionGenerator),
        directStoryReplyMessage = DirectStoryReplyMessage(
          textReply = someOneOf(textReply),
          emoji = someOneOf(emoji),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
