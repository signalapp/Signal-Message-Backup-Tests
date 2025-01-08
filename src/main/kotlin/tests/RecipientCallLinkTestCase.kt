@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import TestCase
import map
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.CallLink
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Recipient
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Reasonable permutations of [CallLink] recipients.
 */
object RecipientCallLinkTestCase : TestCase("recipient_call_link") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += Frame(
      recipient = Recipient(
        id = 10,
        callLink = CallLink(
          rootKey = someCallLinkRootKey().toByteString(),
          adminKey = someNullableBytes(32)?.toByteString(),
          name = someString(),
          restrictions = someEnum(CallLink.Restrictions::class.java),
          expirationMs = some(Generators.expirationTimersMs().map { Instant.ofEpochMilli(it).truncatedTo(ChronoUnit.DAYS).toEpochMilli() })
        )
      )
    )
  }
}
