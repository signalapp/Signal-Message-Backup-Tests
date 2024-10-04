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
  }
}
