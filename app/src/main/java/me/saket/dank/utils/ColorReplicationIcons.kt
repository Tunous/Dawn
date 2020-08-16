package me.saket.dank.utils

import android.content.Context
import me.saket.dank.R
import me.saket.dank.widgets.span.CenterAlignedImageSpan
import net.dean.jraw.models.VoteDirection

object ColorReplicationIcons {
  @JvmStatic fun pushIcon(context: Context, builder: Truss, sizeDimenResId: Int, drawableResId: Int, tintColor: Int) {
    val size = context.resources.getDimensionPixelSize(sizeDimenResId)
    val icon = context.resources.getDrawable(drawableResId, null).mutate()
    icon.setTint(tintColor)
    icon.setBounds(0, 0, size, size)

    builder
        .pushSpan(CenterAlignedImageSpan(icon))
        .append("icon")
        .popSpan()
  }

  @JvmStatic fun pushVoteIcon(context: Context, builder: Truss, vote: VoteDirection?, color: Int, iconSizeResId: Int) {
    val icon = when (vote) {
      VoteDirection.DOWN -> R.drawable.ic_arrow_downward_24dp
      VoteDirection.UP -> R.drawable.ic_arrow_upward_24dp
      VoteDirection.NONE, null -> 0
    }

    if (icon != 0)
      pushIcon(context, builder, iconSizeResId, icon, color)
  }
}
