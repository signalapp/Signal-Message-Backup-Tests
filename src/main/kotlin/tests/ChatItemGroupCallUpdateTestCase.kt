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
  private val ENDED_STATES = setOf(
    GroupCall.State.DECLINED,
    GroupCall.State.MISSED,
    GroupCall.State.MISSED_NOTIFICATION_PROFILE
  )

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB
    frames += StandardFrames.chatGroupAB

    val ringerGenerator = Generators.list(
      StandardFrames.recipientAlice.recipient!!.id,
      StandardFrames.recipientBob.recipient!!.id
    )

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
            ringerRecipientId = some(ringerGenerator),
            startedCallRecipientId = some(startedCallGenerator),
            startedCallTimestamp = someNonZeroTimestamp(),
            endedCallTimestamp = if (callState in ENDED_STATES) {
              100L
            } else {
              0L
            },
            read = someBoolean()
          )
        )
      )
    )
  }
}
