@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Individual call updates
 */
object ChatItemIndividualCallUpdateTestCase : TestCase("chat_item_individual_call_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val callState = someEnum(IndividualCall.State::class.java, excluding = IndividualCall.State.UNKNOWN_STATE)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        directionless = ChatItem.DirectionlessMessageDetails(),
        dateSent = someIncrementingTimestamp(),
        updateMessage = ChatUpdateMessage(
          individualCall = IndividualCall(
            callId = somePositiveLong(),
            type = someEnum(IndividualCall.Type::class.java, excluding = IndividualCall.Type.UNKNOWN_TYPE),
            direction = when (callState) {
              // We can only have "missed notification profile" for incoming calls...
              IndividualCall.State.MISSED_NOTIFICATION_PROFILE -> IndividualCall.Direction.INCOMING
              // ...so use the other statuses to cover incoming and outgoing.
              IndividualCall.State.ACCEPTED,
              IndividualCall.State.MISSED -> IndividualCall.Direction.INCOMING
              IndividualCall.State.NOT_ACCEPTED -> IndividualCall.Direction.OUTGOING
              IndividualCall.State.UNKNOWN_STATE -> throw NotImplementedError()
            },
            state = callState,
            startedCallTimestamp = someNonZeroTimestamp(),
            // Only missed call states can potentially be unread.
            read = when (callState) {
              IndividualCall.State.MISSED -> false
              IndividualCall.State.MISSED_NOTIFICATION_PROFILE -> true
              IndividualCall.State.ACCEPTED,
              IndividualCall.State.NOT_ACCEPTED -> true
              IndividualCall.State.UNKNOWN_STATE -> throw NotImplementedError()
            }
          )
        )
      )
    )
  }
}
