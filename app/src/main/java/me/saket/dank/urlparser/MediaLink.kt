package me.saket.dank.urlparser

import me.saket.dank.utils.ImageWithMultipleVariants

/**
 * An image, GIF or a video. See implementations.
 */
abstract class MediaLink : Link() {
  abstract fun cacheKey(): String?
  abstract fun highQualityUrl(): String?
  abstract fun lowQualityUrl(): String?
  open fun title(): String? = null
  open fun description(): String? = null

  open fun previewVariants(): ImageWithMultipleVariants? = null

  override fun isGif(): Boolean = UrlParser.isGifUrl(unparsedUrl())

  fun cacheKeyWithClassName(key: String): String {
    val name = javaClass.simpleName
    val nameWithoutAutoValue = if (name.startsWith("AutoValue_")) name.substring("AutoValue_".length) else name
    return nameWithoutAutoValue + "_" + key
  }
}
