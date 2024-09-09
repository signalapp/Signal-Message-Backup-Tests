@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

object ChatItemThreadMergeUpdateTestCase : TestCase("chat_item_thread_merge_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient!!.id,
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          threadMerge = ThreadMergeChatUpdate(
            previousE164 = someE164()
          )
        )
      )
    )
  }
}
