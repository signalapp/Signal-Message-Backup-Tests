@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import TestCase
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing payment update messages.
 */
object ChatItemPaymentNotificationTestCase : TestCase("chat_item_payment_notification") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val (transactionGenerator, failedTransactionGenerator) = oneOf(
      Generators.permutation {
        val publicKey = listOf(some(Generators.bytes(32)).toByteString())
        val keyImages = listOf(some(Generators.bytes(32)).toByteString())

        frames += PaymentNotification.TransactionDetails.Transaction(
          status = someEnum(PaymentNotification.TransactionDetails.Transaction.Status::class.java),
          mobileCoinIdentification = PaymentNotification.TransactionDetails.MobileCoinTxoIdentification(
            publicKey = publicKey,
            keyImages = keyImages
          ),
          timestamp = someIncrementingTimestamp(),
          blockIndex = somePositiveLong(),
          blockTimestamp = someTimestamp(),
          transaction = someBytes(32).toByteString(),
          receipt = some(Generators.mobileCoinReceipts()).toByteString()
        )
      },
      Generators.permutation {
        frames += PaymentNotification.TransactionDetails.FailedTransaction(
          reason = someEnum(PaymentNotification.TransactionDetails.FailedTransaction.FailureReason::class.java)
        )
      }
    )

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
        paymentNotification = PaymentNotification(
          amountMob = some(Generators.picoMobs()),
          feeMob = some(Generators.picoMobs()),
          note = some(Generators.textBody().nullable()),
          transactionDetails = PaymentNotification.TransactionDetails(
            transaction = someOneOf<PaymentNotification.TransactionDetails.Transaction?>(transactionGenerator)?.let { transaction ->
              // We only want to take one of publickey/keyImages depending on if the message is incoming or outgoing.
              // Because this generator is populated immediately, it doesn't have up-to-date info on whether incoming/outgoing is present.
              // Therefore, we need to map it on-the-fly to account for the current value of incoming/outgoing.
              transaction.copy(
                mobileCoinIdentification = transaction.mobileCoinIdentification!!.copy(
                  publicKey = transaction.mobileCoinIdentification.publicKey.takeIf { incoming != null } ?: emptyList(),
                  keyImages = transaction.mobileCoinIdentification.keyImages.takeIf { outgoing != null } ?: emptyList()
                )
              )
            },
            failedTransaction = someOneOf(failedTransactionGenerator)
          )
        )
      )
    )
  }
}
