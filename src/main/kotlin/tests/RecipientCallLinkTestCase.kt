@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.CallLink
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Recipient

/**
 * Reasonable permutations of [CallLink] recipients.
 */
object RecipientCallLinkTestCase : TestCase("recipient_call_link") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += Frame(
      recipient = Recipient(
        callLink = CallLink(
          rootKey = someBytes(16).toByteString(),
          adminKey = someNullableBytes(32)?.toByteString(),
          name = someString(),
          restrictions = someEnum(CallLink.Restrictions::class.java, excluding = CallLink.Restrictions.UNKNOWN),
          expirationMs = somePositiveLong()
        )
      )
    )
  }
}
