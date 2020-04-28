package me.saket.dank.ui.submission

import android.text.Spannable
import me.saket.dank.widgets.TextViewLimitSelection

// This is a no-op span to identify title position in TextView
class SubmissionTitleSpan {
  companion object {
    @JvmStatic fun limitSelectionForTextView(view: TextViewLimitSelection) {
      val title = view.text as Spannable
      title.getSpans(0, title.length, SubmissionTitleSpan::class.java).take(1).forEach {
        view.setSelectionLimits(title.getSpanStart(it), title.getSpanEnd(it))
      }
    }
  }
}
