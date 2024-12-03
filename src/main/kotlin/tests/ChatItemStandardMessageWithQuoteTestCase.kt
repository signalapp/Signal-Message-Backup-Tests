@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import StandardFrames
import TestCase
import asGenerator
import asList
import map
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import toByteArray

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

    val (
      standardMessageGenerator,
      contactMessageGenerator,
      stickerMessageGenerator,
      giftBadgeGenerator,
      viewOnceGenerator
    ) = oneOf(
      standardMessageGenerator(),
      ContactMessage(
        contact = listOf(ContactAttachment(name = ContactAttachment.Name(givenName = "Peter", familyName = "Parker")))
      ).asGenerator(),
      Generators.permutation {
        frames += StickerMessage(
          sticker = Sticker(
            packId = someBytes(16).toByteString(),
            packKey = someBytes(32).toByteString(),
            emoji = someEmoji(),
            data_ = some(Generators.stickerFilePointer())
          )
        )
      },
      GiftBadge(
        receiptCredentialPresentation = some(Generators.receiptCredentialPresentation()).serialize().toByteString(),
        state = GiftBadge.State.OPENED
      ).asGenerator(),
      ViewOnceMessage().asGenerator()
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
        giftBadge = someOneOf(giftBadgeGenerator),
        viewOnceMessage = someOneOf(viewOnceGenerator)
      )
    )

    frames += targetMessage

    checkNotNull(targetMessage.chatItem)

    val quoteThumbnail: MessageAttachment = some(
      Generators.quoteFilePointer().map { pointer ->
        MessageAttachment(
          pointer = pointer,
          flag = MessageAttachment.Flag.NONE,
          wasDownloaded = someBoolean()
        )
      }
    )

    val targetHasMedia = (
      targetMessage.chatItem.stickerMessage?.sticker?.data_?.contentType
        ?: targetMessage.chatItem.standardMessage?.attachments?.firstOrNull()?.pointer?.contentType
      ) != null

    val quoteAttachment: Quote.QuotedAttachment? = if (targetHasMedia) {
      Quote.QuotedAttachment(
        contentType = "image/jpeg",
        fileName = quoteThumbnail.pointer?.fileName,
        thumbnail = quoteThumbnail
      )
    } else {
      null
    }

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
            text = targetMessage.chatItem.getQuoteText(),
            type = when {
              targetMessage.chatItem.giftBadge != null -> Quote.Type.GIFT_BADGE
              targetMessage.chatItem.viewOnceMessage != null -> Quote.Type.VIEW_ONCE
              else -> Quote.Type.NORMAL
            },
            attachments = if (quoteAttachment != null) {
              listOf(quoteAttachment)
            } else {
              listOf()
            }
          ),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }

  private fun standardMessageGenerator(): Generator<Any?> {
    return Generators.permutation {
      val standardMessageGenerator = Generators.permutation<MessageAttachment> {
        frames += MessageAttachment(
          pointer = some(Generators.bodyAttachmentFilePointer()),
          flag = MessageAttachment.Flag.NONE,
          wasDownloaded = someBoolean(),
          clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
        )
      }.asList(0, 1, 3)

      val voiceNoteAttachmentGenerator = Generators.permutation<MessageAttachment> {
        frames += MessageAttachment(
          pointer = some(Generators.voiceMessageFilePointer()),
          flag = MessageAttachment.Flag.VOICE_MESSAGE,
          wasDownloaded = someBoolean(),
          clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
        )
      }.asList(1)

      val attachmentGenerator: Generator<List<MessageAttachment>> = Generators.merge(standardMessageGenerator, voiceNoteAttachmentGenerator)

      val attachments = some(attachmentGenerator)

      frames += StandardMessage(
        text = if (attachments.firstOrNull()?.flag == MessageAttachment.Flag.VOICE_MESSAGE) {
          null
        } else {
          Text(body = "asdf")
        },
        attachments = attachments
      )
    }
  }

  private fun ChatItem.getQuoteText(): Text? {
    return when {
      this.standardMessage?.text != null -> {
        Text(
          body = this.standardMessage.text.body ?: "",
          bodyRanges = this.standardMessage.text.bodyRanges ?: emptyList()
        )
      }
      this.contactMessage != null -> {
        val name = this.contactMessage.contact[0].name!!
        Text(
          body = "${name.givenName} ${name.familyName}"
        )
      }
      else -> null
    }
  }
}
