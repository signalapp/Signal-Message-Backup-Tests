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
            direction = someEnum(IndividualCall.Direction::class.java, excluding = IndividualCall.Direction.UNKNOWN_DIRECTION),
            state = someEnum(IndividualCall.State::class.java, excluding = IndividualCall.State.UNKNOWN_STATE),
            startedCallTimestamp = someNonZeroTimestamp(),
            read = someBoolean()
          )
        )
      )
    )
  }
}
