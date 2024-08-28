@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.AdHocCall
import org.thoughtcrime.securesms.backup.v2.proto.Frame

/**
 * Reasonable permutations of [AdHocCall] objects.
 */
object AdHocCallTestCase : TestCase("ad_hoc_call") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientCallLink

    frames += Frame(
      adHocCall = AdHocCall(
        callId = some(Generators.nonZeroLongs(1, Long.MAX_VALUE)),
        recipientId = StandardFrames.recipientCallLink.recipient!!.id,
        state = someEnum(AdHocCall.State::class.java, excluding = AdHocCall.State.UNKNOWN_STATE),
        callTimestamp = someTimestamp()
      )
    )
  }
}
