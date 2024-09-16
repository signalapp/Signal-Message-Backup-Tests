@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import StandardFrames
import TestCase
import asList
import okio.ByteString
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

    fun makeChatItemFrame(bodyRanges: List<BodyRange>): Frame {
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
              body = "0123456789",
              bodyRanges = bodyRanges
            )
          )
        )
      )
    }

    fun makeStyleGenerator(): Generator<BodyRange.Style> {
      return Generators.enum(BodyRange.Style::class.java, excluding = BodyRange.Style.NONE)
    }

    val (mentionGenerator, styleGenerator) = oneOf(
      Generators.list(
        StandardFrames.recipientAlice.recipient.contact!!.aci,
        StandardFrames.recipientBob.recipient.contact!!.aci
      ),
      makeStyleGenerator() as Generator<Any?>
    )

    // Mentions and styles can occupy identical or disjoint ranges, but cannot
    // partially overlap. (I.e., styles cannot be applied to part-but-not-all
    // of a mention.)
    val (mentionRangeGenerator, styleRangeGenerator) = listOf(
      Generators.list(
        Pair(6, 2),
        Pair(8, 2)
      ),
      // Includes intentional doubles, so we get multiple styles with the same
      // exact ranges.
      Generators.list(
        Pair(0, 1),
        Pair(0, 4),
        Pair(0, 4),
        Pair(1, 5),
        Pair(2, 2),
        Pair(6, 2),
        Pair(6, 2)
      )
    )

    frames += makeChatItemFrame(
      bodyRanges = Generators.permutation<BodyRange> {
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
      }.asList(0, 1, 2, 3, 4, 5).let { some(it) }
    )

    // Add a special frame where the style and the mention are both the full
    // length of the body. We can't cover this case with the ranges in the frame
    // above, since style/mention ranges can't ever partially overlap; if there
    // were a style/mention range with a full-length range in the frame above,
    // it'd potentially overlap with another style/mention range in the same
    // frame.
    frames += makeChatItemFrame(
      bodyRanges = listOf(
        BodyRange(
          start = 0,
          length = 10,
          style = some(makeStyleGenerator())
        ),
        BodyRange(
          start = 0,
          length = 10,
          mentionAci = StandardFrames.recipientAlice.recipient.contact.aci
        )
      )
    )
  }
}
