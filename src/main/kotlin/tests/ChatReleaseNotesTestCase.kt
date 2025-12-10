package tests

import Generators
import PermutationScope
import TestCase
import nullable
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.ChatUpdateMessage
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.MessageAttachment
import org.thoughtcrime.securesms.backup.v2.proto.SimpleChatUpdate
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage
import org.thoughtcrime.securesms.backup.v2.proto.Text
import toByteArray

/**
 * Incoming messages in the release notes chat.
 */
object ChatReleaseNotesTestCase : TestCase("chat_release_notes") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.chatReleaseNotes

    val releaseNotesChatId = StandardFrames.chatReleaseNotes.chat!!.id
    val releaseNotesRecipientId = StandardFrames.recipientReleaseNotes.recipient!!.id

    // Standard incoming message
    frames += Frame(
      chatItem = ChatItem(
        chatId = releaseNotesChatId,
        authorId = releaseNotesRecipientId,
        dateSent = someIncrementingTimestamp(),
        incoming = ChatItem.IncomingMessageDetails(
          dateReceived = someIncrementingTimestamp(),
          dateServerSent = someIncrementingTimestamp(),
          read = someBoolean(),
          sealedSender = someBoolean()
        ),
        standardMessage = StandardMessage(
          text = Text(
            body = some(Generators.textBody())
          ),
          attachments = some(
            Generators.permutation<MessageAttachment> {
              frames += MessageAttachment(
                pointer = some(Generators.bodyAttachmentFilePointer(includeIncrementalMac = false)),
                flag = MessageAttachment.Flag.NONE,
                wasDownloaded = someBoolean(),
                clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
              )
            }
          ).let { listOf(it) }
        )
      )
    )

    // Release channel donation request
    frames += Frame(
      chatItem = ChatItem(
        chatId = releaseNotesChatId,
        authorId = releaseNotesRecipientId,
        dateSent = someIncrementingTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          simpleUpdate = SimpleChatUpdate(type = SimpleChatUpdate.Type.RELEASE_CHANNEL_DONATION_REQUEST)
        )
      )
    )
  }
}
