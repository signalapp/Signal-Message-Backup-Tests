@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import TestCase
import asList
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing text-only messages with formatted text. Excludes mentions.
 */
object ChatItemStandardMessageFormattedTextTestCase : TestCase("chat_item_standard_message_formatted_text") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB
    frames += StandardFrames.chatGroupAB

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(
      StandardFrames.recipientAlice.recipient!!,
      StandardFrames.recipientBob.recipient!!
    )

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val (mentionGenerator, styleGenerator) = oneOf(
      Generators.list(
        StandardFrames.recipientAlice.recipient.contact!!.aci,
        StandardFrames.recipientBob.recipient.contact!!.aci
      ),
      Generators.enum(BodyRange.Style::class.java) as Generator<Any?>
    )

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient!!.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        standardMessage = StandardMessage(
          text = Text(
            body = "0123456789",
            bodyRanges = Generators.permutation<BodyRange> {
              frames += BodyRange(
                start = some(Generators.list(0, 0, 0, 1, 2, 3)),
                length = some(Generators.list(1, 5, 10, 5, 7, 3)),
                style = someOneOf(styleGenerator),
                mentionAci = someOneOf(mentionGenerator)
              )
            }.asList(0, 1, 2, 3, 4, 5).let { some(it) }
          )
        )
      )
    )
  }
}
