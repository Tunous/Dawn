package me.saket.dank.urlparser;

import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonClass;
import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JsonClass(generateAdapter = true, generator = "avm")
@AutoValue
public abstract class ImgurAlbumLink extends MediaAlbumLink<ImgurLink> implements Parcelable {

  @NotNull
  @Override
  public abstract String unparsedUrl();

  public abstract String albumId();

  public abstract String albumUrl();

  @Nullable
  @Override
  public abstract String albumTitle();

  @Override
  public abstract String coverImageUrl();

  @Override
  public abstract List<ImgurLink> images();

  @Override
  public String cacheKey() {
    return cacheKeyWithClassName(albumId());
  }

  public ImgurAlbumLink withCoverImageUrl(String newCoverImageUrl) {
    return create(albumId(), albumUrl(), albumTitle(), newCoverImageUrl, images());
  }

  public static ImgurAlbumLink create(String albumId, String albumUrl, @Nullable String albumTitle, String coverImageUrl, List<ImgurLink> images) {
    return new AutoValue_ImgurAlbumLink(albumId, albumUrl /* unparsedUrl */, albumUrl, albumTitle, coverImageUrl, images);
  }

  public static JsonAdapter<ImgurAlbumLink> jsonAdapter(Moshi moshi) {
    return new AutoValue_ImgurAlbumLink.MoshiJsonAdapter(moshi);
  }
}
