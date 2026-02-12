package tests

import Generator
import Generators
import PermutationScope
import TestCase
import asList
import map
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.*
import kotlin.collections.plus

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

    val groupRecipientId = 10L

    val snapshot: Group.GroupSnapshot = somePermutation {
      frames += Group.GroupSnapshot(
        title = Group.GroupAttributeBlob(
          title = some(Generators.titles())
        ),
        description = someNullablePermutation {
          frames += Group.GroupAttributeBlob(
            descriptionText = some(Generators.textBody())
          )
        },
        avatarUrl = someUrl(),
        disappearingMessagesTimer = someNullablePermutation {
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
        members = generateMembers(),
        membersPendingProfileKey = Generators.permutation<Group.MemberPendingProfileKey> {
          frames += Group.MemberPendingProfileKey(
            member = Group.Member(
              userId = StandardFrames.recipientDan.recipient!!.contact!!.aci!!,
              role = Group.Member.Role.DEFAULT
            ),
            addedByUserId = StandardFrames.recipientAlice.recipient!!.contact!!.aci!!
          )
        }.asList(0, 1).let { some(it) },
        membersPendingAdminApproval = Generators.permutation<Group.MemberPendingAdminApproval> {
          frames += Group.MemberPendingAdminApproval(
            userId = StandardFrames.recipientEve.recipient!!.contact!!.aci!!
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

    // If blocked, can't be whitelisted.
    val (isBlocked, isWhitelisted) = some(
      Generators.list<Pair<Boolean, Boolean>>(
        Pair(true, false),
        Pair(false, true),
        Pair(false, false)
      )
    )

    frames += Frame(
      recipient = Recipient(
        id = groupRecipientId,
        group = Group(
          masterKey = someBytes(32).toByteString(),
          whitelisted = if (isBlocked) false else isWhitelisted,
          hideStory = someBoolean(),
          storySendMode = someEnum(Group.StorySendMode::class.java),
          snapshot = snapshot,
          blocked = isBlocked,
          avatarColor = someEnum(AvatarColor::class.java)
        )
      )
    )

    frames += Frame(
      chat = Chat(
        id = 1,
        recipientId = groupRecipientId,
        expirationTimerMs = snapshot.disappearingMessagesTimer?.let { timer ->
          timer.disappearingMessagesDuration!!.toLong() * 1000
        },
        expireTimerVersion = 1
      )
    )
  }
}

private fun PermutationScope.generateMembers(): List<Group.Member> = some(
  memberGenerator()
    .asList(1, 2, 3)
    .map { members ->
      val label = someMemberLabel()
      val self = Group.Member(
        userId = StandardFrames.SELF_ACI.toByteString(),
        role = Group.Member.Role.DEFAULT,
        labelEmoji = label.emoji,
        labelString = label.string
      )
      members + self
    }
)

private fun memberGenerator(): Generator<Group.Member> = Generators.permutation {
  val userGenerator: Generator<Contact> = Generators.list(
    StandardFrames.recipientAlice.recipient!!.contact!!,
    StandardFrames.recipientBob.recipient!!.contact!!,
    StandardFrames.recipientCarol.recipient!!.contact!!
  )

  val user = some(userGenerator)
  val label = someMemberLabel()

  frames += Group.Member(
    userId = user.aci!!,
    role = someEnum(Group.Member.Role::class.java, excluding = Group.Member.Role.UNKNOWN),
    labelEmoji = label.emoji,
    labelString = label.string
  )
}

private fun PermutationScope.someMemberLabel(): MemberLabel = some(
  Generators.list(
    MemberLabel(emoji = "", string = ""),
    MemberLabel(emoji = "", string = some(Generators.titles())),
    MemberLabel(emoji = some(Generators.emoji()), string = some(Generators.titles()))
  )
)

private data class MemberLabel(val emoji: String, val string: String)
