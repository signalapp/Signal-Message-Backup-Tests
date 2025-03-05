@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Every reasonable permutation of chats
 */
object RecipientSelfTestCase : TestCase("recipient_self") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.backupInfo
    frames += StandardFrames.accountData
    frames += StandardFrames.recipientReleaseNotes
    frames += StandardFrames.recipientMyStory

    frames += Frame(
      recipient = Recipient(
        id = 1,
        self = Self(
          avatarColor = someEnum(AvatarColor::class.java)
        )
      )
    )
  }
}
