@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import asGenerator
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing messages that quote other messages. We try to cover quoting every type of message here.
 */
object ChatItemStandardMessageWithQuoteTestCase : TestCase("chat_item_standard_message_with_quote") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    // TODO more message types!

    val (
      standardMessageGenerator,
      contactMessageGenerator,
      stickerMessageGenerator
    ) = oneOf(
      StandardMessage(
        text = Text(body = "asdf")
      ).asGenerator(),
      ContactMessage(
        contact = listOf(ContactAttachment(name = ContactAttachment.Name(givenName = "Peter", familyName = "Parker")))
      ).asGenerator(),
      StickerMessage(
        sticker = Sticker(
          packId = ByteArray(16) { 0 }.toByteString(),
          packKey = ByteArray(32) { 1 }.toByteString(),
          emoji = "👍",
          data_ = FilePointer(invalidAttachmentLocator = FilePointer.InvalidAttachmentLocator())
        )
      ).asGenerator()
    )

    val targetDateSent = 1L

    val targetMessage = Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient.id,
        dateSent = targetDateSent,
        incoming = ChatItem.IncomingMessageDetails(
          dateReceived = targetDateSent,
          dateServerSent = targetDateSent,
          read = true
        ),
        standardMessage = someOneOf(standardMessageGenerator),
        contactMessage = someOneOf(contactMessageGenerator),
        stickerMessage = someOneOf(stickerMessageGenerator)
      )
    )

    frames += targetMessage

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
            body = someString()
          ),
          quote = Quote(
            targetSentTimestamp = targetDateSent,
            authorId = StandardFrames.recipientAlice.recipient.id,
            text = if (targetMessage.chatItem?.standardMessage?.text?.body != null) {
              Text(
                body = targetMessage.chatItem.standardMessage.text.body,
                bodyRanges = if (targetMessage.chatItem?.standardMessage?.text?.bodyRanges != null) {
                  targetMessage.chatItem.standardMessage.text.bodyRanges
                } else {
                  emptyList()
                }
              )
            } else if (targetMessage.chatItem?.contactMessage != null) {
              var name = targetMessage.chatItem.contactMessage.contact[0].name
              Text(
                body = "${name!!.givenName} ${name!!.familyName}"
              )
            } else {
              null
            },
            type = Quote.Type.NORMAL
          ),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
