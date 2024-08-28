@file:Suppress("UNCHECKED_CAST")

package tests

import PermutationScope
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.Frame

object ${TEST_NAME}TestCase : TestCase("${OUTPUT_FILE_NAME}") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES
    
    frames += Frame(
      
    ) 
  }
}