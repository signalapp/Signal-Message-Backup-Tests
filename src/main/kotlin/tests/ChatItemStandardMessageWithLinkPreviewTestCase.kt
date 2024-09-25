package tests

import Generators
import PermutationScope
import TestCase
import map
import nullable
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing messages with standard attachments (i.e. no flags, meaning no voice notes, etc.).
 */
object ChatItemStandardMessageWithLinkPreviewTestCase : TestCase("chat_item_standard_message_with_link_preview") {

  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.chatAlice

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    val url = some(Generators.nonEmptyUrls())

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatAlice.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient!!.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        standardMessage = StandardMessage(
          quote = null,
          text = Text(
            body = some(Generators.textBody().map { "$it $url" })
          ),
          linkPreview = Generators.permutation<LinkPreview> {
            frames += LinkPreview(
              url = url,
              title = some(Generators.titles().nullable()),
              image = some(Generators.linkPreviewFilePointer().nullable()),
              description = some(Generators.textBody().nullable()),
              date = someTimestamp()
            )
          }.map { listOf(it) }.let { some(it) },
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }
}
