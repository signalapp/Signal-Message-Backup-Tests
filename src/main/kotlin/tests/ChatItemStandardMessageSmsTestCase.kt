package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing text-only messages.
 */
object ChatItemStandardMessageSmsTestCase : TestCase("chat_item_standard_message_sms") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

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
        sms = true,
        standardMessage = StandardMessage(
          text = Text(
            body = someNonEmptyString()
          ),
          attachments = Generators.lists(listOf(0, 1, 4)) {
            Generators.permutation<MessageAttachment> {
              frames += MessageAttachment(
                pointer = some(Generators.smsAttachmentFilePointer()),
                flag = MessageAttachment.Flag.NONE,
                wasDownloaded = someBoolean(),
                clientUuid = null
              )
            }
          }.let { some(it) }
        )
      )
    )
  }
}
