package me.saket.dank.utils;

import okhttp3.HttpUrl;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.util.Util;

public enum VideoFormat {
  /** The only independent format. */
  DASH,

  /** By Apple. */
  HLS,

  /** By Microsoft. */
  SMOOTH_STREAMING,

  /** Probably a direct link. */
  OTHER;

  public boolean canBeCached() {
    // Apparently ExoMedia handles caching for DASH, HLS and SMOOTH_STREAMING.
    return this == OTHER;
  }

  @SuppressWarnings("ConstantConditions")
  public static VideoFormat parse(String videoUrl) {
    String fileName = HttpUrl.parse(videoUrl).encodedPath();
    @C.ContentType int type = Util.inferContentType(fileName);

    switch (type) {
      case C.TYPE_DASH:
        return DASH;

      case C.TYPE_SS:
        return SMOOTH_STREAMING;

      case C.TYPE_HLS:
        return HLS;

      case C.TYPE_OTHER:
        return OTHER;

      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }
}
