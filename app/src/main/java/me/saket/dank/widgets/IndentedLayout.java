package me.saket.dank.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.f2prateek.rx.preferences2.Preference;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import me.saket.dank.R;
import me.saket.dank.di.Dank;

public class IndentedLayout extends LinearLayout {
  private static final int DEFAULT_SPACING_PER_DEPTH_DP = 10;
  private static final int DEFAULT_LINE_WIDTH = 6;

  private int indentationDepth;
  private final int spacePerDepthPx;
  private Paint indentationLinePaint;
  private int indentationLineWidth;
  private int defaultIndentationLineColor;

  private int originalPaddingStart;

  private static final String[] INDENTATION_COLORS = {
      "#FFB300",    // Vivid Yellow
      "#803E75",    // Strong Purple
      "#FF6800",    // Vivid Orange
      "#A6BDD7",    // Very Light Blue
      "#C10020",    // Vivid Red
      "#CEA262",    // Grayish Yellow
      "#817066",    // Medium Gray
      "#007D34",    // Vivid Green
      "#F6768E",    // Strong Purplish Pink
      "#00538A",    // Strong Blue
      "#FF7A5C",    // Strong Yellowish Pink
      "#53377A",    // Strong Violet
      "#FF8E00",    // Vivid Orange Yellow
      "#B32851",    // Strong Purplish Red
      "#F4C800",    // Vivid Greenish Yellow
      "#7F180D",    // Strong Reddish Brown
      "#93AA00",    // Vivid Yellowish Green
      "#593315",    // Deep Yellowish Brown
      "#F13A13",    // Vivid Reddish Orange
      "#232C16",    // Dark Olive Green
  };

  private List<ColoredTree> trees = new ArrayList<>();

  @Inject @Named("show_colored_comments_tree") Preference<Boolean> coloredDepthPreference;
  private boolean showColoredCommentsTree;

  public IndentedLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    Dank.dependencyInjector().inject(this);

    indentationLinePaint = new Paint();
    originalPaddingStart = getPaddingStart();

    TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.IndentedLayout);
    spacePerDepthPx = attributes.getDimensionPixelSize(R.styleable.IndentedLayout_spacePerDepth, dpToPx(DEFAULT_SPACING_PER_DEPTH_DP, context));
    indentationLineWidth = attributes.getDimensionPixelSize(R.styleable.IndentedLayout_indentationLineWidth, dpToPx(DEFAULT_LINE_WIDTH, context));
    defaultIndentationLineColor = attributes.getColor(R.styleable.IndentedLayout_indentationLineColor, Color.LTGRAY);
    attributes.recycle();

    // Using a Path so that dashes can be rendered
    indentationLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    indentationLinePaint.setStrokeWidth(indentationLineWidth);
    indentationLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    indentationLinePaint.setPathEffect(new DashPathEffect(new float[] { indentationLineWidth * 2, indentationLineWidth * 2 }, 0));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    for (int i = 0; i < indentationDepth; i++) {
      float lineStartX = spacePerDepthPx * (i + 1) + indentationLinePaint.getStrokeWidth();

      Path linePath = new Path();
      linePath.moveTo(lineStartX, 0);
      linePath.lineTo(lineStartX, getHeight());

      trees.add(new ColoredTree((this.showColoredCommentsTree ? Color.parseColor(INDENTATION_COLORS[i]) : defaultIndentationLineColor), linePath));
    }
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);

    for (ColoredTree tree : trees) {
      indentationLinePaint.setColor(tree.color);
      canvas.drawPath(tree.path, indentationLinePaint);
    }
  }

  public void setIndentationDepth(@IntRange(from = 0, to = 1) int depth) {
    indentationDepth = depth;

    this.showColoredCommentsTree = coloredDepthPreference.get();
    trees = new ArrayList<>();

    int indentationSpacing = (int) (indentationDepth * spacePerDepthPx + indentationLinePaint.getStrokeWidth());
    setPaddingRelative(originalPaddingStart + indentationSpacing, getPaddingTop(), getPaddingEnd(), getPaddingBottom());

    invalidate();
  }

  @Px
  public static int dpToPx(float dpValue, Context context) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
  }

  /** For storing each path and its color. */
  private static class ColoredTree {
    public final int color;
    public final Path path;

    public ColoredTree(int color, Path path) {
      this.color = color;
      this.path = path;
    }
  }
}
