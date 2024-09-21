package tests

import Generators
import PermutationScope
import TestCase
import asList
import map
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.DistributionList
import org.thoughtcrime.securesms.backup.v2.proto.DistributionListItem
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.Recipient
import toByteArray

/**
 * Every reasonable permutation of Recipient.DistributionList
 */
object RecipientDistributionListTestCase : TestCase("recipient_distribution_list") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES_WITHOUT_MY_STORY

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol

    val memberIds: List<Long> = listOf(
      StandardFrames.recipientAlice,
      StandardFrames.recipientBob,
      StandardFrames.recipientCarol
    ).map { it.recipient!!.id }

    val myStoryGenerator = Generators.permutation<DistributionList> {
      val privacyModeGenerator = Generators.enum(DistributionList.PrivacyMode::class.java, excluding = DistributionList.PrivacyMode.UNKNOWN)
      val memberRecipientIdGenerator = Generators.list(memberIds).asList(1, 2, 3)

      val privacyMode = some(privacyModeGenerator)
      val memberRecipientIds = some(memberRecipientIdGenerator)

      frames += DistributionList(
        // Empty name specifically for My Story, which has a localized name
        // built into clients.
        name = "",
        allowReplies = someBoolean(),
        memberRecipientIds = if (privacyMode == DistributionList.PrivacyMode.ALL) {
          emptyList()
        } else {
          memberRecipientIds
        },
        privacyMode = privacyMode
      )
    }

    frames += Frame(
      recipient = Recipient(
        id = 7,
        distributionList = DistributionListItem(
          distributionId = StandardFrames.MY_STORY_UUID,
          distributionList = some(myStoryGenerator)
        )
      )
    )

    val (deletionTimestampGenerator, distributionListGenerator) = oneOf(
      Generators.single(someIncrementingTimestamp()),
      Generators.permutation {
        val memberRecipientIdGenerator = Generators.list(memberIds).asList(1, 2, 3)

        val privacyMode = DistributionList.PrivacyMode.ONLY_WITH
        val memberRecipientIds = some(memberRecipientIdGenerator)

        frames += DistributionList(
          name = some(Generators.titles()),
          allowReplies = someBoolean(),
          memberRecipientIds = memberRecipientIds,
          privacyMode = privacyMode
        )
      }
    )

    frames += Frame(
      recipient = Recipient(
        id = 8,
        distributionList = DistributionListItem(
          distributionId = someUuid().toByteArray().toByteString(),
          deletionTimestamp = someOneOf(deletionTimestampGenerator),
          distributionList = someOneOf(distributionListGenerator)
        )
      )
    )
  }
}
