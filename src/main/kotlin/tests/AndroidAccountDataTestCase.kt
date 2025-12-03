@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.AccountData
import org.thoughtcrime.securesms.backup.v2.proto.Frame

/**
 * Simplified AccountData test case focused on AndroidSpecificSettings.
 */
object AndroidAccountDataTestCase : TestCase("android_account_data") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.backupInfo

    frames += Frame(
      account = AccountData(
        profileKey = someBytes(32).toByteString(),
        givenName = "Alice",
        familyName = "Smith",
        accountSettings = AccountData.AccountSettings(
          readReceipts = true,
          sealedSenderIndicators = true,
          typingIndicators = true,
          linkPreviews = true,
          notDiscoverableByPhoneNumber = false,
          preferContactAvatars = false,
          universalExpireTimerSeconds = 0,
          preferredReactionEmoji = listOf(),
          displayBadgesOnProfile = true,
          keepMutedChatsArchived = false,
          hasSetMyStoriesPrivacy = true,
          hasViewedOnboardingStory = true,
          storiesDisabled = false,
          storyViewReceiptsEnabled = true,
          hasSeenGroupStoryEducationSheet = true,
          hasCompletedUsernameOnboarding = true,
          pinReminders = false,
          phoneNumberSharingMode = AccountData.PhoneNumberSharingMode.NOBODY,
          defaultSentMediaQuality = AccountData.SentMediaQuality.HIGH,
          appTheme = AccountData.AppTheme.SYSTEM,
          callsUseLessDataSetting = AccountData.CallsUseLessDataSetting.NEVER,
          allowSealedSenderFromAnyone = false,
          autoDownloadSettings = AccountData.AutoDownloadSettings(
            images = AccountData.AutoDownloadSettings.AutoDownloadOption.WIFI_AND_CELLULAR,
            audio = AccountData.AutoDownloadSettings.AutoDownloadOption.WIFI_AND_CELLULAR,
            video = AccountData.AutoDownloadSettings.AutoDownloadOption.WIFI,
            documents = AccountData.AutoDownloadSettings.AutoDownloadOption.WIFI
          )
        ),
        androidSpecificSettings = AccountData.AndroidSpecificSettings(
          useSystemEmoji = someBoolean(),
          screenshotSecurity = someBoolean(),
          navigationBarSize = someEnum(AccountData.AndroidSpecificSettings.NavigationBarSize::class.java, excluding = AccountData.AndroidSpecificSettings.NavigationBarSize.UNKNOWN_BAR_SIZE)
        )
      )
    )

    frames += StandardFrames.recipientSelf
    frames += StandardFrames.recipientReleaseNotes
    frames += StandardFrames.recipientMyStory
  }
}
