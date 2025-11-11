@file:Suppress("UNCHECKED_CAST")

package tests

import Generator
import Generators
import PermutationScope
import StandardFrames
import TestCase
import org.thoughtcrime.securesms.backup.v2.proto.*

/**
 * Incoming/outgoing single-vote polls
 */
object ChatItemPollSingleVoteTestCase : TestCase("chat_item_poll_single_vote") {
  override fun PermutationScope.execute() {
    frames += StandardFrames.MANDATORY_FRAMES

    frames += StandardFrames.recipientAlice
    frames += StandardFrames.recipientBob
    frames += StandardFrames.recipientGroupAB

    frames += StandardFrames.chatGroupAB

    val (incomingGenerator, outgoingGenerator) = Generators.incomingOutgoingDetails(StandardFrames.recipientAlice.recipient!!)

    val incoming = some(incomingGenerator)
    val outgoing = some(outgoingGenerator)

    frames += Frame(
      chatItem = ChatItem(
        chatId = StandardFrames.chatGroupAB.chat!!.id,
        authorId = if (outgoing != null) {
          StandardFrames.recipientSelf.recipient!!.id
        } else {
          StandardFrames.recipientAlice.recipient.id
        },
        dateSent = someIncrementingTimestamp(),
        incoming = incoming,
        outgoing = outgoing,
        poll = Poll(
          question = someNonEmptyString(),
          allowMultiple = false,
          hasEnded = someBoolean(),
          options = some(singleVotePollOption()),
          reactions = some(Generators.reactions(2, StandardFrames.recipientSelf.recipient!!, StandardFrames.recipientAlice.recipient))
        )
      )
    )
  }

  private fun singleVotePollOption(): Generator<List<Poll.PollOption>> {
    return Generators.list(
      // Two options, no votes
      listOf(
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Two options, one vote on the first
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient!!.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Three options, all votes on the first
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient!!.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient!!.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Three options, one vote on each
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        )
      ),
      // Three options, two votes on one
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Four options, two votes on one option
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Four options, all votes on one option
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Four options, no votes
      listOf(
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Five options, one votes each
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Five options, all votes on one
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Six options, all vote on one option
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Seven options, two votes on one option
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Eight options, one vote on each
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(

            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Nine options, all votes on one
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Ten options, one vote on each
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(

            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Ten options, all votes on one
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            ),
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientAlice.recipient.id,
              voteCount = SeededRandom.int(0)
            ),

            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientBob.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Ten options, one vote
      listOf(
        Poll.PollOption(
          option = SeededRandom.lipsum.getWords(1, 3),
          votes = listOf(
            Poll.PollOption.PollVote(
              voterId = StandardFrames.recipientSelf.recipient.id,
              voteCount = SeededRandom.int(0)
            )
          )
        ),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      ),
      // Ten options, no votes
      listOf(
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf()),
        Poll.PollOption(option = SeededRandom.lipsum.getWords(1, 3), votes = listOf())
      )
    )
  }
}
