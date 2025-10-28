@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing polls
 */
object ChatItemPollTestCase : TestCase("chat_item_poll") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB

    frames += StandardFrames.chatGroupAB

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        poll = Poll(
          question = someNonEmptyString(),
          allowMultiple = someBoolean(),
          hasEnded = someBoolean(),
          options = some(Generators.pollOption(StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient, StandardFrames.recipientBob.recipient!!)),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
