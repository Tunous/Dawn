package me.saket.dank.urlparser;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;

@AutoValue
public abstract class RedditUserLink extends RedditLink implements Parcelable {

  @NotNull
  @Override
  public abstract String unparsedUrl();

  public abstract String name();

  @Override
  public RedditLinkType redditLinkType() {
    return RedditLinkType.USER;
  }

  public static RedditUserLink create(String unparsedUrl, String userName) {
    return new AutoValue_RedditUserLink(unparsedUrl, userName);
  }
}
