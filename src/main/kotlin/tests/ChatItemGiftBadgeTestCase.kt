@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import SeededRandom
import StandardFrames
import TestCase
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.signal.libsignal.zkgroup.ServerSecretParams
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation
import org.signal.libsignal.zkgroup.receipts.ReceiptSerial
import org.signal.libsignal.zkgroup.receipts.ServerZkReceiptOperations
import org.thoughtcrime.securesms.backup.v2.proto.*
import java.security.SecureRandom

/**
 * Incoming/outgoing gift badges
 */
object ChatItemGiftBadgeTestCase : TestCase("chat_item_gift_badge") {
  private val validPresentation: ReceiptCredentialPresentation = run {
    // libsignal requires a SecureRandom for zkgroup operations
    val derivedRandom = SecureRandom.getInstance("SHA1PRNG")
    derivedRandom.setSeed(SeededRandom.long())
    val serverParams = ServerSecretParams.generate(derivedRandom)
    val clientOps = ClientZkReceiptOperations(serverParams.publicParams)

    val serial = ReceiptSerial(SeededRandom.bytes(ReceiptSerial.SIZE))
    val context = clientOps.createReceiptCredentialRequestContext(derivedRandom, serial)
    val response = ServerZkReceiptOperations(serverParams).issueReceiptCredential(derivedRandom, context.request, 0, 1)
    val credential = clientOps.receiveReceiptCredential(context, response)
    val presentation = clientOps.createReceiptCredentialPresentation(derivedRandom, credential)

    presentation
  }

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val state = someEnum(GiftBadge.State::class.java)

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
        giftBadge = GiftBadge(
          state = state,
          receiptCredentialPresentation = if (state == GiftBadge.State.FAILED) {
            ByteString.EMPTY
          } else {
            validPresentation.serialize().toByteString()
          }
        )
      )
    )
  }
}
