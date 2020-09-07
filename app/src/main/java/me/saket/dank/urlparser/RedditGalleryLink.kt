package me.saket.dank.urlparser

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import me.saket.dank.utils.ImageWithMultipleVariants
import me.saket.dank.utils.Optional
import net.dean.jraw.models.Submission
import timber.log.Timber
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

private data class UnknownMimeException(val mime: String?): Exception()

@JsonClass(generateAdapter = true)
@Parcelize
data class RedditGalleryLink(
    val galleryUrl: String,
    val imageList: List<RedditGalleryImageLink>
): MediaAlbumLink<RedditGalleryImageLink>(), Parcelable {

  companion object {
    @JvmStatic fun extractImages(submission: Submission): Sequence<RedditGalleryImageLink>? {
      return submission.galleryData?.items?.asSequence()?.map {
        val id = it.mediaId
        val meta = submission.mediaMetadata?.get(id)

        val (hqUrl, lqUrl, mediaType) = if (meta != null && meta.mime == "image/gif") {
          val (url, mediaType) = meta.full?.mp4Url
              ?.let { u -> Pair(u, Type.SINGLE_VIDEO) }
              ?: Pair("https://i.redd.it/$id.gif", Type.SINGLE_GIF)
          Triple(url, null, mediaType)
        } else {
          val ext = when (meta?.mime) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> throw UnknownMimeException(meta?.mime)
          }
          Triple(
              "https://i.redd.it/$id.$ext",
              meta.previews?.lastOrNull()?.imgUrl,
              Type.SINGLE_IMAGE
          )
        }

        val previews = ImageWithMultipleVariants.of(meta)
        RedditGalleryImageLink(hqUrl, lqUrl, previews, mediaType, it.caption)
      }
    }

    @JvmStatic fun extractFirstImage(submission: Submission): RedditGalleryImageLink? {
      return extractImages(submission)?.firstOrNull()
    }

    @JvmStatic fun create(galleryUrl: String, submission: Submission): Optional<RedditGalleryLink> {
      val images = try {
        extractImages(submission)?.toList()
      } catch (e: UnknownMimeException) {
        Timber.e("Unknown mime type ${e.mime} in reddit gallery $galleryUrl")
        null
      }

      return if (images?.isEmpty() != false) Optional.empty()
      else Optional.of(RedditGalleryLink(galleryUrl, images))
    }
  }

  override fun unparsedUrl(): String = galleryUrl
  override fun cacheKey(): String = cacheKeyWithClassName(galleryUrl)
  override fun albumTitle(): String? = null
  override fun coverImageUrl(): String = imageList[0].let { it.lowQualityUrl ?: it.highQualityUrl() }
  override fun images(): List<RedditGalleryImageLink> = imageList
}

@JsonClass(generateAdapter = true)
@Parcelize
data class RedditGalleryImageLink(
    val highQualityUrl: String,
    val lowQualityUrl: String?,
    val previews: ImageWithMultipleVariants?,
    val mediaType: Type,
    val title: String?
): MediaLink(), Parcelable {
  override fun type(): Type = mediaType
  override fun isGif(): Boolean = mediaType == Type.SINGLE_GIF
  override fun unparsedUrl(): String = highQualityUrl()
  override fun cacheKey(): String = cacheKeyWithClassName(highQualityUrl)
  override fun previewVariants(): ImageWithMultipleVariants? = previews
  override fun lowQualityUrl(): String = lowQualityUrl ?: highQualityUrl
  override fun highQualityUrl(): String = highQualityUrl
  override fun title(): String? = title
}
