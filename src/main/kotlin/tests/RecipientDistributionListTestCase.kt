package tests

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
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientCarol

    val memberIds: List<Long> = listOf(
      StandardFrames.recipientAlice,
      StandardFrames.recipientBob,
      StandardFrames.recipientCarol
    ).map { it.recipient!!.id }

    val (deletionTimestampGenerator, distributionListGenerator) = oneOf(
      Generators.single(someIncrementingTimestamp()),
      Generators.permutation {
        val privacyModeGenerator = Generators.enum(DistributionList.PrivacyMode::class.java, excluding = DistributionList.PrivacyMode.UNKNOWN)
        val memberRecipientIdGenerator = Generators.list(memberIds).asList(0, 1, 2, 3)

        val privacyMode = some(privacyModeGenerator)
        val memberRecipientIds = some(memberRecipientIdGenerator)

        frames += DistributionList(
          name = someNonEmptyString(),
          allowReplies = someBoolean(),
          memberRecipientIds = if (privacyMode == DistributionList.PrivacyMode.ALL) {
            emptyList()
          } else {
            memberRecipientIds
          },
          privacyMode = privacyMode
        )
      }
    )

    frames += Frame(
      recipient = Recipient(
        id = 7,
        distributionList = DistributionListItem(
          distributionId = someUuid().toByteArray().toByteString(),
          deletionTimestamp = someOneOf(deletionTimestampGenerator),
          distributionList = someOneOf(distributionListGenerator)
        )
      )
    )
  }
}
