@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Individual call updates
 */
object ChatItemGroupCallUpdateTestCase : TestCase("chat_item_group_call_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB
    frames += StandardFrames.chatGroupAB

    val startedCallGenerator = Generators.list(
      StandardFrames.recipientSelf.recipient!!.id,
      StandardFrames.recipientAlice.recipient!!.id,
      StandardFrames.recipientBob.recipient!!.id
    )

    val callState = someEnum(GroupCall.State::class.java, excluding = GroupCall.State.UNKNOWN_STATE)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        directionless = ChatItem.DirectionlessMessageDetails(),
        dateSent = someIncrementingTimestamp(),
        updateMessage = ChatUpdateMessage(
          groupCall = GroupCall(
            callId = somePositiveLong(),
            state = callState,
            ringerRecipientId = when (callState) {
              // Only incoming ringing call states potentially have a ringer...
              GroupCall.State.RINGING,
              GroupCall.State.ACCEPTED,
              GroupCall.State.DECLINED,
              GroupCall.State.MISSED -> StandardFrames.recipientAlice.recipient!!.id
              // ...but we want to cover a null ringer for one ringing state.
              GroupCall.State.MISSED_NOTIFICATION_PROFILE -> null
              GroupCall.State.GENERIC,
              GroupCall.State.JOINED,
              GroupCall.State.OUTGOING_RING -> null
              GroupCall.State.UNKNOWN_STATE -> throw NotImplementedError()
            },
            startedCallRecipientId = some(startedCallGenerator),
            startedCallTimestamp = someNonZeroTimestamp(),
            endedCallTimestamp = when (callState) {
              GroupCall.State.DECLINED,
              GroupCall.State.MISSED,
              GroupCall.State.MISSED_NOTIFICATION_PROFILE -> 100L
              GroupCall.State.GENERIC,
              GroupCall.State.JOINED,
              GroupCall.State.OUTGOING_RING,
              GroupCall.State.RINGING,
              GroupCall.State.ACCEPTED -> null
              GroupCall.State.UNKNOWN_STATE -> throw NotImplementedError()
            },
            read = when (callState) {
              // Only missed call states can potentially be unread...
              GroupCall.State.MISSED -> false
              // ...but we want to cover "read" missed calls too.
              GroupCall.State.MISSED_NOTIFICATION_PROFILE -> true
              GroupCall.State.DECLINED,
              GroupCall.State.GENERIC,
              GroupCall.State.JOINED,
              GroupCall.State.OUTGOING_RING,
              GroupCall.State.RINGING,
              GroupCall.State.ACCEPTED -> true
              GroupCall.State.UNKNOWN_STATE -> throw NotImplementedError()
            }
          )
        )
      )
    )
  }
}
