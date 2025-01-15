@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
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

    fun makeChatItemFrame(text: String, bodyRanges: List<BodyRange>): Frame {
      return Frame(
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
              body = text,
              bodyRanges = bodyRanges
            )
          )
        )
      )
    }

    val bodyText = "0123456789"

    frames += makeChatItemFrame(
      text = bodyText,
      bodyRanges = Generators.bodyRanges(
        inputText = bodyText,
        mentionAcis = listOf(
          StandardFrames.recipientAlice.recipient.contact!!.aci!!,
          StandardFrames.recipientBob.recipient.contact!!.aci!!
        )
      ).let { some(it) }
    )

    // Add a special frame where the style and the mention are both the full
    // length of the body. We can't cover this case with the ranges in the frame
    // above, since style/mention ranges can't ever partially overlap; if there
    // were a style/mention range with a full-length range in the frame above,
    // it'd potentially overlap with another style/mention range in the same
    // frame.
    frames += makeChatItemFrame(
      text = bodyText,
      bodyRanges = listOf(
        BodyRange(
          start = 0,
          length = 10,
          style = someEnum(BodyRange.Style::class.java, excluding = BodyRange.Style.NONE)
        ),
        BodyRange(
          start = 0,
          length = 10,
          mentionAci = StandardFrames.recipientAlice.recipient.contact!!.aci
        )
      )
    )
  }
}
