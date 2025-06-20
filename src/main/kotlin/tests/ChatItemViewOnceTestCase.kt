@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import nullable
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.*
import toByteArray

/**
 * Incoming/outgoing view-once messages
 */
object ChatItemViewOnceTestCase : TestCase("chat_item_view_once") {

  private var incrementingDate = 0L

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    incrementingDate = someIncrementingTimestamp()

    val incomingGenerator = Generators.permutation<ChatItem.IncomingMessageDetails> {
      frames += ChatItem.IncomingMessageDetails(
        dateReceived = incrementingDate,
        dateServerSent = incrementingDate,
        read = someBoolean(),
        sealedSender = someBoolean()
      )
    }

    val outgoingGenerator = Generators.permutation<ChatItem.OutgoingMessageDetails> {
      frames += ChatItem.OutgoingMessageDetails(
        sendStatus = Generators.sendStatus(
          recipientIdGenerator = Generators.single(StandardFrames.recipientAlice.recipient!!.id)
        ).let { listOf(some(it)) }
      )
    }

    // Incoming
    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientAlice.recipient!!.id,
        dateSent = incrementingDate,
        incoming = some(incomingGenerator),
        viewOnceMessage = ViewOnceMessage(
          attachment = Generators.permutation<MessageAttachment> {
            frames += MessageAttachment(
              pointer = some(Generators.viewOnceFilePointer()),
              flag = MessageAttachment.Flag.NONE,
              wasDownloaded = someBoolean(),
              clientUuid = some(Generators.uuids().nullable())?.toByteArray()?.toByteString()
            )
          }.nullable().let { some(it) }
        )
      )
    )

    // Outgoing
    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        dateSent = incrementingDate + 1,
        outgoing = some(outgoingGenerator),
        viewOnceMessage = ViewOnceMessage(
          attachment = null
        )
      )
    )
  }
}
