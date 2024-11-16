@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import aci
import asList
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import toByteString
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Reasonable permutations of [GroupChangeChatMultipleUpdate] objects.
 */
object ChatItemGroupChangeChatMultipleUpdateTestCase : TestCase("chat_item_group_change_chat_multiple_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol
    frames += StandardFrames.recipientDan
    frames += StandardFrames.recipientGroupAB
    frames += StandardFrames.chatGroupAB

    val groupMembersExcludingSelfGenerator = { Generators.list(StandardFrames.recipientAlice.aci, StandardFrames.recipientBob.aci) }
    val peopleNotInGroupGenerator = { Generators.list(StandardFrames.recipientCarol.aci, StandardFrames.recipientDan.aci) }

    var updaterAci = StandardFrames.SELF_ACI.toByteString()

    val listOfGenerators = oneOf(
      // genericGroupUpdateGenerator,
      Generators.permutation {
        frames += GenericGroupUpdate(
          updaterAci
        )
      },
      // groupCreationUpdateGenerator,
      Generators.permutation {
        frames += GroupCreationUpdate(
          updaterAci
        )
      },
      // groupNameUpdateGenerator,
      Generators.permutation {
        frames += GroupNameUpdate(
          updaterAci,
          newGroupName = some(Generators.titles().nullable())
        )
      },
      // groupAvatarUpdateGenerator,
      Generators.permutation {
        frames += GroupAvatarUpdate(
          updaterAci,
          wasRemoved = someBoolean()
        )
      },
      // groupDescriptionUpdateGenerator,
      Generators.permutation {
        frames += GroupDescriptionUpdate(
          updaterAci,
          newDescription = some(Generators.textBody().nullable())
        )
      },
      // groupMembershipAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupMembershipAccessLevelChangeUpdate(
          updaterAci,
          accessLevel = someEnum(GroupV2AccessLevel::class.java, excluding = listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
      },
      // groupAttributesAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupAttributesAccessLevelChangeUpdate(
          updaterAci,
          accessLevel = someEnum(GroupV2AccessLevel::class.java, listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
      },
      // groupAnnouncementOnlyChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupAnnouncementOnlyChangeUpdate(
          updaterAci,
          isAnnouncementOnly = someBoolean()
        )
      },
      // groupAdminStatusUpdateGenerator,
      Generators.permutation {
        frames += GroupAdminStatusUpdate(
          updaterAci,
          memberAci = some(groupMembersExcludingSelfGenerator()),
          wasAdminStatusGranted = someBoolean()
        )
      },
      // groupMemberRemovedUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberRemovedUpdate(
          removerAci = updaterAci,
          removedAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupUnknownInviteeUpdateGenerator,
      Generators.permutation {
        frames += GroupUnknownInviteeUpdate(
          inviterAci = updaterAci,
          inviteeCount = some(Generators.ints(1, 3))
        )
      },
      // groupMemberJoinedUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberJoinedUpdate(
          newMemberAci = some(peopleNotInGroupGenerator())
        )
      },
      // groupMemberAddedUpdateGenerator,
      Generators.permutation {
        val inviter = some(groupMembersExcludingSelfGenerator().nullable())
        val hadOpenInvitation = someBoolean()
        frames += GroupMemberAddedUpdate(
          updaterAci,
          newMemberAci = some(peopleNotInGroupGenerator()),
          hadOpenInvitation = if (inviter != null) true else hadOpenInvitation,
          inviterAci = inviter
        )
      },
      // groupSelfInvitationRevokedUpdateGenerator,
      Generators.permutation {
        frames += GroupSelfInvitationRevokedUpdate(
          revokerAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupInvitationRevokedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationRevokedUpdate(
          updaterAci
        )
      },
      // groupJoinRequestUpdateGenerator,
      Generators.permutation {
        frames += GroupJoinRequestUpdate(
          requestorAci = some(peopleNotInGroupGenerator())
        )
      },
      // groupJoinRequestApprovalUpdateGenerator,
      Generators.permutation {
        frames += GroupJoinRequestApprovalUpdate(
          requestorAci = some(peopleNotInGroupGenerator()),
          updaterAci,
          wasApproved = someBoolean()
        )
      },
      // groupJoinRequestCanceledUpdateGenerator,
      Generators.permutation {
        frames += GroupJoinRequestCanceledUpdate(
          requestorAci = some(peopleNotInGroupGenerator())
        )
      },
      // groupInviteLinkResetUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkResetUpdate(
          updaterAci
        )
      },
      // groupInviteLinkEnabledUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkEnabledUpdate(
          updaterAci,
          linkRequiresAdminApproval = someBoolean()
        )
      },
      // groupInviteLinkAdminApprovalUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkAdminApprovalUpdate(
          updaterAci,
          linkRequiresAdminApproval = someBoolean()
        )
      },
      // groupInviteLinkDisabledUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkDisabledUpdate(
          updaterAci
        )
      },
      // groupMemberJoinedByLinkUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberJoinedByLinkUpdate(
          newMemberAci = some(Generators.list(StandardFrames.recipientCarol.aci, StandardFrames.recipientDan.aci))
        )
      },
      // groupV2MigrationUpdateGenerator,
      Generators.single(GroupV2MigrationUpdate()),
      // groupV2MigrationSelfInvitedUpdateGenerator,
      Generators.single(GroupV2MigrationSelfInvitedUpdate()),
      // groupV2MigrationInvitedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationInvitedMembersUpdate(
          invitedMembersCount = some(Generators.ints(1, 5))
        )
      },
      // groupV2MigrationDroppedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationDroppedMembersUpdate(
          droppedMembersCount = some(Generators.ints(1, 5))
        )
      },
      // groupSequenceOfRequestsAndCancelsUpdateGenerator,
      Generators.permutation {
        frames += GroupSequenceOfRequestsAndCancelsUpdate(
          requestorAci = some(groupMembersExcludingSelfGenerator()),
          count = some(Generators.ints(1, 5))
        )
      },
      // groupExpirationTimerUpdateGenerator
      Generators.permutation {
        frames += GroupExpirationTimerUpdate(
          updaterAci = updaterAci,
          expiresInMs = some(Generators.longs(lower = 5.minutes.inWholeSeconds, upper = 28.days.inWholeSeconds)).seconds.inWholeMilliseconds
        )
      }
    )

    val genericGroupUpdateGenerator = listOfGenerators[0]
    val groupCreationUpdateGenerator = listOfGenerators[1]
    val groupNameUpdateGenerator = listOfGenerators[2]
    val groupAvatarUpdateGenerator = listOfGenerators[3]
    val groupDescriptionUpdateGenerator = listOfGenerators[4]
    val groupMembershipAccessLevelChangeUpdateGenerator = listOfGenerators[5]
    val groupAttributesAccessLevelChangeUpdateGenerator = listOfGenerators[6]
    val groupAnnouncementOnlyChangeUpdateGenerator = listOfGenerators[7]
    val groupAdminStatusUpdateGenerator = listOfGenerators[8]
    val groupMemberRemovedUpdateGenerator = listOfGenerators[9]
    val groupUnknownInviteeUpdateGenerator = listOfGenerators[10]
    val groupMemberJoinedUpdateGenerator = listOfGenerators[11]
    val groupMemberAddedUpdateGenerator = listOfGenerators[12]
    val groupSelfInvitationRevokedUpdateGenerator = listOfGenerators[13]
    val groupInvitationRevokedUpdateGenerator = listOfGenerators[14]
    val groupJoinRequestUpdateGenerator = listOfGenerators[15]
    val groupJoinRequestApprovalUpdateGenerator = listOfGenerators[16]
    val groupJoinRequestCanceledUpdateGenerator = listOfGenerators[17]
    val groupInviteLinkResetUpdateGenerator = listOfGenerators[18]
    val groupInviteLinkEnabledUpdateGenerator = listOfGenerators[19]
    val groupInviteLinkAdminApprovalUpdateGenerator = listOfGenerators[20]
    val groupInviteLinkDisabledUpdateGenerator = listOfGenerators[21]
    val groupMemberJoinedByLinkUpdateGenerator = listOfGenerators[22]
    val groupV2MigrationUpdateGenerator = listOfGenerators[23]
    val groupV2MigrationSelfInvitedUpdateGenerator = listOfGenerators[24]
    val groupV2MigrationInvitedMembersUpdateGenerator = listOfGenerators[25]
    val groupV2MigrationDroppedMembersUpdateGenerator = listOfGenerators[26]
    val groupSequenceOfRequestsAndCancelsUpdateGenerator = listOfGenerators[27]
    val groupExpirationTimerUpdateGenerator = listOfGenerators[28]

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          groupChange = GroupChangeChatUpdate(
            updates = Generators.permutation<GroupChangeChatUpdate.Update> {
              frames += GroupChangeChatUpdate.Update(
                genericGroupUpdate = someOneOf(genericGroupUpdateGenerator),
                groupCreationUpdate = someOneOf(groupCreationUpdateGenerator),
                groupNameUpdate = someOneOf(groupNameUpdateGenerator),
                groupAvatarUpdate = someOneOf(groupAvatarUpdateGenerator),
                groupDescriptionUpdate = someOneOf(groupDescriptionUpdateGenerator),
                groupMembershipAccessLevelChangeUpdate = someOneOf(groupMembershipAccessLevelChangeUpdateGenerator),
                groupAttributesAccessLevelChangeUpdate = someOneOf(groupAttributesAccessLevelChangeUpdateGenerator),
                groupAnnouncementOnlyChangeUpdate = someOneOf(groupAnnouncementOnlyChangeUpdateGenerator),
                groupAdminStatusUpdate = someOneOf(groupAdminStatusUpdateGenerator),
                groupMemberRemovedUpdate = someOneOf(groupMemberRemovedUpdateGenerator),
                groupUnknownInviteeUpdate = someOneOf(groupUnknownInviteeUpdateGenerator),
                groupMemberJoinedUpdate = someOneOf(groupMemberJoinedUpdateGenerator),
                groupMemberAddedUpdate = someOneOf(groupMemberAddedUpdateGenerator),
                groupSelfInvitationRevokedUpdate = someOneOf(groupSelfInvitationRevokedUpdateGenerator),
                groupInvitationRevokedUpdate = someOneOf(groupInvitationRevokedUpdateGenerator),
                groupJoinRequestUpdate = someOneOf(groupJoinRequestUpdateGenerator),
                groupJoinRequestApprovalUpdate = someOneOf(groupJoinRequestApprovalUpdateGenerator),
                groupJoinRequestCanceledUpdate = someOneOf(groupJoinRequestCanceledUpdateGenerator),
                groupInviteLinkResetUpdate = someOneOf(groupInviteLinkResetUpdateGenerator),
                groupInviteLinkEnabledUpdate = someOneOf(groupInviteLinkEnabledUpdateGenerator),
                groupInviteLinkAdminApprovalUpdate = someOneOf(groupInviteLinkAdminApprovalUpdateGenerator),
                groupInviteLinkDisabledUpdate = someOneOf(groupInviteLinkDisabledUpdateGenerator),
                groupMemberJoinedByLinkUpdate = someOneOf(groupMemberJoinedByLinkUpdateGenerator),
                groupV2MigrationUpdate = someOneOf(groupV2MigrationUpdateGenerator),
                groupV2MigrationSelfInvitedUpdate = someOneOf(groupV2MigrationSelfInvitedUpdateGenerator),
                groupV2MigrationInvitedMembersUpdate = someOneOf(groupV2MigrationInvitedMembersUpdateGenerator),
                groupV2MigrationDroppedMembersUpdate = someOneOf(groupV2MigrationDroppedMembersUpdateGenerator),
                groupSequenceOfRequestsAndCancelsUpdate = someOneOf(groupSequenceOfRequestsAndCancelsUpdateGenerator),
                groupExpirationTimerUpdate = someOneOf(groupExpirationTimerUpdateGenerator)
              )
            }.asList(*List(listOfGenerators.size / 3) { 3 }.toIntArray()).let { some(it) }
          )
        )
      )
    )
  }
}
