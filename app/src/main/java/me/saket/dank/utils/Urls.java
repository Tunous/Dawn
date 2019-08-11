package me.saket.dank.utils;

import android.net.Uri;

import timber.log.Timber;

import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for URLs.
 */
public class Urls {

  private static final String URL_PARSE_REGEX = "\\/([^\\/]+?)\\/([^\\/]+)\\/?(.+)";

  /**
   * Gets the domain name without any TLD. For example, this will return "nytimes.com" when
   * <var>url</var> is "http://www.nytimes.com/2016/11/30/technology/while...".
   */
  public static String parseDomainName(String url) {
    try {
      // getHost() returns the part between http(s):// and the first slash.
      // So this will contain the TLD and possibly a "www".
      String uriHost = Uri.parse(url).getHost();

      if (uriHost == null) {
        // Uri host is null for emails.
        return url;
      }

      String domainName = uriHost;
      if (domainName.startsWith("www.")) {
        domainName = domainName.substring(4);
      }
      return domainName;

    } catch (Exception e) {
      Timber.e(e, "Error while parsing domain name from URL: %s", url);
      return url;
    }
  }

  public static String parseFileNameWithExtension(String url) {
    Pattern pattern = Pattern.compile(URL_PARSE_REGEX);
    Matcher matcher = pattern.matcher(url);

    String path = Uri.parse(url).getPath();

    while (matcher.find()) {
      if (matcher.group(3).equals("DASHPlaylist.mpd")) {
        return matcher.group(2) + ".mp4";
      }
    }

    return path.substring(path.lastIndexOf('/') + 1);
  }

  public static Optional<String> subdomain(Uri URI) {
    String host = URI.getHost();
    int dotCount = 0;

    if (host == null) {
      // Probably email address.
      return Optional.empty();
    }

    for (int i = 0; i < host.length(); i++) {
      if (host.charAt(i) == '.') {
        ++dotCount;
      }
    }
    boolean hasSubDomain = dotCount > 1;
    if (hasSubDomain) {
      String hostWithoutTold = host.substring(0, host.lastIndexOf('.'));
      String subdomain = hostWithoutTold.substring(0, hostWithoutTold.lastIndexOf('.'));
      return Optional.of(subdomain);
    } else {
      return Optional.empty();
    }
  }
}
