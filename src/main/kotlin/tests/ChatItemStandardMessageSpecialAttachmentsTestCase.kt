package tests

import PermutationScope
import TestCase
import asList
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.MessageAttachment
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage

/**
 * Incoming/outgoing messages with special attachments (i.e. voice note, borderless).
 */
object ChatItemStandardMessageSpecialAttachmentsTestCase : TestCase("chat_item_standard_message_special_attachments") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val voiceMessageFilePointer = some(Generators.voiceMessageFilePointer())
    val gifFilePointer = some(Generators.gifFilePointer())
    val borderlessFilePointer = some(Generators.borderlessFilePointer())

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
          quote = null,
          attachments = Generators.permutation<MessageAttachment> {
            val flag = someEnum(MessageAttachment.Flag::class.java, excluding = MessageAttachment.Flag.NONE)

            frames += MessageAttachment(
              flag = flag,
              pointer = when (flag) {
                MessageAttachment.Flag.VOICE_MESSAGE -> voiceMessageFilePointer
                MessageAttachment.Flag.GIF -> gifFilePointer
                MessageAttachment.Flag.BORDERLESS -> borderlessFilePointer
                // Excluded above.
                MessageAttachment.Flag.NONE -> throw NotImplementedError()
              },
              wasDownloaded = someBoolean()
            )
          }.asList(1).let { some(it) },
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
