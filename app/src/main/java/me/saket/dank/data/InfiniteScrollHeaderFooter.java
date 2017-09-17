package me.saket.dank.data;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.google.auto.value.AutoValue;

/**
 * Used in RecyclerView adapters that support infinite scrolling.
 */
@AutoValue
@Deprecated
public abstract class InfiniteScrollHeaderFooter {

  public enum Type {
    PROGRESS,
    ERROR,
    HIDDEN,
    CUSTOM
  }

  public abstract Type type();

  @StringRes
  public abstract int titleRes();

  @DrawableRes
  public abstract int otherTypeIconRes();

  @ColorRes
  public abstract int otherTypeTextColor();

  @Nullable
  public abstract View.OnClickListener onClickListener();

  public static InfiniteScrollHeaderFooter createHidden() {
    return new AutoValue_InfiniteScrollHeaderFooter(Type.HIDDEN, 0, 0, 0, null);
  }

  public static InfiniteScrollHeaderFooter createHeaderProgress(@StringRes int progressTitleRes) {
    return new AutoValue_InfiniteScrollHeaderFooter(Type.PROGRESS, progressTitleRes, 0, 0, null);
  }

  public static InfiniteScrollHeaderFooter createFooterProgress() {
    return new AutoValue_InfiniteScrollHeaderFooter(Type.PROGRESS, 0, 0, 0, null);
  }

  public static InfiniteScrollHeaderFooter createError(@StringRes int errorTitleRes, View.OnClickListener onRetryClickListener) {
    return new AutoValue_InfiniteScrollHeaderFooter(Type.ERROR, errorTitleRes, 0, 0, onRetryClickListener);
  }

  public static InfiniteScrollHeaderFooter createCustom(@StringRes int titleRes, View.OnClickListener onClickListener) {
    return new AutoValue_InfiniteScrollHeaderFooter(Type.CUSTOM, titleRes, 0, 0, onClickListener);
  }
}
