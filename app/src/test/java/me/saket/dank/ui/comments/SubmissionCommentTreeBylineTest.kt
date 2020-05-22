package me.saket.dank.ui.comments

import com.google.common.truth.Truth.assertThat
import me.saket.dank.ui.submission.SubmissionCommentTreeUiConstructor
import me.saket.dank.ui.submission.adapter.SubmissionRemoteComment
import me.saket.dank.walkthrough.SyntheticComment
import me.saket.dank.walkthrough.SyntheticSubmission
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubmissionCommentTreeBylineTest: SubmissionCommentTreeBaseTest() {

  @Test
  fun `by default byline should consist of author, points, flair and date`() {
    val submission = SyntheticSubmission(commentCount = 1, title = "Submission")
    val comment = SyntheticComment(
      "Comment",
      submission,
      isScoreHidden = false,
      score = 5,
      authorFlair = "Flair"
    )

    val uiModel = buildCommentTree(submission, comment).test()
      .assertReturnsSingleUiModel<SubmissionRemoteComment.UiModel>()

    assertThat(uiModel.byline().toString()).isEqualTo("Dawn \u00b7 5 points \u00b7 Flair \u00b7 Just now")
  }

  @Test
  fun `score should be replaced by marker if it is hidden`() {
    val submission = SyntheticSubmission(commentCount = 1, title = "Submission")
    val comment = SyntheticComment(
      "Comment",
      submission,
      isScoreHidden = true,
      score = 5,
      authorFlair = "Flair"
    )

    val uiModel = buildCommentTree(submission, comment).test()
      .assertReturnsSingleUiModel<SubmissionRemoteComment.UiModel>()

    assertThat(uiModel.byline().toString()).isEqualTo("Dawn \u00b7 [score hidden] \u00b7 Flair \u00b7 Just now")
  }

  @Test
  fun `author flair should be hidden if not present`() {
    val submission = SyntheticSubmission(commentCount = 1, title = "Submission")
    val comment = SyntheticComment(
      "Comment",
      submission,
      isScoreHidden = false,
      score = 1,
      authorFlair = null
    )

    val uiModel = buildCommentTree(submission, comment).test()
      .assertReturnsSingleUiModel<SubmissionRemoteComment.UiModel>()

    assertThat(uiModel.byline().toString()).isEqualTo("Dawn \u00b7 1 point \u00b7 Just now")
  }

  @Test
  fun `when comment is collapsed byline should display author and hidden comment count`() {
    val submission = SyntheticSubmission(commentCount = 1, title = "Submission")
    val replyComment = SyntheticComment("Reply", submission)
    val commentWithReply = SyntheticComment(
      "Comment",
      submission,
      replies = listOf(replyComment),
      isScoreHidden = false,
      score = 5,
      authorFlair = "Flair"
    )
    val commentWithoutReply = SyntheticComment(
      "Comment without reply",
      submission,
      isScoreHidden = false,
      score = 5,
      authorFlair = "Flair"
    )

    SubmissionCommentTreeUiConstructor.COLLAPSED_COMMENT_IDS.collapse(commentWithReply)
    SubmissionCommentTreeUiConstructor.COLLAPSED_COMMENT_IDS.collapse(commentWithoutReply)

    val uiModels = buildCommentTree(submission, commentWithReply, commentWithoutReply).test()
      .assertReturnsUiModels<SubmissionRemoteComment.UiModel>()

    assertThat(uiModels[0].byline().toString()).isEqualTo("Dawn \u00b7 2 hidden")
    assertThat(uiModels[1].byline().toString()).isEqualTo("Dawn \u00b7 1 hidden")
  }
}
