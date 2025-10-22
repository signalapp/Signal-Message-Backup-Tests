package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import aci
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.*
import pni
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

    val groupMembersExcludingSelfGenerator = { Generators.list(StandardFrames.recipientAlice, StandardFrames.recipientBob) }
    val peopleNotInGroupGenerator = { Generators.list(StandardFrames.recipientCarol, StandardFrames.recipientDan) }

    val updaters = mutableListOf<Frame>()
    val listOfGenerators = oneOf(
      // genericGroupUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GenericGroupUpdate(
          updaterAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupCreationUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupCreationUpdate(
          updaterAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupNameUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupNameUpdate(
          updaterAci = updater.aci,
          newGroupName = some(Generators.titles().nullable())
        )
        updaters.add(updater)
      },
      // groupAvatarUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupAvatarUpdate(
          updaterAci = updater.aci,
          wasRemoved = someBoolean()
        )
        updaters.add(updater)
      },
      // groupDescriptionUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupDescriptionUpdate(
          updaterAci = updater.aci,
          newDescription = some(Generators.textBody().nullable())
        )
        updaters.add(updater)
      },
      // groupMembershipAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupMembershipAccessLevelChangeUpdate(
          updaterAci = updater.aci,
          accessLevel = someEnum(GroupV2AccessLevel::class.java, excluding = listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
        updaters.add(updater)
      },
      // groupAttributesAccessLevelChangeUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupAttributesAccessLevelChangeUpdate(
          updaterAci = updater.aci,
          accessLevel = someEnum(GroupV2AccessLevel::class.java, listOf(GroupV2AccessLevel.UNKNOWN, GroupV2AccessLevel.UNSATISFIABLE))
        )
        updaters.add(updater)
      },
      // groupAnnouncementOnlyChangeUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupAnnouncementOnlyChangeUpdate(
          updaterAci = updater.aci,
          isAnnouncementOnly = someBoolean()
        )
        updaters.add(updater)
      },
      // groupAdminStatusUpdateGenerator,
      Generators.permutation {
        frames += GroupAdminStatusUpdate(
          updaterAci = StandardFrames.SELF_ACI.toByteString(),
          memberAci = some(groupMembersExcludingSelfGenerator()).aci,
          wasAdminStatusGranted = someBoolean()
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupMemberLeftUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberLeftUpdate(
          aci = some(groupMembersExcludingSelfGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupMemberRemovedUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberRemovedUpdate(
          removerAci = StandardFrames.SELF_ACI.toByteString(),
          removedAci = some(groupMembersExcludingSelfGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // selfInvitedToGroupUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += SelfInvitedToGroupUpdate(
          inviterAci = updater.aci
        )
        updaters.add(updater)
      },
      // selfInvitedOtherUserToGroupUpdateGenerator,
      Generators.permutation {
        frames += SelfInvitedOtherUserToGroupUpdate(
          inviteeServiceId = some(groupMembersExcludingSelfGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupUnknownInviteeUpdateGenerator,
      Generators.permutation {
        frames += GroupUnknownInviteeUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()).aci,
          inviteeCount = some(Generators.ints(1, 3))
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupInvitationAcceptedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationAcceptedUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()).aci,
          newMemberAci = some(peopleNotInGroupGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupInvitationDeclinedUpdateGenerator,
      Generators.permutation {
        frames += GroupInvitationDeclinedUpdate(
          inviterAci = some(groupMembersExcludingSelfGenerator()).aci,
          inviteeAci = some(peopleNotInGroupGenerator().nullable())?.aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupMemberJoinedUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberJoinedUpdate(
          newMemberAci = some(peopleNotInGroupGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupMemberAddedUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        val inviter = some(groupMembersExcludingSelfGenerator().nullable())
        val hadOpenInvitation = someBoolean()
        frames += GroupMemberAddedUpdate(
          updaterAci = updater.aci,
          newMemberAci = some(peopleNotInGroupGenerator()).aci,
          hadOpenInvitation = if (inviter != null) true else hadOpenInvitation,
          inviterAci = inviter?.aci
        )
        updaters.add(updater)
      },
      // groupSelfInvitationRevokedUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupSelfInvitationRevokedUpdate(
          revokerAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupInvitationRevokedUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupInvitationRevokedUpdate(
          updaterAci = updater.aci,
          invitees = listOf(
            GroupInvitationRevokedUpdate.Invitee(
              inviterAci = some(groupMembersExcludingSelfGenerator()).aci,
              inviteeAci = some(Generators.list(StandardFrames.recipientCarol.aci, null)),
              inviteePni = some(Generators.list(null, StandardFrames.recipientCarol.pni))
            )
          )
        )
        updaters.add(updater)
      },
      // groupJoinRequestUpdateGenerator,
      Generators.permutation {
        val updater = some(peopleNotInGroupGenerator())
        frames += GroupJoinRequestUpdate(
          requestorAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupJoinRequestApprovalUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupJoinRequestApprovalUpdate(
          requestorAci = some(peopleNotInGroupGenerator()).aci,
          updaterAci = updater.aci,
          wasApproved = someBoolean()
        )
        updaters.add(updater)
      },
      // groupJoinRequestCanceledUpdateGenerator,
      Generators.permutation {
        frames += GroupJoinRequestCanceledUpdate(
          requestorAci = some(peopleNotInGroupGenerator()).aci
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupInviteLinkResetUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupInviteLinkResetUpdate(
          updaterAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupInviteLinkEnabledUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupInviteLinkEnabledUpdate(
          updaterAci = updater.aci,
          linkRequiresAdminApproval = someBoolean()
        )
        updaters.add(updater)
      },
      // groupInviteLinkAdminApprovalUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupInviteLinkAdminApprovalUpdate(
          updaterAci = updater.aci,
          linkRequiresAdminApproval = someBoolean()
        )
        updaters.add(updater)
      },
      // groupInviteLinkDisabledUpdateGenerator,
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupInviteLinkDisabledUpdate(
          updaterAci = updater.aci
        )
        updaters.add(updater)
      },
      // groupMemberJoinedByLinkUpdateGenerator,
      Generators.permutation {
        frames += GroupMemberJoinedByLinkUpdate(
          newMemberAci = some(Generators.list(StandardFrames.recipientCarol.aci, StandardFrames.recipientDan.aci))
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupV2MigrationUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationUpdate()
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupV2MigrationSelfInvitedUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationSelfInvitedUpdate()
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupV2MigrationInvitedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationInvitedMembersUpdate(
          invitedMembersCount = some(Generators.ints(1, 5))
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupV2MigrationDroppedMembersUpdateGenerator,
      Generators.permutation {
        frames += GroupV2MigrationDroppedMembersUpdate(
          droppedMembersCount = some(Generators.ints(1, 5))
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupSequenceOfRequestsAndCancelsUpdateGenerator,
      Generators.permutation {
        frames += GroupSequenceOfRequestsAndCancelsUpdate(
          requestorAci = some(groupMembersExcludingSelfGenerator()).aci,
          count = some(Generators.ints(1, 5))
        )
        updaters.add(StandardFrames.recipientSelf)
      },
      // groupExpirationTimerUpdateGenerator
      Generators.permutation {
        val updater = some(groupMembersExcludingSelfGenerator())
        frames += GroupExpirationTimerUpdate(
          updaterAci = updater.aci,
          expiresInMs = some(Generators.longs(lower = 5.minutes.inWholeSeconds, upper = 28.days.inWholeSeconds)).seconds.inWholeMilliseconds
        )
        updaters.add(updater)
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
    val updatesGenerator = Generators.permutation<GroupChangeChatUpdate.Update> {
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
    }

    val update = some(updatesGenerator)
    val updater = some(Generators.list(updaters))

    val updatersSize = updaters.size
    val updatesSize = updatesGenerator.minSize
    println(updatersSize)
    require(updatersSize == updatesSize) { "Every update frame must specify an updater. Frames: $updatesSize, updaters: $updatersSize." }

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = updater.recipient?.id ?: StandardFrames.recipientSelf.recipient!!.id,
        dateSent = someNonZeroTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          groupChange = GroupChangeChatUpdate(
            updates = listOf(update)
          )
        )
      )
    )
  }
}
