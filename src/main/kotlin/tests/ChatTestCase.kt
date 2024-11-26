@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import PermutationScope
import StandardFrames
import TestCase
import oneOf
import org.thoughtcrime.securesms.backup.v2.proto.Chat
import org.thoughtcrime.securesms.backup.v2.proto.ChatStyle
import org.thoughtcrime.securesms.backup.v2.proto.Frame

/**
 * Every reasonable permutation of chats
 */
object ChatTestCase : TestCase("chat") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice

    val (wallpaperPhotoGenerator, wallpaperPresetGenerator) = oneOf(
      Generators.wallpaperFilePointer() as Generator<Any?>,
      Generators.enum(ChatStyle.WallpaperPreset::class.java, ChatStyle.WallpaperPreset.UNKNOWN_WALLPAPER_PRESET) as Generator<Any?>
    )

    val (bubbleAutoGenerator, bubblePresetGenerator, bubbleCustomGenerator) = oneOf(
      Generators.list(ChatStyle.AutomaticBubbleColor()),
      Generators.enum(ChatStyle.BubbleColorPreset::class.java, ChatStyle.BubbleColorPreset.UNKNOWN_BUBBLE_COLOR_PRESET) as Generator<Any?>,
      Generators.list(1L, 2L, 3L)
    )

    frames += Frame(
      chat = Chat(
        id = 3,
        recipientId = StandardFrames.recipientAlice.recipient!!.id,
        archived = someBoolean(),
        pinnedOrder = some(Generators.list(listOf(0, 1))).takeIf { it > 0 },
        expirationTimerMs = someExpirationTimerMs().takeIf { it > 0 },
        muteUntilMs = someTimestamp().takeIf { it > 0 },
        markedUnread = someBoolean(),
        dontNotifyForMentionsIfMuted = someBoolean(),
        style = somePermutation {
          frames += ChatStyle(
            wallpaperPreset = someOneOf(wallpaperPresetGenerator),
            wallpaperPhoto = someOneOf(wallpaperPhotoGenerator),
            autoBubbleColor = someOneOf(bubbleAutoGenerator),
            bubbleColorPreset = someOneOf(bubblePresetGenerator),
            customColorId = someOneOf(bubbleCustomGenerator),
            dimWallpaperInDarkMode = someBoolean()
          )
        },
        expireTimerVersion = someExpirationTimerVersion()
      )
    )
  }
}
