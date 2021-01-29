package me.thanel.dawn.linkunfurler

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkMetadata(
  val url: String,
  val title: String?,
  val faviconUrl: String?,
  val imageUrl: String?
) {

  fun hasImage(): Boolean = !imageUrl.isNullOrEmpty()

  fun hasFavicon(): Boolean = !faviconUrl.isNullOrEmpty()
}
