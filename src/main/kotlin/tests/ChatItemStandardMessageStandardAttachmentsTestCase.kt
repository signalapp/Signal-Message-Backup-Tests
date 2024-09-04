package tests

import PermutationScope
import TestCase
import asList
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing messages with standard attachments (i.e. no flags, meaning no voice notes, etc.).
 */
object ChatItemStandardMessageStandardAttachmentsTestCase : TestCase("chat_item_standard_message_standard_attachments") {

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
        standardMessage = StandardMessage(
          quote = null,
          text = someNullablePermutation {
            frames += Text(
              body = someString()
            )
          },
          attachments = Generators.permutation<MessageAttachment> {
            frames += MessageAttachment(
              pointer = some(Generators.filePointer()),
              flag = MessageAttachment.Flag.NONE,
              wasDownloaded = someBoolean()
            )
          }.asList(1, 3, 5).let { some(it) },
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}