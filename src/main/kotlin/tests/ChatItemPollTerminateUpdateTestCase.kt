@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.ChatUpdateMessage
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.PollTerminateUpdate

/**
 * Permutations of the [PollTerminateUpdate] message.
 */
object ChatItemPollTerminateUpdateTestCase : TestCase("chat_item_poll_terminate_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB

    frames += StandardFrames.chatGroupAB

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient!!.id,
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          pollTerminate = PollTerminateUpdate(
            targetSentTimestamp = someNonZeroTimestamp(),
            question = someNonEmptyString()
          )
        )
      )
    )
  }
}
