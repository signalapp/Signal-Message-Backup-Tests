@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import TestCase
import nullable
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import plus

/**
 * Incoming/outgoing remote deleted messages.
 */
object ChatItemDirectStoryReplyTestCase : TestCase("chat_item_direct_story_reply") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

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

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient!!.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        directStoryReplyMessage = DirectStoryReplyMessage(
          textReply = someOneOf(textReply),
          emoji = someOneOf(emoji),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
