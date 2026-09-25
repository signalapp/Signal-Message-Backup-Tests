package tests

import Generator
import Generators
import PermutationScope
import StandardFrames
import TestCase
import asList
import nullable
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.MessageAttachment
import org.thoughtcrime.securesms.backup.v2.proto.StandardMessage
import toByteArray

/**
 * Incoming/outgoing messages with audio attachments, covering the audio-only
 * waveform and duration details. Includes both voice messages and plain audio
 * files, since either can carry those details.
 */
object ChatItemStandardMessageAudioAttachmentsTestCase : TestCase("chat_item_standard_message_audio_attachments") {

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
        standardMessage = StandardMessage(
          quote = null,
          // Merged rather than permuting the flag, so that every set of audio
          // details is paired with both kinds of audio attachment.
          attachments = Generators.merge(
            audioAttachments(MessageAttachment.Flag.VOICE_MESSAGE),
            audioAttachments(MessageAttachment.Flag.NONE)
          ).asList(1).let { some(it) },
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }

  /** Audio attachments with the given [flag], one for each set of audio details. */
  private fun audioAttachments(flag: MessageAttachment.Flag): Generator<MessageAttachment> {
    return Generators.permutation {
      frames += MessageAttachment(
        pointer = some(Generators.audioAttachmentFilePointer()),
        flag = flag,
        wasDownloaded = someBoolean(),
        clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
      )
    }
  }
}
