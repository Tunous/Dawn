package me.saket.dank.ui.comments

import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import me.saket.dank.ImmediateSchedulersRule
import me.saket.dank.reply.ReplyRepository
import me.saket.dank.ui.submission.SubmissionAndComments
import me.saket.dank.ui.submission.SubmissionCommentTreeUiConstructor
import me.saket.dank.ui.submission.adapter.SubmissionScreenUiModel
import me.saket.dank.utils.Optional
import me.saket.dank.utils.markdown.Markdown
import me.saket.dank.vote.VotingManager
import net.dean.jraw.models.*
import net.dean.jraw.tree.RootCommentNode
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class SubmissionCommentTreeBaseTest {
  @get:Rule
  val schedulersRule: ImmediateSchedulersRule = ImmediateSchedulersRule.create()

  private lateinit var uiConstructor: SubmissionCommentTreeUiConstructor

  @Before
  fun setUp() {
    val replyRepository = mock<ReplyRepository> {
      on { streamPendingSyncReplies(any()) } doReturn Observable.empty()
    }
    val votingManager = mock<VotingManager> {
      on { streamChanges() } doReturn Observable.just(Any())
      on { getPendingOrDefaultVote(any<Comment>(), any()) } doReturn VoteDirection.NONE
      on { getScoreAfterAdjustingPendingVote(any<Comment>()) } doAnswer {
        it.getArgument<Comment>(0).score
      }
    }
    val markdown = mock<Markdown> {
      on { parse(any<Comment>()) } doAnswer {
        it.getArgument<Comment>(0).body
      }
      on { parseAuthorFlair(any()) } doAnswer { it.getArgument(0) }
      on { stripMarkdown(any<Comment>()) } doAnswer {
        it.getArgument<Comment>(0).body
      }
    }
    uiConstructor = SubmissionCommentTreeUiConstructor(
      { replyRepository },
      { votingManager },
      { markdown },
      { mock() }
    )
    SubmissionCommentTreeUiConstructor.COLLAPSED_COMMENT_IDS.clear()
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <T : SubmissionScreenUiModel> TestObserver<List<SubmissionScreenUiModel>>.assertReturnsUiModels(): List<T> {
    assertNoErrors()
    return values().first().map { it as T }
  }

  @Suppress("UNCHECKED_CAST")
  protected fun <T : SubmissionScreenUiModel> TestObserver<List<SubmissionScreenUiModel>>.assertReturnsSingleUiModel(): T {
    return assertReturnsUiModels<T>().single()
  }

  protected fun buildCommentTree(
    submission: Submission,
    vararg comment: Comment
  ): Observable<List<SubmissionScreenUiModel>> {
    val comments = Listing.create<NestedIdentifiable>(null, comment.toList())
    val rootCommentNode = RootCommentNode(submission, comments, null)
    val submissionAndComments = SubmissionAndComments(submission, Optional.of(rootCommentNode))
    return uiConstructor.stream(
      ApplicationProvider.getApplicationContext(),
      Observable.just(submissionAndComments),
      Observable.empty(),
      Schedulers.computation()
    )
  }
}
