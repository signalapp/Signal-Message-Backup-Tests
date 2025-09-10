package tests

import Generators
import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage
import org.thoughtcrime.securesms.backup.v2.proto.Text

/**
 * Incoming/outgoing text-only messages.
 */
object ChatItemStandardMessageGroupTextOnlyTestCase : TestCase("chat_item_standard_message_group_text_only") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol
    frames += StandardFrames.recipientGroupABC
    frames += StandardFrames.chatGroupABC

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(
      shuffledStatuses = true,
      StandardFrames.recipientAlice.recipient!!,
      StandardFrames.recipientBob.recipient!!,
      StandardFrames.recipientCarol.recipient!!
    )

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupABC.chat!!.id,
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
            body = some(Generators.textBody())
          )
        )
      )
    )
  }
}
