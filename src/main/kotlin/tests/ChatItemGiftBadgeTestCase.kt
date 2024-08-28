@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing gift badges
 */
object ChatItemGiftBadgeTestCase : TestCase("chat_item_gift_badge") {
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
        giftBadge = GiftBadge(
          receiptCredentialPresentation = someBytes(32).toByteString(),
          state = someEnum(GiftBadge.State::class.java)
        )
      )
    )
  }
}
