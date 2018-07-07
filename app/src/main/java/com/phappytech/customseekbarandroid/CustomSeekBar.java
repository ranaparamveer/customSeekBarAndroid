package com.phappytech.customseekbarandroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by ranaparamveer.
 * Custom implementation showing a segmented horizontal view.
 */
public class CustomSeekBar extends View {
    private static final String TAG = "CustomSeekBar";
    private int textColor;
    private ArrayList<ProgressSegment> progressSegments = new ArrayList<>();
    private float maxValue;
    private Paint borderPaint;
    private RectF outerRect;
    private Paint[] progressSegmentPaints;
    private RectF[] progressRects;
    private Paint textPaint;
    private float arrowLineLength = 40;
    private float arrowLineWidth = 4;
    private float borderWidth = 4;
    private Paint linePaint;
    private boolean showArrowLine;
    private boolean showText;
    private float textPadding = 10;
    private float textSize = 30;
    private boolean progressInPercent = false;
    private SegmentClickedListener segmentClickedListener;
    private int CLICK_ACTION_THRESHOLD = 200;
    private float startX;
    private float startY;
    private Paint currentProgressPaint;
    private boolean shouldShowProgressOnClick;
    private boolean showingProgress;
    private float currentProgress;
    private int borderColor;
    private int arrowColor;
    private boolean showDummyData;

    public CustomSeekBar(Context context) {
        super(context);
        init();
    }

