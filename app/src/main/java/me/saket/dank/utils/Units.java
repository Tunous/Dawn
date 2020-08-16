package me.saket.dank.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.Px;

public class Units {

  @Px
  public static int unitToPx(float value, int unit, Context context) {
    return (int) TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
  }

  @Px
  public static int dpToPx(float dpValue, Context context) {
    return unitToPx(dpValue, TypedValue.COMPLEX_UNIT_DIP, context);
  }

  @Px
  public static int spToPx(float spValue, Context context) {
    return unitToPx(spValue, TypedValue.COMPLEX_UNIT_SP, context);
  }
}
