@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import TestCase
import asGenerator
import nullable
import okio.ByteString.Companion.toByteString
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.AccountData
import org.thoughtcrime.securesms.backup.v2.proto.ChatStyle
import org.thoughtcrime.securesms.backup.v2.proto.Frame

/**
 * Every reasonable permutation of AccountData.
 */
object AccountDataTestCase : TestCase("account_data") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.backupInfo

    val (wallpaperPhotoGenerator, wallpaperPresetGenerator) = oneOf(
      Generators.wallpaperFilePointer() as Generator<Any?>,
      Generators.enum(ChatStyle.WallpaperPreset::class.java, excluding = ChatStyle.WallpaperPreset.UNKNOWN_WALLPAPER_PRESET) as Generator<Any?>
    )

    val (bubbleAutoGenerator, bubblePresetGenerator, bubbleCustomGenerator) = oneOf(
      Generators.list(ChatStyle.AutomaticBubbleColor()),
      Generators.enum(ChatStyle.BubbleColorPreset::class.java, excluding = ChatStyle.BubbleColorPreset.UNKNOWN_BUBBLE_COLOR_PRESET) as Generator<Any?>,
      Generators.list(1L, 2L, 3L) as Generator<Any?>
    )

    val usernameLinkGenerator: Generator<AccountData.UsernameLink?> = Generators.permutation<AccountData.UsernameLink?> {
      frames += AccountData.UsernameLink(
        entropy = someBytes(32).toByteString(),
        serverId = someBytes(16).toByteString(),
        color = someEnum(AccountData.UsernameLink.Color::class.java, excluding = AccountData.UsernameLink.Color.UNKNOWN)
      )
    }.nullable()
    val usernameLink = some(usernameLinkGenerator)
    val username = some(Generators.usernames())

    val backupsSubscriberDataGenerator: Generator<AccountData.IAPSubscriberData?> = Generators.permutation<AccountData.IAPSubscriberData?> {
      val (purchaseToken, originalTransactionId) = oneOf(
        Generators.nonEmptyStrings() as Generator<Any?>,
        Generators.longs(lower = 1) as Generator<Any?>
      )

      frames += AccountData.IAPSubscriberData(
        subscriberId = someBytes(32).toByteString(),
        purchaseToken = someOneOf(purchaseToken),
        originalTransactionId = someOneOf(originalTransactionId)
      )
    }.nullable()
    val backupTierGenerator: Generator<Long?> = Generators.list(null, 200L, 201L)
    val backupTier = some(backupTierGenerator)
    val candidateBackupsSubscriberData = some(backupsSubscriberDataGenerator)
    val backupsSubscriberData = if (backupTier != 200L) candidateBackupsSubscriberData else null
    val optimizeOnDeviceStorage = someBoolean()

    frames += Frame(
      account = AccountData(
        profileKey = someBytes(32).toByteString(),
        username = if (usernameLink != null) {
          username
        } else {
          null
        },
        svrPin = some(Generators.list("1234", "123456", "abcdefg", "안녕하세요, 세계", "")),
        usernameLink = usernameLink,
        givenName = some(Generators.merge("".asGenerator(), Generators.firstNames())),
        familyName = some(Generators.merge("".asGenerator(), Generators.lastNames())),
        avatarUrlPath = someUrl(),
        donationSubscriberData = someNullablePermutation {
          frames += AccountData.SubscriberData(
            subscriberId = someBytes(32).toByteString(),
            currencyCode = some(Generators.list("USD", "EUR", "GBP")),
            manuallyCancelled = someBoolean()
          )
        },
        accountSettings = AccountData.AccountSettings(
          readReceipts = someBoolean(),
          sealedSenderIndicators = someBoolean(),
          typingIndicators = someBoolean(),
          linkPreviews = someBoolean(),
          notDiscoverableByPhoneNumber = someBoolean(),
          preferContactAvatars = someBoolean(),
          universalExpireTimerSeconds = somePositiveInt(),
          preferredReactionEmoji = listOf(),
          displayBadgesOnProfile = someBoolean(),
          keepMutedChatsArchived = someBoolean(),
          hasSetMyStoriesPrivacy = someBoolean(),
          hasViewedOnboardingStory = someBoolean(),
          storiesDisabled = someBoolean(),
          storyViewReceiptsEnabled = someBoolean(),
          hasSeenGroupStoryEducationSheet = someBoolean(),
          hasCompletedUsernameOnboarding = someBoolean(),
          phoneNumberSharingMode = someEnum(AccountData.PhoneNumberSharingMode::class.java, excluding = AccountData.PhoneNumberSharingMode.UNKNOWN),
          defaultChatStyle = someNullablePermutation {
            frames += ChatStyle(
              wallpaperPreset = someOneOf(wallpaperPresetGenerator),
              wallpaperPhoto = someOneOf(wallpaperPhotoGenerator),
              autoBubbleColor = someOneOf(bubbleAutoGenerator),
              bubbleColorPreset = someOneOf(bubblePresetGenerator),
              customColorId = someOneOf(bubbleCustomGenerator),
              dimWallpaperInDarkMode = someBoolean()
            )
          },
          customChatColors = listOf(
            ChatStyle.CustomChatColor(
              id = 1,
              solid = someColor()
            ),
            ChatStyle.CustomChatColor(
              id = 2,
              solid = someColor()
            ),
            ChatStyle.CustomChatColor(
              id = 3,
              gradient = ChatStyle.Gradient(
                angle = someInt(0, 360),
                colors = listOf(someColor(), someColor()),
                positions = listOf(0f, 1f)
              )
            )
          ),
          // This setting is only available if we are subscribed to Backups.
          optimizeOnDeviceStorage = if (backupTier == 201L) {
            optimizeOnDeviceStorage
          } else {
            false
          },
          backupTier = backupTier
        ),
        backupsSubscriberData = backupsSubscriberData
      )
    )

    frames += StandardFrames.recipientSelf
    frames += StandardFrames.recipientReleaseNotes
    frames += StandardFrames.recipientMyStory
  }
}
