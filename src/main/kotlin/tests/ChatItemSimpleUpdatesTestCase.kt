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

    // Generate timestamps for as many frames as we might want to add depending
    // on simpleChatUpdate. (We need to register the max number of generators
    // we might eventually use, up-front.)
    val dateSentTimestamps = listOf(someNonZeroTimestamp(), someNonZeroTimestamp())

    val simpleChatUpdate = someEnum(SimpleChatUpdate.Type::class.java, excluding = SimpleChatUpdate.Type.UNKNOWN)

    val selfRecipientId = StandardFrames.recipientSelf.recipient!!.id
    val aliceRecipientId = StandardFrames.recipientAlice.recipient!!.id
    val releaseNotesRecipientId = StandardFrames.recipientReleaseNotes.recipient!!.id

    // Some simple chat updates can originate from the local user or a remote
    // user, while some are necessarily from one or the other.
    val authorIds: List<Long> = when (simpleChatUpdate) {
      // ...and release note donation requests should come from the release
      // notes recipient.
      SimpleChatUpdate.Type.RELEASE_CHANNEL_DONATION_REQUEST -> listOf(releaseNotesRecipientId)
      SimpleChatUpdate.Type.IDENTITY_UPDATE,
      SimpleChatUpdate.Type.IDENTITY_VERIFIED,
      SimpleChatUpdate.Type.IDENTITY_DEFAULT,
      SimpleChatUpdate.Type.JOINED_SIGNAL,
      SimpleChatUpdate.Type.CHANGE_NUMBER -> listOf(aliceRecipientId)
      SimpleChatUpdate.Type.CHAT_SESSION_REFRESH,
      SimpleChatUpdate.Type.REPORTED_SPAM,
      SimpleChatUpdate.Type.BLOCKED,
      SimpleChatUpdate.Type.UNBLOCKED,
      SimpleChatUpdate.Type.MESSAGE_REQUEST_ACCEPTED -> listOf(selfRecipientId)
      SimpleChatUpdate.Type.END_SESSION,
      SimpleChatUpdate.Type.BAD_DECRYPT,
      SimpleChatUpdate.Type.PAYMENTS_ACTIVATED,
      SimpleChatUpdate.Type.PAYMENT_ACTIVATION_REQUEST,
      SimpleChatUpdate.Type.UNSUPPORTED_PROTOCOL_MESSAGE -> listOf(selfRecipientId, aliceRecipientId)
      // Impossible, checked above.
      SimpleChatUpdate.Type.UNKNOWN -> listOf()
    }

    for ((idx, authorId) in authorIds.withIndex()) {
      frames += Frame(
        chatItem = ChatItem(
          chatId = StandardFrames.chatAlice.chat!!.id,
          authorId = authorId,
          dateSent = dateSentTimestamps[idx],
          directionless = ChatItem.DirectionlessMessageDetails(),
          updateMessage = ChatUpdateMessage(
            simpleUpdate = SimpleChatUpdate(type = simpleChatUpdate)
          )
        )
      )
    }
  }
}
