package me.saket.dank.urlparser;

import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonClass;
import com.squareup.moshi.Moshi;

import me.saket.dank.BuildConfig;
import me.saket.dank.utils.Urls;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

@JsonClass(generateAdapter = true, generator = "avm")
@AutoValue
public abstract class ImgurLink extends MediaLink implements Parcelable {

  @NotNull
  public abstract String unparsedUrl();

  @Override
  public abstract Link.Type type();

  @Nullable
  @Override
  public abstract String title();

  @Nullable
  @Override
  public abstract String description();

  @Override
  public abstract String highQualityUrl();

  @Override
  public abstract String lowQualityUrl();

  @Override
  public String cacheKey() {
    return cacheKeyWithClassName(Urls.parseFileNameWithExtension(highQualityUrl()));
  }

  public static ImgurLink create(String unparsedUrl, Type type, @Nullable String title, @Nullable String description,
                                 String highQualityImageUrl, String lowQualityImageUrl) {
    if (BuildConfig.DEBUG && highQualityImageUrl.startsWith("http://")) {
      Timber.e(new Exception("Use https for imgur!"));
    }
    return new AutoValue_ImgurLink(unparsedUrl, type, title, description,
        highQualityImageUrl, lowQualityImageUrl);
  }

  public static JsonAdapter<ImgurLink> jsonAdapter(Moshi moshi) {
    return new AutoValue_ImgurLink.MoshiJsonAdapter(moshi);
  }
}
