package me.saket.dank.widgets.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

class CenterAlignedImageSpan(drawable: Drawable, private val lineSpacingExtra: Int): ImageSpan(drawable, ALIGN_BASELINE) {

  override fun draw(canvas: Canvas, text: CharSequence?,
                    start: Int, end: Int, x: Float,
                    top: Int, y: Int, bottom: Int, paint: Paint) {
    val drawableHeight = drawable.bounds.height()
    val realExtraSpacing = if (canvas.clipBounds.bottom != bottom) lineSpacingExtra else 0 // extra line spacing is not applied to last line
    val translationY = top + (bottom - top - drawableHeight - realExtraSpacing) / 2f

    canvas.save()
    canvas.translate(x, translationY)
    drawable.draw(canvas)
    canvas.restore()
  }
}
