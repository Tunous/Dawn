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
import io.reactivex.Observable;

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
      "#FFC40D", // Yellow
      "#2D89EF", // Blue
      "#B91D47", // Dark Red
      "#00ABA9", // Teal
      "#E3A21A", // Orange
      "#99B433", // Light Green
      "#7E3878", // Purple
      "#FFB300", // Vivid Yellow
      "#FFFFFF", // White
      "#00A300", // Green
      "#2B5797", // Dark Blue
      "#9F00A7", // Light Purple
      "#603CBA", // Dark Purple
      "#EE1111", // Red
      "#EFF4FF", // Light Blue
      "#DA532C", // Dark Orange
      "#FF0097", // Magenta
      "#1E7145", // Dark Green
  };

  private List<ColoredTree> trees = new ArrayList<>();
  @Inject @Named("show_colored_comments_tree") Preference<Boolean> coloredDepthPreference;

  public IndentedLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    Dank.dependencyInjector().inject(this);

    coloredDepthPreference.asObservable()
        .subscribe(o -> {
          this.coloredDepthPreference.set(o.booleanValue());
          invalidate();
        });

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

      this.trees.get(i).path.moveTo(lineStartX, 0);
      this.trees.get(i).path.lineTo(lineStartX, getHeight());
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

    this.setupDepthLines(depth);

    int indentationSpacing = (int) (indentationDepth * spacePerDepthPx + indentationLinePaint.getStrokeWidth());
    setPaddingRelative(originalPaddingStart + indentationSpacing, getPaddingTop(), getPaddingEnd(), getPaddingBottom());

    invalidate();
  }

  private void setupDepthLines(int depth) {
    trees = new ArrayList<>();

    for (int i = 0; i < depth; i++) {
      Path linePath = new Path();
      trees.add(new ColoredTree((this.coloredDepthPreference.get() ? Color.parseColor(INDENTATION_COLORS[(i > INDENTATION_COLORS.length ? INDENTATION_COLORS.length : i)]) : defaultIndentationLineColor), linePath));
    }
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
