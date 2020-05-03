package me.saket.dank.widgets

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class SelectionLimitingTextView : AppCompatTextView {
  var limitStart = -1
  var limitEnd = -1

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  fun setSelectionLimits(start: Int, end: Int) {
    limitStart = start
    limitEnd = end
  }

  override fun onSelectionChanged(selStart: Int, selEnd: Int) {
    var modified = false
    var deselect = false
    var start = selStart
    var end = selEnd

    if (start > -1 && limitStart > -1) {
      if (start < limitStart) {
        start = limitStart
        modified = true
      } else if (start > limitEnd) {
        deselect = true
      }
    }
    if (end > -1 && limitEnd > -1) {
      if (end > limitEnd) {
        end = limitEnd
        modified = true
      } else if (end < limitStart) {
        deselect = true
      }
    }
    if (deselect) {
      start = -1; end = -1
      Selection.removeSelection(this.text as Spannable)
    } else if (modified) {
      Selection.setSelection(this.text as Spannable, start, end)
    }
    super.onSelectionChanged(start, end)
  }
}
