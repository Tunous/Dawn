package me.saket.dank.ui.submission.adapter

import android.text.Html
import me.saket.dank.urlparser.UrlParser
import me.saket.dank.utils.Optional
import net.dean.jraw.models.SubmissionPreview
import java.util.*
import kotlin.math.abs

class ImageWithMultipleVariants private constructor(private val optionalRedditPreviews: Optional<SubmissionPreview>) {

  val isNonEmpty: Boolean
    get() = optionalRedditPreviews.isPresent

  /**
   * Find an image provided by Reddit that is the closest to <var>preferredWidth</var>.
   * Gives preference to higher-res thumbnails if multiple images have the same distance from the preferred width.
   *
   * @param minWidth Minimum preview width.
   * Specify -1 to find any preview. minWidth is ignored if it larger than preferredWidth
   */
  fun findNearestFor(preferredWidth: Int, minWidth: Int): SubmissionPreview.Variation? {
    if (optionalRedditPreviews.isEmpty) {
      return null
    }

    val minWidthChecked = if (minWidth > preferredWidth) -1 else minWidth

    val redditPreviews = optionalRedditPreviews.get().images[0]
    var closestImage: SubmissionPreview.Variation = redditPreviews.source
    var closestDifference = preferredWidth - redditPreviews.source.width

    for (variation in redditPreviews.resolutions) {
      val differenceAbs = abs(preferredWidth - variation.width)
      if (differenceAbs < abs(closestDifference)
        // If another image is found with the same difference, choose the higher-res image.
        || differenceAbs == closestDifference && variation.width > closestImage.width
      ) {
        closestDifference = preferredWidth - variation.width
        closestImage = variation
      }
    }

    return if (closestImage.width < minWidthChecked) null else {
      closestImage
    }
  }

  @Suppress("DEPRECATION")
  fun findNearestUrlFor(preferredWidth: Int, minWidth: Int): String? {
    val url = findNearestFor(preferredWidth, minWidth)?.url
    return if (url != null) {
      // Reddit sends HTML-escaped URLs.
      Html.fromHtml(url).toString()
    } else {
      null
    }
  }

  fun findNearestUrlFor(preferredWidth: Int): String {
    return findNearestUrlFor(preferredWidth, -1) ?:
      throw NoSuchElementException("No reddit supplied images present")
  }

  fun findNearestUrlFor(preferredWidth: Int, minWidth: Int, defaultValue: String): String {
    if (UrlParser.isGifUrl(defaultValue)) {
      throw AssertionError("Optimizing GIFs is an error: $defaultValue")
    }

    return findNearestUrlFor(preferredWidth, minWidth) ?: defaultValue
  }

  fun findNearestUrlFor(preferredWidth: Int, defaultValue: String): String {
    return findNearestUrlFor(preferredWidth, -1, defaultValue)
  }

  companion object {

    const val DEFAULT_VIEWER_MIN_WIDTH = 1200

    fun of(redditSuppliedImages: SubmissionPreview?): ImageWithMultipleVariants {
      return ImageWithMultipleVariants(Optional.ofNullable(redditSuppliedImages))
    }

    fun of(redditSuppliedImages: Optional<SubmissionPreview>): ImageWithMultipleVariants {
      return ImageWithMultipleVariants(redditSuppliedImages)
    }
  }
}
