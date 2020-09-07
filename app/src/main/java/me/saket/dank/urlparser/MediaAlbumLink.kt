package me.saket.dank.urlparser

abstract class MediaAlbumLink<out L: MediaLink>: MediaLink() {
  override fun type(): Type = Type.MEDIA_ALBUM
  override fun isGif(): Boolean = false
  override fun highQualityUrl(): String = throw UnsupportedOperationException()
  override fun lowQualityUrl(): String = throw UnsupportedOperationException()

  abstract fun albumTitle(): String?
  abstract fun coverImageUrl(): String?
  abstract fun images(): List<L>?
}
