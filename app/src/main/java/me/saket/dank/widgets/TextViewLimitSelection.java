package me.saket.dank.widgets;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;

public class TextViewLimitSelection extends androidx.appcompat.widget.AppCompatTextView {
  int limitStart = -1;
  int limitEnd = -1;

  public TextViewLimitSelection(Context context) {
    super(context);
  }

  public TextViewLimitSelection(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TextViewLimitSelection(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setSelectionLimits(int start, int end) {
    limitStart = start;
    limitEnd = end;
  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    boolean modified = false, deselect = false;

    if (selStart > -1 && limitStart > -1) {
      if (selStart < limitStart) {
        selStart = limitStart;
        modified = true;
      } else if (selStart > limitEnd) {
        deselect = true;
      }
    }

    if (selEnd > -1 && limitEnd > -1) {
      if (selEnd > limitEnd) {
        selEnd = limitEnd;
        modified = true;
      } else if (selEnd < limitStart) {
        deselect = true;
      }
    }

    if (deselect) {
      selStart = -1; selEnd = -1;
      Selection.removeSelection((Spannable) this.getText());
    } else if (modified) {
      Selection.setSelection((Spannable) this.getText(), selStart, selEnd);
    }

    super.onSelectionChanged(selStart, selEnd);
  }
}