    public CustomSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomSeekBar, 0, 0);
        textColor = a.getColor(R.styleable.CustomSeekBar_textColor,
                context.getResources().getColor(android.R.color.black));
        textSize = a.getDimension(R.styleable.CustomSeekBar_textSize, textSize);
        textPadding = a.getDimension(R.styleable.CustomSeekBar_textPadding, textPadding);
        showText = a.getBoolean(R.styleable.CustomSeekBar_showText, true);

        borderColor = a.getColor(R.styleable.CustomSeekBar_borderColor,
                context.getResources().getColor(android.R.color.black));
        borderWidth = a.getDimension(R.styleable.CustomSeekBar_borderWidth, borderWidth);

        arrowColor = a.getColor(R.styleable.CustomSeekBar_arrowColor,
                context.getResources().getColor(android.R.color.black));
        arrowLineLength = a.getDimension(R.styleable.CustomSeekBar_arrowLineHeight, arrowLineLength);
        arrowLineWidth = a.getDimension(R.styleable.CustomSeekBar_arrowLineWidth, arrowLineWidth);
        showArrowLine = a.getBoolean(R.styleable.CustomSeekBar_showArrows, true);

        shouldShowProgressOnClick = a.getBoolean(R.styleable.CustomSeekBar_showArrows, false);

        showDummyData = a.getBoolean(R.styleable.CustomSeekBar_showDummyData, false);


        a.recycle();


        init();
    }

    /**
     * Set new data to be drawn on seekbar. Call this method on UI thread only.
     *
     * @param progressSegments ArrayList of ProgressItems to be displayed on view
     */
    public void setProgressSegments(@NonNull ArrayList<ProgressSegment> progressSegments) {
        this.progressSegments = progressSegments;
        if (!progressInPercent) {
            maxValue = 0;
            for (ProgressSegment progressSegment : progressSegments) {
                maxValue += progressSegment.progress;
            }
        }
        setProgressSegments(progressSegments, maxValue);
    }

    /**
     * Set new data to be drawn on seekbar. Call this method on UI thread only.
     *
     * @param progressSegments ArrayList of ProgressItems to be displayed on view
     * @param maxValue         Any float value. If not specified and percentage progress values are used,
     *                         this will be 100.
     *                         If specified and percentage values are used,this will be max available
     *                         percentage
     */
    public void setProgressSegments(@NonNull ArrayList<ProgressSegment> progressSegments, float maxValue) {
        this.progressSegments = progressSegments;
        setMaxValue(maxValue);
        resetArrays();
    }

    private void resetArrays() {
        outerRect = new RectF(0, 0, 0, 0);
        progressSegmentPaints = new Paint[progressSegments.size()];
        progressRects = new RectF[progressSegments.size()];
        for (int i = 0; i < progressSegments.size(); i++) {
            progressSegmentPaints[i] = new Paint();
            progressRects[i] = new RectF();
        }
        invalidate();
    }

    /**
     * MaxValue represents the total value that seekbar represents. For eg. if you need to set progress
     * in terms of actual values rather then percentages,set custom maxvalue.
     *
     * @param maxValue Any float value. If not specified and percentage progress values are used,
     *                 this will be 100.
     */
    private void setMaxValue(float maxValue) {
        if (progressInPercent)
            this.maxValue = 100;
        else
            this.maxValue = maxValue;
    }

    /**
     * Use this method to manage ProgressItems as percent values.
     *
     * @param progressInPercent true if you want to manage views in terms of percentages,
     *                          false otherwise.
     *                          default value is true
     */
    public void setProgressInPercent(boolean progressInPercent) {
        this.progressInPercent = progressInPercent;
    }

    private void init() {
        if (showDummyData)
            setDummyData();
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);

        textPaint = new TextPaint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint();
        linePaint.setColor(arrowColor);
        linePaint.setStrokeWidth(arrowLineWidth);

        currentProgressPaint = new Paint();
    }

    public void setDummyData() {
        ArrayList<ProgressSegment> progressSegments = new ArrayList<>();
        int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.YELLOW};
        for (int i = 0; i < 5; i++) {
            ProgressSegment progressSegment = new ProgressSegment(Parcel.obtain());
            progressSegment.name = "progress" + i;
            progressSegment.progress = (i + 3) * 10;
            progressSegment.color = colors[i];
            progressSegments.add(progressSegment);
        }
        setProgressSegments(progressSegments);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < progressSegments.size(); i++) {
            ProgressSegment progressSegment = progressSegments.get(i);
            progressSegmentPaints[i].setColor(progressSegment.color);
            progressRects[i] = getRect(i);
        }
        if (progressSegments.size() > 0)
            createOuterRect();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void createOuterRect() {
        outerRect.left = progressRects[0].left;
        outerRect.right = progressRects[progressRects.length - 1].right;

        for (int i = 1; i < progressRects.length; i++) {
            outerRect.top = Math.max(outerRect.top,
                    Math.min(progressRects[i - 1].top, progressRects[i].top));
            outerRect.bottom = Math.max(outerRect.bottom,
                    Math.max(progressRects[i - 1].bottom, progressRects[i].bottom));
        }
    }

    private RectF getRect(int i) {
        RectF rectF = progressRects[i];
        if (i > 0) {
            rectF.left = progressRects[i - 1].right;
        } else rectF.left = getPaddingStart() + borderPaint.getStrokeWidth() / 2;
        rectF.top = borderPaint.getStrokeWidth() + getPaddingTop();
        if (showArrowLine)
            rectF.top += arrowLineLength;
        if (showText)
            rectF.top += textPadding + textSize;
        rectF.bottom = rectF.top + 50 - getPaddingBottom();
        rectF.right = (rectF.left + ((getAvailableWidth() * progressSegments.get(i).progress) / maxValue));
        if (i == progressRects.length - 1)
            rectF.right -= getPaddingEnd() + (borderPaint.getStrokeWidth() / 2);
        return rectF;
    }

    private int getAvailableWidth() {
        return getMeasuredWidth() - getPaddingEnd();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        for (int i = 0; i < progressRects.length; i++) {
            canvas.drawRect(progressRects[i], progressSegmentPaints[i]);
            RectF rectF = progressRects[i];
            float horizontalMid = (rectF.right + rectF.left) / 2;
            if (showArrowLine) {
                canvas.drawLine(horizontalMid, (i % 2 == 0) ? rectF.bottom : rectF.top, horizontalMid,
                        (i % 2 == 0) ? rectF.bottom + arrowLineLength : rectF.top - arrowLineLength, linePaint);
            }
            if (showText) {
                float finalTextPadding = (textPadding + (textSize / 2));
                if (showArrowLine)
                    finalTextPadding += arrowLineLength;
                canvas.drawText(progressSegments.get(i).name, horizontalMid,
                        (i % 2 == 0) ? rectF.bottom + finalTextPadding :
                                rectF.top - finalTextPadding + (textSize / 2), textPaint);
            }
        }
        if (showingProgress) {
            Point currentProgressPoint = getCurrentProgressPoint();
            canvas.drawText(getCurrentProgressText(), currentProgressPoint.x, currentProgressPoint.y, currentProgressPaint);
        }
        //Now draw outerRect as border
        canvas.drawRoundRect(outerRect, 10f, 10f, borderPaint);
        super.onDraw(canvas);

    }

    private Point getCurrentProgressPoint() {
        Point point = new Point();
        point.y = (int) (outerRect.top - (textSize / 2));
        point.x = (int) ((getAvailableWidth() * currentProgress) / maxValue);
        return point;
    }

    private String getCurrentProgressText() {
        if (progressInPercent)
            return currentProgress + "%";
        return String.valueOf(currentProgress);
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    /**
     * Set current progress of seekbar.
     *
     * @param currentProgress current value in float. It can be value in percent based on the type
     *                        of the values being displayed.
     */
    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
        invalidate();
    }

    /**
     * Setter to display a current progress text over seekbar whenever a click is observed.
     *
     * @param shouldShowProgressOnClick true to show progress on click.
     *                                  false otherwise.
     *                                  default is false.
     */
    public void setShouldShowProgressOnClick(boolean shouldShowProgressOnClick) {
        this.shouldShowProgressOnClick = shouldShowProgressOnClick;
    }

    public void setSegmentClickedListener(SegmentClickedListener segmentClickedListener) {
        this.segmentClickedListener = segmentClickedListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                if (isAClick(startX, endX, startY, endY)) {
                    int pos = findClickedSegmentPos(event);
                    Log.d(TAG, "pos: " + pos);
                    if (pos >= 0 && segmentClickedListener != null)
                        segmentClickedListener.onClickSegment(pos);
                    if (shouldShowProgressOnClick) {
                        changeCurrentProgressVisibility();
                    }
                }
                break;
        }
        return true;
    }

    private void changeCurrentProgressVisibility() {
        if (!showingProgress) {
            currentProgressPaint.setTextSize(textSize);
            currentProgressPaint.setColor(Color.WHITE);
        }
        showingProgress = !showingProgress;
        invalidate();
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }


    private int findClickedSegmentPos(MotionEvent event) {
        for (int i = 0; i < progressRects.length; i++) {
            if (progressRects[i].contains(event.getX(), event.getY()))
                return i;
        }
        return -1;
    }
}
