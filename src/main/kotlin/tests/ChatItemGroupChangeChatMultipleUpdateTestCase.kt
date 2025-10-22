@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import aci
import asList
import chunkSizes
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import pni
import toByteString

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
          revokerAci = StandardFrames.SELF_ACI.toByteString()
        )
      },
      // groupInvitationRevokedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationRevokedUpdate(
          updaterAci,
          invitees = listOf(
            GroupInvitationRevokedUpdate.Invitee(
              inviterAci = some(groupMembersExcludingSelfGenerator()),
              inviteeAci = some(Generators.list(StandardFrames.recipientCarol.aci, null)),
              inviteePni = some(Generators.list(null, StandardFrames.recipientCarol.pni))
            )
          )
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
          requestorAci = StandardFrames.SELF_ACI.toByteString()
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
          newMemberAci = StandardFrames.SELF_ACI.toByteString()
        )
      },

      // groupSequenceOfRequestsAndCancelsUpdateGenerator,
      Generators.permutation {
        frames += GroupSequenceOfRequestsAndCancelsUpdate(
          requestorAci = StandardFrames.SELF_ACI.toByteString(),
          count = some(Generators.ints(1, 5))
        )
      }
    )

    val listOfMigrationGenerators = oneOf(
      // groupV2MigrationSelfInvitedUpdateGenerator,
      Generators.single(GroupV2MigrationSelfInvitedUpdate()),
      // groupV2MigrationInvitedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationInvitedMembersUpdate(
          invitedMembersCount = 2
        )
      },
      // groupV2MigrationDroppedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationDroppedMembersUpdate(
          droppedMembersCount = 3
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
    val groupMemberAddedUpdateGenerator = listOfGenerators[11]
    val groupSelfInvitationRevokedUpdateGenerator = listOfGenerators[12]
    val groupInvitationRevokedUpdateGenerator = listOfGenerators[13]
    val groupJoinRequestUpdateGenerator = listOfGenerators[14]
    val groupJoinRequestApprovalUpdateGenerator = listOfGenerators[15]
    val groupJoinRequestCanceledUpdateGenerator = listOfGenerators[16]
    val groupInviteLinkResetUpdateGenerator = listOfGenerators[17]
    val groupInviteLinkEnabledUpdateGenerator = listOfGenerators[18]
    val groupInviteLinkAdminApprovalUpdateGenerator = listOfGenerators[19]
    val groupInviteLinkDisabledUpdateGenerator = listOfGenerators[20]
    val groupMemberJoinedByLinkUpdateGenerator = listOfGenerators[21]
    val groupSequenceOfRequestsAndCancelsUpdateGenerator = listOfGenerators[22]

    val updates = Generators.permutation<GroupChangeChatUpdate.Update> {
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
        groupSequenceOfRequestsAndCancelsUpdate = someOneOf(groupSequenceOfRequestsAndCancelsUpdateGenerator)
      )
    }

    // Migration update messages should not be mixed with other types of update messages
    val groupV2MigrationSelfInvitedUpdateGenerator = listOfMigrationGenerators[0]
    val groupV2MigrationInvitedMembersUpdateGenerator = listOfMigrationGenerators[1]
    val groupV2MigrationDroppedMembersUpdateGenerator = listOfMigrationGenerators[2]

    val migrationUpdates = Generators.permutation<GroupChangeChatUpdate.Update> {
      frames += GroupChangeChatUpdate.Update(
        groupV2MigrationSelfInvitedUpdate = someOneOf(groupV2MigrationSelfInvitedUpdateGenerator),
        groupV2MigrationInvitedMembersUpdate = someOneOf(groupV2MigrationInvitedMembersUpdateGenerator),
        groupV2MigrationDroppedMembersUpdate = someOneOf(groupV2MigrationDroppedMembersUpdateGenerator)
      )
    }

    val multipleUpdatesGenerator = updates.asList(*chunkSizes(updates.minSize, 3))
    val multipleMigrationUpdatesGenerator = migrationUpdates.asList(*chunkSizes(migrationUpdates.minSize, 3))

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          groupChange = GroupChangeChatUpdate(
            updates = some(Generators.merge(multipleUpdatesGenerator, multipleMigrationUpdatesGenerator))
          )
        )
      )
    )
  }
}
