package me.saket.dank.urlparser

import android.os.Parcelable

/**
 * See implementations.
 */
abstract class Link : Parcelable {
  enum class Type {
    /**
     * Submission / user / subreddit.
     */
    REDDIT_PAGE,

    SINGLE_IMAGE,

    SINGLE_GIF,

    SINGLE_VIDEO,

    MEDIA_ALBUM,

    /**
     * A link that will be opened in a browser.
     */
    EXTERNAL
  }

  abstract fun unparsedUrl(): String
  abstract fun type(): Type

  fun isImageOrGif(): Boolean = isImage() || isGif()
  open fun isImage(): Boolean = type() == Type.SINGLE_IMAGE
  open fun isGif(): Boolean = type() == Type.SINGLE_GIF
  open fun isVideo(): Boolean = type() == Type.SINGLE_VIDEO
  open fun isExternal(): Boolean = type() == Type.EXTERNAL
  open fun isRedditPage(): Boolean = type() == Type.REDDIT_PAGE
  open fun isMediaAlbum(): Boolean = type() == Type.MEDIA_ALBUM
}
