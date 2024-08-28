@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Permutations of the [ProfileChangeChatUpdate] message.
 */
object ChatItemProfileChangeUpdateTestCase : TestCase("chat_item_profile_change_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient!!.id,
        dateSent = someTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          profileChange = ProfileChangeChatUpdate(
            previousName = someNonEmptyString(),
            newName = someNonEmptyString()
          )
        )
      )
    )
  }
}
