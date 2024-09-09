package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.ChatUpdateMessage
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.SimpleChatUpdate

/**
 * All simple chat updates.
 */
object ChatItemSimpleUpdatesTestCase : TestCase("chat_item_simple_updates") {

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
          simpleUpdate = SimpleChatUpdate(
            type = someEnum(SimpleChatUpdate.Type::class.java, excluding = SimpleChatUpdate.Type.UNKNOWN)
          )
        )
      )
    )
  }
}
