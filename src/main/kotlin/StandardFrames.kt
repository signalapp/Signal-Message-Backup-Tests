@file:OptIn(ExperimentalStdlibApi::class)

import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.*
import java.util.*

/**
 * Defines a set of standard frames that can be used in various tests.
 * Helpful when you need an arbitrary recipient, chat, etc.
 */
object StandardFrames {
  val MY_STORY_UUID: ByteString = UUID.fromString("00000000-0000-0000-0000-000000000000").toByteArray().toByteString()

  val SELF_ACI: ByteArray = UUID.fromString("00000000-0000-4000-8000-000000000001").toByteArray()
  val SELF_PNI: ByteArray = UUID.fromString("00000000-0000-4000-8000-000000000002").toByteArray()

  val backupInfo = BackupInfo(
    version = 1,
    backupTimeMs = 1715636551000,
    mediaRootBackupKey = base64Decode("j7o84ZRreKTKDZIp3bBQoScCByMJ1ldK4SRRJp5f27I=").toByteString(),
    currentAppVersion = "FooClient 2.0.0",
    firstAppVersion = "FooClient 1.0.0"
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
        distributionId = MY_STORY_UUID,
        distributionList = DistributionList(
          name = "",
          privacyMode = DistributionList.PrivacyMode.ALL
        )
      )
    )
  )

  val accountData = Frame(
    account = AccountData(
      profileKey = base64Decode("YQKRq+3DQklInaOaMcmlzZnN0m/1hzLiaONX7gB12dg=").toByteString(),
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

  val MANDATORY_FRAMES_WITHOUT_MY_STORY = listOf(
    backupInfo,
    accountData,
    recipientSelf,
    recipientReleaseNotes
  )

  val MANDATORY_FRAMES = MANDATORY_FRAMES_WITHOUT_MY_STORY + listOf(
    recipientMyStory
  )

  val chatSelf = Frame(
    chat = Chat(
      id = 1,
      recipientId = recipientSelf.recipient!!.id,
      expireTimerVersion = 1
    )
  )

  val recipientAlice = Frame(
    recipient = Recipient(
      id = 4,
      contact = Contact(
        aci = UUID.fromString("000a11ce-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("000a11ce-0000-4000-8000-000000000002").toByteArray().toByteString(),
        e164 = 16105550101,
        profileGivenName = "Alice",
        profileFamilyName = "Smith",
        registered = Contact.Registered(),
        // All 1s, since all As results in an all-zero blob.
        profileKey = base64Decode("1111111111111111111111111111111111111111111=").toByteString(),
        identityKey = base64Decode("Bep1hatPbeGROvYFn2m1CtJsVa8neeMb3ljeyLoWXhZO").toByteString(),
        identityState = Contact.IdentityState.DEFAULT
      )
    )
  )

  val recipientBob = Frame(
    recipient = Recipient(
      id = 5,
      contact = Contact(
        aci = UUID.fromString("00000b0b-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("00000b0b-0000-4000-8000-000000000002").toByteArray().toByteString(),
        e164 = 16105550102,
        profileGivenName = "Bob",
        profileFamilyName = "Jones",
        registered = Contact.Registered(),
        profileKey = base64Decode("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=").toByteString(),
        identityKey = base64Decode("BS3ebG3Prw/9y8Xi6skOWhjCaWmQfJQureOg40WUi+J+").toByteString(),
        identityState = Contact.IdentityState.VERIFIED
      )
    )
  )

  val recipientCarol = Frame(
    recipient = Recipient(
      id = 6,
      contact = Contact(
        aci = UUID.fromString("0000ca01-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("0000ca01-0000-4000-8000-000000000002").toByteArray().toByteString(),
        e164 = 16105550103,
        profileGivenName = "Carol",
        profileFamilyName = "Johnson",
        registered = Contact.Registered(),
        profileKey = base64Decode("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=").toByteString(),
        identityKey = base64Decode("BUY3abereji+LL+n6M/343y7MncNdiNwxVz76CNGWT9U").toByteString(),
        identityState = Contact.IdentityState.UNVERIFIED
      )
    )
  )

  val recipientDan = Frame(
    recipient = Recipient(
      id = 7,
      contact = Contact(
        aci = UUID.fromString("000000da-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("000000da-0000-4000-8000-000000000002").toByteArray().toByteString(),
        e164 = 16105550104,
        profileGivenName = "Dan",
        profileFamilyName = "Brown",
        registered = Contact.Registered(),
        profileKey = base64Decode("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD=").toByteString(),
        identityKey = base64Decode("BZ4QBy7jJyEjd+9MRChDtCbqSh8WoCnCPJ0Ci+T1E6l8").toByteString(),
        identityState = Contact.IdentityState.DEFAULT
      )
    )
  )

  val recipientEve = Frame(
    recipient = Recipient(
      id = 8,
      contact = Contact(
        aci = UUID.fromString("000000ee-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("000000ee-0000-4000-8000-000000000002").toByteArray().toByteString(),
        e164 = 16105550105,
        profileGivenName = "Eve",
        profileFamilyName = "Green",
        registered = Contact.Registered(),
        profileKey = base64Decode("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE=").toByteString(),
        identityKey = base64Decode("Bf87/oZKm/EmAlQ/zW+jAxIZ/AMfR0h/8GXLBRujYDk4").toByteString(),
        identityState = Contact.IdentityState.VERIFIED
      )
    )
  )

  val recipientFrank = Frame(
    recipient = Recipient(
      id = 9,
      contact = Contact(
        aci = UUID.fromString("000000fa-0000-4000-8000-000000000001").toByteArray().toByteString(),
        pni = UUID.fromString("000000fa-0000-4000-8000-000000000002").toByteArray().toByteString(),
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
              role = Group.Member.Role.ADMINISTRATOR
            ),
            Group.Member(
              userId = recipientAlice.recipient!!.contact!!.aci!!,
              role = Group.Member.Role.DEFAULT
            ),
            Group.Member(
              userId = recipientBob.recipient!!.contact!!.aci!!,
              role = Group.Member.Role.DEFAULT
            )
          ),
          accessControl = Group.AccessControl(
            attributes = Group.AccessControl.AccessRequired.MEMBER,
            members = Group.AccessControl.AccessRequired.MEMBER,
            addFromInviteLink = Group.AccessControl.AccessRequired.UNSATISFIABLE
          )
        )
      )
    )
  )

  val recipientCallLink = Frame(
    recipient = Recipient(
      id = 11,
      callLink = CallLink(
        rootKey = ByteArray(16) { pos -> pos.toByte() }.toByteString(),
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
      recipientId = recipientAlice.recipient!!.id,
      expireTimerVersion = 1
    )
  )

  val chatGroupAB = Frame(
    chat = Chat(
      id = 3,
      recipientId = recipientGroupAB.recipient!!.id,
      expireTimerVersion = 1
    )
  )

  val chatReleaseNotes = Frame(
    chat = Chat(
      id = 4,
      recipientId = recipientReleaseNotes.recipient!!.id,
      expireTimerVersion = 1
    )
  )
}
