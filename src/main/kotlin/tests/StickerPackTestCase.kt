@file:Suppress("UNCHECKED_CAST")

package tests

import Generators
import PermutationScope
import TestCase
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.StickerPack

/**
 * Some sticker pack variations.
 */
object StickerPackTestCase : TestCase("sticker_pack") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += Frame(
      stickerPack = StickerPack(
        packId = some(Generators.bytes(numBytes = 16, minSize = 3)).toByteString(),
        packKey = some(Generators.bytes(numBytes = 32, minSize = 3)).toByteString()
      )
    )
  }
}
