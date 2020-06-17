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
  @Suppress("DEPRECATION")
  fun findNearestFor(preferredWidth: Int, minWidth: Int): String? {
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
          || differenceAbs == closestDifference && variation.width > closestImage.width) {
        closestDifference = preferredWidth - variation.width
        closestImage = variation
      }
    }

    return if (closestImage.width < minWidthChecked) null else {
      // Reddit sends HTML-escaped URLs.
      Html.fromHtml(closestImage.url).toString()
    }
  }

  fun findNearestFor(preferredWidth: Int): String {
    return this.findNearestFor(preferredWidth, -1) ?:
      throw NoSuchElementException("No reddit supplied images present")
  }

  fun findNearestFor(preferredWidth: Int, minWidth: Int, defaultValue: String): String {
    if (UrlParser.isGifUrl(defaultValue)) {
      throw AssertionError("Optimizing GIFs is an error: $defaultValue")
    }

    return this.findNearestFor(preferredWidth, minWidth) ?: defaultValue
  }

  fun findNearestFor(preferredWidth: Int, defaultValue: String): String {
    return findNearestFor(preferredWidth, -1, defaultValue)
  }

  companion object {

    fun of(redditSuppliedImages: SubmissionPreview?): ImageWithMultipleVariants {
      return ImageWithMultipleVariants(Optional.ofNullable(redditSuppliedImages))
    }

    fun of(redditSuppliedImages: Optional<SubmissionPreview>): ImageWithMultipleVariants {
      return ImageWithMultipleVariants(redditSuppliedImages)
    }
  }
}
