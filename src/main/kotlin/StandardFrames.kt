@file:OptIn(ExperimentalStdlibApi::class)

import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.*
import java.util.*

/**
 * Defines a set of standard frames that can be used in various tests.
 * Helpful when you need an arbitrary recipient, chat, etc.
 */
object StandardFrames {
  private val MY_STORY_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
  val SELF_PROFILE_KEY: ByteArray = base64Decode("YQKRq+3DQklInaOaMcmlzZnN0m/1hzLiaONX7gB12dg=")

  val SELF_ACI: ByteArray = UUID(100, 100).toByteArray()
  val SELF_PNI: ByteArray = UUID(101, 101).toByteArray()

  val backupInfo = BackupInfo(
    version = 1,
    backupTimeMs = 1715636551000
  )

  val recipientSelf = Frame(
    recipient = Recipient(
      id = 1,
      self = Self()
    )
  )

  val recipientReleaseNotes = Frame(
    recipient = Recipient(
      id = 2,
      releaseNotes = ReleaseNotes()
    )
  )

  val recipientMyStory = Frame(
    recipient = Recipient(
      id = 3,
      distributionList = DistributionListItem(
        distributionId = MY_STORY_UUID.toByteArray().toByteString(),
        distributionList = DistributionList(
          name = "My Story",
          privacyMode = DistributionList.PrivacyMode.ALL
        )
      )
    )
  )

  val accountData = Frame(
    account = AccountData(
      profileKey = SELF_PROFILE_KEY.toByteString(),
      username = "boba_fett.66",
      usernameLink = AccountData.UsernameLink(
        entropy = base64Decode("ZWdcc9AOsBAF47t8SkfylstlVPeJgSOIFekV2CT9LpM=").toByteString(),
        serverId = base64Decode("YcEBogDVQheJwgUY2El68A==").toByteString(),
        color = AccountData.UsernameLink.Color.OLIVE
      ),
      givenName = "Boba",
      familyName = "Fett",
      avatarUrlPath = "",
      donationSubscriberData = AccountData.SubscriberData(
        subscriberId = base64Decode("7LtoxzQzGi6jM82nR8mMRVNlImFYK0/OWuDeqE3OZRk=").toByteString(),
        currencyCode = "USD",
        manuallyCancelled = true
      ),
      // TODO
//      backupsSubscriberData = AccountData.SubscriberData(
//        subscriberId = base64Decode("7LtoxzQzGi6jM82nR8mMRVNlImFYK0/OWuDeqE3OZRk=").toByteString(),
//        currencyCode = "USD",
//        manuallyCancelled = false
//      ),
      accountSettings = AccountData.AccountSettings(
        readReceipts = true,
        sealedSenderIndicators = true,
        typingIndicators = true,
        linkPreviews = true,
        notDiscoverableByPhoneNumber = true,
        preferContactAvatars = true,
        universalExpireTimerSeconds = 3600,
        preferredReactionEmoji = listOf("a", "b", "c"),
        displayBadgesOnProfile = true,
        keepMutedChatsArchived = true,
        hasSetMyStoriesPrivacy = true,
        hasViewedOnboardingStory = true,
        storiesDisabled = true,
        storyViewReceiptsEnabled = true,
        hasSeenGroupStoryEducationSheet = true,
        hasCompletedUsernameOnboarding = true,
        phoneNumberSharingMode = AccountData.PhoneNumberSharingMode.NOBODY,
        customChatColors = listOf(
          ChatStyle.CustomChatColor(
            id = 1,
            solid = "FF000000".hexToInt()
          ),
          ChatStyle.CustomChatColor(
            id = 2,
            solid = "FFFF0000".hexToInt()
          ),
          ChatStyle.CustomChatColor(
            id = 3,
            solid = "FF00FF00".hexToInt()
          )
        )
      )
    )
  )

  val MANDATORY_FRAMES = listOf(
    backupInfo,
    accountData,
    recipientSelf,
    recipientReleaseNotes,
    recipientMyStory
  )

  val chatSelf = Frame(
    chat = Chat(
      id = 1,
      recipientId = recipientSelf.recipient!!.id
    )
  )

