package me.saket.dank.urlparser;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;

@AutoValue
public abstract class RedgifsUnresolvedLink extends MediaLink implements Parcelable, UnresolvedMediaLink {

  @NotNull
  public abstract String unparsedUrl();

  public abstract String threeWordId();

  @Override
  public Link.Type type() {
    return Link.Type.SINGLE_VIDEO;
  }

  @Override
  public String highQualityUrl() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String lowQualityUrl() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String cacheKey() {
    return cacheKeyWithClassName(threeWordId());
  }

  public static RedgifsUnresolvedLink create(String unparsedUrl, String threeWordId) {
    return new AutoValue_RedgifsUnresolvedLink(unparsedUrl, threeWordId);
  }
}
