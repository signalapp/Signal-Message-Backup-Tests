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
      stickerMessageGenerator,
      giftBadgeGenerator
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
          data_ = FilePointer(
            contentType = "image/webp",
            // If we update to have a sticker with a valid attachment, the quote
            // below should be updated to include a valid thumbnail attachment.
            invalidAttachmentLocator = FilePointer.InvalidAttachmentLocator()
          )
        )
      ).asGenerator(),
      GiftBadge(
        receiptCredentialPresentation = some(Generators.receiptCredentialPresentation()).serialize().toByteString(),
        state = GiftBadge.State.OPENED
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
        stickerMessage = someOneOf(stickerMessageGenerator),
        giftBadge = someOneOf(giftBadgeGenerator)
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
            body = some(Generators.textBody())
          ),
          quote = Quote(
            targetSentTimestamp = targetDateSent,
            authorId = StandardFrames.recipientAlice.recipient.id,
            text = if (targetMessage.chatItem!!.standardMessage != null) {
              Text(
                body = targetMessage.chatItem.standardMessage!!.text!!.body,
                bodyRanges = targetMessage.chatItem.standardMessage!!.text!!.bodyRanges
              )
            } else if (targetMessage.chatItem!!.contactMessage != null) {
              val name = targetMessage.chatItem.contactMessage!!.contact[0].name
              Text(
                body = "${name!!.givenName} ${name!!.familyName}"
              )
            } else {
              null
            },
            type = if (targetMessage.chatItem!!.giftBadge != null) {
              Quote.Type.GIFTBADGE
            } else {
              Quote.Type.NORMAL
            },
            attachments = if (targetMessage.chatItem!!.stickerMessage != null) {
              // The sole sticker message test case has an invalid attachment,
              // so we wouldn't have a proper thumbnail attachment here.
              listOf(
                Quote.QuotedAttachment(
                  contentType = "image/webp",
                  fileName = null
                )
              )
            } else {
              emptyList()
            }
          ),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
