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
 * Reasonable permutations of [GroupChangeChatUpdate] objects.
 */
object ChatItemGroupChangeChatUpdateTestCase : TestCase("chat_item_group_change_chat_update") {
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

    val listOfGenerators = oneOf(
      // genericGroupUpdateGenerator,
      Generators.permutation {
        frames += GenericGroupUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupCreationUpdateGenerator,
      Generators.permutation {
        frames += GroupCreationUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupNameUpdateGenerator,
      Generators.permutation {
        frames += GroupNameUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          newGroupName = some(Generators.titles().nullable())
        )
      },
      // groupAvatarUpdateGenerator,
      Generators.permutation {
        frames += GroupAvatarUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          wasRemoved = someBoolean()
        )
      },
      // groupDescriptionUpdateGenerator,
      Generators.permutation {
        frames += GroupDescriptionUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          newDescription = some(Generators.textBody().nullable())
        )
      },
      // groupMembershipAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupMembershipAccessLevelChangeUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          accessLevel = someEnum(GroupV2AccessLevel::class.java, excluding = listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
      },
      // groupAttributesAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupAttributesAccessLevelChangeUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          accessLevel = someEnum(GroupV2AccessLevel::class.java, listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
      },
      // groupAnnouncementOnlyChangeUpdateGenerator,
      Generators.permutation {
        frames += GroupAnnouncementOnlyChangeUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          isAnnouncementOnly = someBoolean()
        )
      },
      // groupAdminStatusUpdateGenerator,
      Generators.permutation {
        frames += GroupAdminStatusUpdate(
          updaterAci = StandardFrames.SELF_ACI.toByteString(),
          memberAci = some(groupMembersExcludingSelfGenerator()),
          wasAdminStatusGranted = someBoolean()
        )
      },
      // groupMemberLeftUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberLeftUpdate(
          aci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupMemberRemovedUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberRemovedUpdate(
          removerAci = StandardFrames.SELF_ACI.toByteString(),
          removedAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // selfInvitedToGroupUpdateGenerator,
      Generators.permutation {
        frames += SelfInvitedToGroupUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // selfInvitedOtherUserToGroupUpdateGenerator,
      Generators.permutation {
        frames += SelfInvitedOtherUserToGroupUpdate(
          inviteeServiceId = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupUnknownInviteeUpdateGenerator,
      Generators.permutation {
        frames += GroupUnknownInviteeUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()),
          inviteeCount = some(Generators.ints(1, 3))
        )
      },
      // groupInvitationAcceptedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationAcceptedUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()),
          newMemberAci = some(peopleNotInGroupGenerator())
        )
      },
      // groupInvitationDeclinedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationDeclinedUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()),
          inviteeAci = some(peopleNotInGroupGenerator())
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
          updaterAci = some(groupMembersExcludingSelfGenerator()),
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
          updaterAci = some(groupMembersExcludingSelfGenerator())
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
          updaterAci = some(groupMembersExcludingSelfGenerator()),
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
          updaterAci = some(groupMembersExcludingSelfGenerator())
        )
      },
      // groupInviteLinkEnabledUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkEnabledUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          linkRequiresAdminApproval = someBoolean()
        )
      },
      // groupInviteLinkAdminApprovalUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkAdminApprovalUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator()),
          linkRequiresAdminApproval = someBoolean()
        )
      },
      // groupInviteLinkDisabledUpdateGenerator,
      Generators.permutation {
        frames += GroupInviteLinkDisabledUpdate(
          updaterAci = some(groupMembersExcludingSelfGenerator())
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
          updaterAci = some(groupMembersExcludingSelfGenerator()),
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
    val groupMemberLeftUpdateGenerator = listOfGenerators[9]
    val groupMemberRemovedUpdateGenerator = listOfGenerators[10]
    val selfInvitedToGroupUpdateGenerator = listOfGenerators[11]
    val selfInvitedOtherUserToGroupUpdateGenerator = listOfGenerators[12]
    val groupUnknownInviteeUpdateGenerator = listOfGenerators[13]
    val groupInvitationAcceptedUpdateGenerator = listOfGenerators[14]
    val groupInvitationDeclinedUpdateGenerator = listOfGenerators[15]
    val groupMemberJoinedUpdateGenerator = listOfGenerators[16]
    val groupMemberAddedUpdateGenerator = listOfGenerators[17]
    val groupSelfInvitationRevokedUpdateGenerator = listOfGenerators[18]
    val groupInvitationRevokedUpdateGenerator = listOfGenerators[19]
    val groupJoinRequestUpdateGenerator = listOfGenerators[20]
    val groupJoinRequestApprovalUpdateGenerator = listOfGenerators[21]
    val groupJoinRequestCanceledUpdateGenerator = listOfGenerators[22]
    val groupInviteLinkResetUpdateGenerator = listOfGenerators[23]
    val groupInviteLinkEnabledUpdateGenerator = listOfGenerators[24]
    val groupInviteLinkAdminApprovalUpdateGenerator = listOfGenerators[25]
    val groupInviteLinkDisabledUpdateGenerator = listOfGenerators[26]
    val groupMemberJoinedByLinkUpdateGenerator = listOfGenerators[27]
    val groupV2MigrationUpdateGenerator = listOfGenerators[28]
    val groupV2MigrationSelfInvitedUpdateGenerator = listOfGenerators[29]
    val groupV2MigrationInvitedMembersUpdateGenerator = listOfGenerators[30]
    val groupV2MigrationDroppedMembersUpdateGenerator = listOfGenerators[31]
    val groupSequenceOfRequestsAndCancelsUpdateGenerator = listOfGenerators[32]
    val groupExpirationTimerUpdateGenerator = listOfGenerators[33]

    val clusters = mutableListOf<Int>()
    clusters += List(listOfGenerators.size) { 1 }
    clusters += List(listOfGenerators.size / 3) { 3 }

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
                groupMemberLeftUpdate = someOneOf(groupMemberLeftUpdateGenerator),
                groupMemberRemovedUpdate = someOneOf(groupMemberRemovedUpdateGenerator),
                selfInvitedToGroupUpdate = someOneOf(selfInvitedToGroupUpdateGenerator),
                selfInvitedOtherUserToGroupUpdate = someOneOf(selfInvitedOtherUserToGroupUpdateGenerator),
                groupUnknownInviteeUpdate = someOneOf(groupUnknownInviteeUpdateGenerator),
                groupInvitationAcceptedUpdate = someOneOf(groupInvitationAcceptedUpdateGenerator),
                groupInvitationDeclinedUpdate = someOneOf(groupInvitationDeclinedUpdateGenerator),
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
            }.asList(*clusters.toIntArray()).let { some(it) }
          )
        )
      )
    )
  }
}
