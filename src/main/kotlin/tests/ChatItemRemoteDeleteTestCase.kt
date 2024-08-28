@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing remote deleted messages.
 */
object ChatItemRemoteDeleteTestCase : TestCase("chat_item_remote_delete") {
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
        remoteDeletedMessage = RemoteDeletedMessage()
      )
    )
  }
}
