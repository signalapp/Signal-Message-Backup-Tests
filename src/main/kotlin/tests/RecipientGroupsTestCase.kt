package tests

import Generator
import PermutationScope
import TestCase
import asList
import map
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.Contact
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Group
import org.thoughtcrime.securesms.backup.v2.proto.Recipient

/**
 * Every reasonable permutation of Recipient.Group
 */
object RecipientGroupsTestCase : TestCase("recipient_groups") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol
    frames += StandardFrames.recipientDan
    frames += StandardFrames.recipientEve
    frames += StandardFrames.recipientFrank

    frames += Frame(
      recipient = Recipient(
        id = 10,
        group = Group(
          masterKey = someBytes(32).toByteString(),
          whitelisted = someBoolean(),
          hideStory = someBoolean(),
          storySendMode = someEnum(Group.StorySendMode::class.java),
          snapshot = somePermutation {
            frames += Group.GroupSnapshot(
              title = Group.GroupAttributeBlob(
                title = someNonEmptyString()
              ),
              description = someNullablePermutation {
                frames += Group.GroupAttributeBlob(
                  descriptionText = someNonEmptyString()
                )
              },
              avatarUrl = someUrl(),
              disappearingMessagesTimer = somePermutation {
                frames += Group.GroupAttributeBlob(
                  disappearingMessagesDuration = somePositiveInt()
                )
              },
              accessControl = Group.AccessControl(
                attributes = someEnum(Group.AccessControl.AccessRequired::class.java, Group.AccessControl.AccessRequired.UNKNOWN, Group.AccessControl.AccessRequired.ANY, Group.AccessControl.AccessRequired.UNSATISFIABLE),
                members = someEnum(Group.AccessControl.AccessRequired::class.java, Group.AccessControl.AccessRequired.UNKNOWN, Group.AccessControl.AccessRequired.ANY, Group.AccessControl.AccessRequired.UNSATISFIABLE),
                addFromInviteLink = someEnum(Group.AccessControl.AccessRequired::class.java, Group.AccessControl.AccessRequired.UNKNOWN, Group.AccessControl.AccessRequired.ANY, Group.AccessControl.AccessRequired.MEMBER)
              ),
              version = somePositiveInt(),
              members = Generators.permutation<Group.Member> {
                val userGenerator: Generator<Contact> = Generators.list(StandardFrames.recipientAlice.recipient!!.contact!!, StandardFrames.recipientBob.recipient!!.contact!!, StandardFrames.recipientCarol.recipient!!.contact!!)
                val user = some(userGenerator)
                frames += Group.Member(
                  userId = user.aci!!,
                  role = someEnum(Group.Member.Role::class.java, excluding = Group.Member.Role.UNKNOWN),
                  profileKey = user.profileKey!!
                )
              }.asList(1, 2, 3).map { members ->
                members + Group.Member(
                  userId = StandardFrames.SELF_ACI.toByteString(),
                  role = Group.Member.Role.DEFAULT,
                  // Backups have no references to our ACI so Desktop can't fill
                  // this back in.
                  profileKey = ByteArray(32) { 0 }.toByteString()
                )
              }.let { some(it) },
              membersPendingProfileKey = Generators.permutation<Group.MemberPendingProfileKey> {
                frames += Group.MemberPendingProfileKey(
                  member = Group.Member(
                    userId = StandardFrames.recipientDan.recipient!!.contact!!.aci!!,
                    role = Group.Member.Role.DEFAULT
                  ),
                  addedByUserId = StandardFrames.recipientAlice.recipient!!.contact!!.aci!!,
                  timestamp = someIncrementingTimestamp()
                )
              }.asList(0, 1).let { some(it) },
              membersPendingAdminApproval = Generators.permutation<Group.MemberPendingAdminApproval> {
                frames += Group.MemberPendingAdminApproval(
                  userId = StandardFrames.recipientEve.recipient!!.contact!!.aci!!,
                  profileKey = StandardFrames.recipientEve.recipient!!.contact!!.profileKey!!,
                  timestamp = someIncrementingTimestamp()
                )
              }.asList(0, 1).let { some(it) },
              members_banned = Generators.permutation<Group.MemberBanned> {
                frames += Group.MemberBanned(
                  userId = StandardFrames.recipientFrank.recipient!!.contact!!.aci!!,
                  timestamp = someIncrementingTimestamp()
                )
              }.asList(0, 1).let { some(it) },
              inviteLinkPassword = someBytes(32).toByteString(),
              announcements_only = someBoolean()
            )
          }
        )
      )
    )
  }
}
