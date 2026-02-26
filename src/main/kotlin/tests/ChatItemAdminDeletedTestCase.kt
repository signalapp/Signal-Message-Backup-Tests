@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming admin deleted messages.
 */
object ChatItemAdminDeletedTestCase : TestCase("chat_item_admin_deleted") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol
    frames += StandardFrames.recipientGroupABC
    frames += StandardFrames.chatGroupABC

    val incomingGenerator = Generators.permutation<ChatItem.IncomingMessageDetails> {
      frames += ChatItem.IncomingMessageDetails(
        dateReceived = someIncrementingTimestamp(),
        dateServerSent = someIncrementingTimestamp(),
        read = someBoolean(),
        sealedSender = someBoolean()
      )
    }

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupABC.chat!!.id,
        authorId = some(Generators.list(StandardFrames.recipientAlice.recipient!!.id, StandardFrames.recipientBob.recipient!!.id, StandardFrames.recipientCarol.recipient!!.id)),
        dateSent = someIncrementingTimestamp(),
        incoming = some(incomingGenerator),
        adminDeletedMessage = AdminDeletedMessage(
          adminId = StandardFrames.recipientSelf.recipient!!.id
        )
      )
    )
  }
}