  val recipientAlice = Frame(
    recipient = Recipient(
      id = 4,
      contact = Contact(
        aci = UUID(0, 1).toByteArray().toByteString(),
        pni = UUID(0, 2).toByteArray().toByteString(),
        e164 = 16105550101,
        profileGivenName = "Alice",
        profileFamilyName = "Smith",
        registered = Contact.Registered(),
        profileKey = base64Decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=").toByteString()
      )
    )
  )

  val recipientBob = Frame(
    recipient = Recipient(
      id = 5,
      contact = Contact(
        aci = UUID(0, 3).toByteArray().toByteString(),
        pni = UUID(0, 4).toByteArray().toByteString(),
        e164 = 16105550102,
        profileGivenName = "Bob",
        profileFamilyName = "Jones",
        registered = Contact.Registered(),
        profileKey = base64Decode("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=").toByteString()
      )
    )
  )

  val recipientCarol = Frame(
    recipient = Recipient(
      id = 6,
      contact = Contact(
        aci = UUID(0, 5).toByteArray().toByteString(),
        pni = UUID(0, 6).toByteArray().toByteString(),
        e164 = 16105550103,
        profileGivenName = "Carol",
        profileFamilyName = "Johnson",
        registered = Contact.Registered(),
        profileKey = base64Decode("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=").toByteString()
      )
    )
  )

  val recipientDan = Frame(
    recipient = Recipient(
      id = 7,
      contact = Contact(
        aci = UUID(0, 7).toByteArray().toByteString(),
        pni = UUID(0, 8).toByteArray().toByteString(),
        e164 = 16105550104,
        profileGivenName = "Dan",
        profileFamilyName = "Brown",
        registered = Contact.Registered(),
        profileKey = base64Decode("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD=").toByteString()
      )
    )
  )

  val recipientEve = Frame(
    recipient = Recipient(
      id = 8,
      contact = Contact(
        aci = UUID(0, 9).toByteArray().toByteString(),
        pni = UUID(0, 10).toByteArray().toByteString(),
        e164 = 16105550105,
        profileGivenName = "Eve",
        profileFamilyName = "Green",
        registered = Contact.Registered(),
        profileKey = base64Decode("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE=").toByteString()
      )
    )
  )

  val recipientFrank = Frame(
    recipient = Recipient(
      id = 9,
      contact = Contact(
        aci = UUID(0, 11).toByteArray().toByteString(),
        pni = UUID(0, 12).toByteArray().toByteString(),
        e164 = 16105550106,
        profileGivenName = "Frank",
        profileFamilyName = "Johnson",
        registered = Contact.Registered(),
        profileKey = base64Decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF=").toByteString()
      )
    )
  )

  /** A group with you, Alice, and Bob */
  val recipientGroupAB = Frame(
    recipient = Recipient(
      id = 10,
      group = Group(
        masterKey = ByteArray(32) { 0 }.toByteString(),
        whitelisted = true,
        snapshot = Group.GroupSnapshot(
          title = Group.GroupAttributeBlob(
            title = "Me, Alice, Bob"
          ),
          avatarUrl = "https://example.com/avatar.jpg",
          version = 1,
          members = listOf(
            Group.Member(
              userId = SELF_ACI.toByteString(),
              role = Group.Member.Role.ADMINISTRATOR,
              profileKey = recipientAlice.recipient!!.contact!!.profileKey!!
            ),
            Group.Member(
              userId = recipientAlice.recipient!!.contact!!.aci!!,
              role = Group.Member.Role.DEFAULT,
              profileKey = recipientAlice.recipient!!.contact!!.profileKey!!
            ),
            Group.Member(
              userId = recipientBob.recipient!!.contact!!.aci!!,
              role = Group.Member.Role.DEFAULT,
              profileKey = recipientBob.recipient!!.contact!!.profileKey!!
            )
          )
        )
      )
    )
  )

  val recipientCallLink = Frame(
    recipient = Recipient(
      callLink = CallLink(
        rootKey = ByteArray(16) { 0 }.toByteString(),
        adminKey = ByteArray(32) { 1 }.toByteString(),
        name = "Test Call Link",
        restrictions = CallLink.Restrictions.NONE,
        expirationMs = 0
      )
    )
  )

  val chatAlice = Frame(
    chat = Chat(
      id = 2,
      recipientId = recipientAlice.recipient!!.id
    )
  )

  val chatGroupAB = Frame(
    chat = Chat(
      id = 3,
      recipientId = recipientGroupAB.recipient!!.id
    )
  )
}
