package me.saket.dank.widgets.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

class CenterAlignedImageSpan(d: Drawable, private val lineSpacingExtra: Int): ImageSpan(d, ALIGN_BASELINE) {
  private var drawableReference: WeakReference<Drawable>? = null

  // Reimplemented because getCachedDrawable above uses field from this class
  override fun getSize(paint: Paint, text: CharSequence?,
                       start: Int, end: Int,
                       fm: FontMetricsInt?): Int {
    val drawable = getCachedDrawable()
    val rect = drawable.bounds

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
    val drawable = getCachedDrawable()
    val drawableHeight = drawable.bounds.height()
    val translationY = (bottom - top - drawableHeight - lineSpacingExtra) / 2f

    canvas.save()
    canvas.translate(x, translationY)
    drawable.draw(canvas)
    canvas.restore()
  }

  // Reimplemented because this is a private member
  private fun getCachedDrawable(): Drawable {
    var cachedDrawable = drawableReference?.get()

    if (cachedDrawable == null) {
      cachedDrawable = drawable
      drawableReference = WeakReference(cachedDrawable)
    }

    return cachedDrawable!!
  }
}
