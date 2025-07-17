package tests

import Generators
import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage
import org.thoughtcrime.securesms.backup.v2.proto.Text

/**
 * Incoming/outgoing messages with long text.
 */
object ChatItemStandardMessageLongTextTestCase : TestCase("chat_item_standard_message_long_text") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val shouldUseLongBody = someBoolean()
    val longTextBody = "Y" + "o".repeat(127 * 1024)
    val shortTextBody = some(Generators.textBody())

    val bodyText = if (shouldUseLongBody) longTextBody else shortTextBody
    val longTextPointerCandidate = some(Generators.longTextFilePointer())
    val longTextPointer = if (shouldUseLongBody) null else longTextPointerCandidate

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
        standardMessage = StandardMessage(
          text = Text(
            body = bodyText
          ),
          longText = longTextPointer,
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
