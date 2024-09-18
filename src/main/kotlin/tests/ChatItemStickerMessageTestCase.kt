@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Sticker
import org.thoughtcrime.securesms.backup.v2.proto.StickerMessage

/**
 * Incoming/outgoing sticker messages.
 */
object ChatItemStickerMessageTestCase : TestCase("chat_item_sticker_message") {
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
        stickerMessage = StickerMessage(
          sticker = Sticker(
            packId = someBytes(16).toByteString(),
            packKey = someBytes(32).toByteString(),
            stickerId = someInt(1, 32),
            emoji = someEmoji(),
            data_ = some(Generators.stickerFilePointer())
          ),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient!!))
        )
      )
    )
  }
}
