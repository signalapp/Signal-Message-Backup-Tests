@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import StandardFrames
import TestCase
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.ChatItem
import org.thoughtcrime.securesms.backup.v2.proto.ChatUpdateMessage
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.LearnedProfileChatUpdate

/**
 * Permutations of the [LearnedProfileChatUpdate] message.
 */
object ChatItemLearnedProfileUpdateTestCase : TestCase("chat_item_learned_profile_update") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (e164Generator, usernameGenerator) = oneOf(
      Generators.e164s() as Generator<Any?>,
      Generators.usernames() as Generator<Any?>
    )

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = StandardFrames.recipientSelf.recipient!!.id,
        dateSent = someIncrementingTimestamp(),
        directionless = ChatItem.DirectionlessMessageDetails(),
        updateMessage = ChatUpdateMessage(
          learnedProfileChange = LearnedProfileChatUpdate(
            e164 = someOneOf(e164Generator),
            username = someOneOf(usernameGenerator)
          )
        )
      )
    )
  }
}
