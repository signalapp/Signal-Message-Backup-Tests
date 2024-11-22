package tests

import Generators
import PermutationScope
import TestCase
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.Contact
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Recipient
import toByteArray

/**
 * Every reasonable permutation of Recipient.Contact
 */
object RecipientContactsTestCase : TestCase("recipient_contacts") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    val (registered, notRegistered) = oneOf(
      Generators.list(Contact.Registered()),
      Generators.permutation {
        frames += Contact.NotRegistered(
          unregisteredTimestamp = someTimestamp()
        )
      }
    )

    val identityKey: ByteArray? = some(Generators.identityKeys().nullable())

    frames += Frame(
      recipient = Recipient(
        id = 4,
        contact = Contact(
          aci = someUuid().toByteArray().toByteString(),
          pni = someUuid().toByteArray().toByteString(),
          username = someNullableUsername(),
          e164 = someE164(),
          blocked = someBoolean(),
          visibility = someEnum(Contact.Visibility::class.java),
          registered = someOneOf(registered),
          notRegistered = someOneOf(notRegistered),
          profileKey = someNullableBytes(32)?.toByteString(),
          profileSharing = someBoolean(),
          profileGivenName = some(Generators.firstNames().nullable()),
          profileFamilyName = some(Generators.lastNames().nullable()),
          hideStory = someBoolean(),
          identityKey = identityKey?.toByteString(),
          identityState = someEnum(Contact.IdentityState::class.java).takeIf { identityKey != null } ?: Contact.IdentityState.DEFAULT
        )
      )
    )
  }
}
