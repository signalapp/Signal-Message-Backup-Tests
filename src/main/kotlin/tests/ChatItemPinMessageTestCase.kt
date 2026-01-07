package tests

import Generator
import Generators
import PermutationScope
import TestCase
import asList
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import toByteArray
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Incoming/outgoing pin messages.
 */
object ChatItemPinMessageTestCase : TestCase("chat_item_pin_message") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val pinnedAt = someIncrementingTimestamp()

    // If a pinNeverExpires is set, it is always true.
    val (pinExpiresGenerator, pinNeverExpires) = oneOf(
      Generators.incrementingTimestamps(lower = pinnedAt + 10000) as Generator<Any?>,
      Generators.list(true)
    )

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        standardMessage = StandardMessage(
          text = Text(body = someNonEmptyString()),
          attachments = Generators.permutation<MessageAttachment> {
            frames += MessageAttachment(
              pointer = some(Generators.bodyAttachmentFilePointer(includeIncrementalMac = false)),
              flag = MessageAttachment.Flag.NONE,
              wasDownloaded = someBoolean(),
              clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
            )
          }.asList(1, 3, 5).let { some(it) }
        ),
        pinDetails = ChatItem.PinDetails(
          pinnedAtTimestamp = pinnedAt,
          pinExpiresAtTimestamp = someOneOf(pinExpiresGenerator),
          pinNeverExpires = someOneOf(pinNeverExpires)
        )
      )
    )
  }
}
