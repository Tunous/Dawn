package me.saket.dank.widgets.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

class CenterAlignedImageSpan(d: Drawable): ImageSpan(d, ALIGN_BASELINE) {
  private var mDrawableRef: WeakReference<Drawable>? = null

  // Reimplemented because this is a private member
  private fun getCachedDrawable(): Drawable? {
    val wr = mDrawableRef
    var d: Drawable? = null

    if (wr != null)
      d = wr.get()

    if (d == null) {
      d = drawable
      mDrawableRef = WeakReference(d)
    }

    return d
  }

  // Reimplemented because getCachedDrawable above uses field from this class
  override fun getSize(paint: Paint?, text: CharSequence?,
                       start: Int, end: Int,
                       fm: FontMetricsInt?): Int {
    val d = getCachedDrawable()
    val rect = d!!.bounds

    if (fm != null) {
      fm.ascent = -rect.bottom
      fm.descent = 0
      fm.top = fm.ascent
      fm.bottom = 0
    }

    return rect.right
  }

  override fun draw(canvas: Canvas, text: CharSequence?,
                    start: Int, end: Int, x: Float,
                    top: Int, y: Int, bottom: Int, paint: Paint) {
    val b = getCachedDrawable()
    canvas.save()

    val fm = paint.fontMetricsInt
    val transY = (fm.bottom - fm.top) / 2 - b!!.bounds.height() / 2

    canvas.translate(x, transY.toFloat())
    b.draw(canvas)
    canvas.restore()
  }
}
