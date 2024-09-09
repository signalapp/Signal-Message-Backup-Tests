@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Permutations of [ExpirationTimerChatUpdate] messages.
 */
object ChatItemExpirationTimerUpdateTestCase : TestCase("chat_item_expiration_timer_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = some(Generators.list(StandardFrames.recipientSelf.recipient!!.id, StandardFrames.recipientAlice.recipient!!.id)),
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          expirationTimerChange = ExpirationTimerChatUpdate(
            expiresInMs = someExpirationTimerMs()
          )
        )
      )
    )
  }
}
