package tests

import Generator
import Generators
import PermutationScope
import SeededRandom
import StandardFrames
import TestCase
import asList
import nullable
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import org.thoughtcrime.securesms.backup.v2.proto.NotificationProfile

object NotificationProfileTestCase : TestCase("notification_profile") {

  /** Notification profiles currently use a specific subset of custom named colors, so limiting to those instead of any color */
  private val colorGenerator = Generators.list(
    0xFFE3E3FE.toInt(),
    0xFFDDE7FC.toInt(),
    0xFFD8E8F0.toInt(),
    0xFFCDE4CD.toInt(),
    0xFFEAE0F8.toInt(),
    0xFFF5E3FE.toInt(),
    0xFFF6D8EC.toInt(),
    0xFFF5D7D7.toInt(),
    0xFFFEF5D0.toInt(),
    0xFFEAE6D5.toInt(),
    0xFFD2D2DC.toInt(),
    0xFFD7D7D9.toInt()
  )

  private val scheduleDaysGenerator = Generators.list(NotificationProfile.DayOfWeek.entries.filter { it != NotificationProfile.DayOfWeek.UNKNOWN })

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB
    frames += StandardFrames.chatGroupAB

    val memberIds: List<Long> = listOf(
      StandardFrames.recipientAlice,
      StandardFrames.recipientBob,
      StandardFrames.recipientGroupAB
    ).map { it.recipient!!.id }

    val allowedMembersGenerator = Generators.list(memberIds).asList(0, 1, 2, 3)

    frames += Frame(
      notificationProfile = NotificationProfile(
        name = some(Generators.titles()),
        emoji = some(Generators.emoji().nullable()),
        color = some(colorGenerator),
        createdAtMs = someTimestamp(),
        allowAllCalls = someBoolean(),
        allowAllMentions = someBoolean(),
        scheduleEnabled = someBoolean(),
        scheduleStartTime = some(ScheduleTimeGenerator()),
        scheduleEndTime = some(ScheduleTimeGenerator()),
        scheduleDaysEnabled = some(scheduleDaysGenerator.asList(0, 1, 2, 3, 4, 5, 6, 7)),
        allowedMembers = some(allowedMembersGenerator)
      )
    )
  }

  private class ScheduleTimeGenerator : Generator<Int> {
    override val minSize: Int = 1
    override fun next(): Int = SeededRandom.int(0..23) * 100 + SeededRandom.int(0..59)
  }
}
