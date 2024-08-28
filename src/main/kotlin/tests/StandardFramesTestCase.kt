package tests

import PermutationScope
import TestCase

/**
 * The simplest possible test case, containing only the mandatory frames you need to have a valid backup.
 */
object StandardFramesTestCase : TestCase("standard_frames") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES
  }
}
