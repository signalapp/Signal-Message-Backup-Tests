package tests

import Generators
import PermutationScope
import StandardFrames
import TestCase
import asList
import org.thoughtcrime.securesms.backup.v2.proto.ChatFolder
import org.thoughtcrime.securesms.backup.v2.proto.Frame

object ChatFolderTestCase : TestCase("chat_folder") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB

    val memberIds: List<Long> = listOf(
      StandardFrames.recipientAlice,
      StandardFrames.recipientBob,
      StandardFrames.recipientGroupAB
    ).map { it.recipient!!.id }

    frames += Frame(
      chatFolder = ChatFolder(
        name = "",
        showOnlyUnread = false,
        showMutedChats = true,
        includeAllIndividualChats = true,
        includeAllGroupChats = true,
        folderType = ChatFolder.FolderType.ALL,
        includedRecipientIds = emptyList(),
        excludedRecipientIds = emptyList()
      )
    )

    val included = some(Generators.randomizedList(memberIds).asList(0, 1, 2, 3))
    val excluded = some(Generators.randomizedList(memberIds).asList(1, 0, 3, 0)).filter { it !in included }

    frames += Frame(
      chatFolder = ChatFolder(
        name = some(Generators.titles()),
        showOnlyUnread = someBoolean(),
        showMutedChats = someBoolean(),
        includeAllIndividualChats = someBoolean(),
        includeAllGroupChats = someBoolean(),
        folderType = ChatFolder.FolderType.CUSTOM,
        includedRecipientIds = included,
        excludedRecipientIds = excluded
      )
    )
  }
}
