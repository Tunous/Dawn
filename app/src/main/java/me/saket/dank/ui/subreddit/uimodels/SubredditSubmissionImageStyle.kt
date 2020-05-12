package me.saket.dank.ui.subreddit.uimodels

import androidx.annotation.StringRes
import me.saket.dank.R

enum class SubredditSubmissionImageStyle {
  NONE,
  THUMBNAIL,
  LARGE,
  FULL_HEIGHT;

  val userVisibleName: Int
    @StringRes get() {
      return when (this) {
        NONE -> R.string.image_style_disabled
        THUMBNAIL -> R.string.image_style_thumbnail
        LARGE -> R.string.image_style_large
        FULL_HEIGHT -> R.string.image_style_full_height
      }
    }
}
