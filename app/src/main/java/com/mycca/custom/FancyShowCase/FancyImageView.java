package com.mycca.custom.FancyShowCase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by ftoptas on 13/03/17.
 * ImageView with focus area animation
 */

class FancyImageView extends AppCompatImageView {

    private static final int DEFAULT_ANIM_COUNTER = 20;

    private Bitmap mBitmap;
    private Paint mBackgroundPaint, mErasePaint, mCircleBorderPaint;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mFocusBorderColor = Color.TRANSPARENT;
    private int mFocusBorderSize;
    private int mRoundRectRadius = 20;
    private Calculator mCalculator;
    private int mAnimCounter;
    private int mStep = 1;
    private double mAnimMoveFactor = 1;
    private boolean mAnimationEnabled = true;
    private Path mPath;
    private RectF rectF;
    private int mFocusAnimationMaxValue;
    private int mFocusAnimationStep;

    public FancyImageView(Context context) {
        super(context);
        init();
    }

    public FancyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FancyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializations for background and paints
     */
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setAlpha(0xFF);

        mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mErasePaint.setAlpha(0xFF);
        mErasePaint.setAntiAlias(true);

        mPath = new Path();
        mCircleBorderPaint = new Paint();
        mCircleBorderPaint.setAntiAlias(true);
        mCircleBorderPaint.setColor(mFocusBorderColor);
        mCircleBorderPaint.setStrokeWidth(mFocusBorderSize);
        mCircleBorderPaint.setStyle(Paint.Style.STROKE);

        rectF = new RectF();
    }

    /**
     * Setting parameters for background an animation
     *
     * @param backgroundColor background color
     * @param calculator      calculator object for calculations
     */
    public void setParameters(int backgroundColor, Calculator calculator) {
        mBackgroundColor = backgroundColor;
        mAnimMoveFactor = 1;
        mCalculator = calculator;
    }

    /**
     * Setting parameters for focus border
     *
     * @param focusBorderColor
     * @param focusBorderSize
     */
    public void setBorderParameters(int focusBorderColor, int focusBorderSize) {
        mFocusBorderSize = focusBorderSize;
        mCircleBorderPaint.setColor(focusBorderColor);
        mCircleBorderPaint.setStrokeWidth(focusBorderSize);
    }

    /**
     * Setting round rectangle radius
     *
     * @param roundRectRadius
     */
    public void setRoundRectRadius(int roundRectRadius) {
        mRoundRectRadius = roundRectRadius;
    }

    /**
     * Enable/disable animation
     *
     * @param animationEnabled
     */
    public void setAnimationEnabled(final boolean animationEnabled) {
        mAnimationEnabled = animationEnabled;
        mAnimCounter = mAnimationEnabled ? DEFAULT_ANIM_COUNTER : 0;
    }

    /**
     * Draws background and moving focus area
     *
     * @param canvas draw canvas
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(mBackgroundColor);

        }
        canvas.drawBitmap(mBitmap, 0, 0, mBackgroundPaint);
        if (mCalculator.hasFocus()) {
            if (mCalculator.getFocusShape().equals(FocusShape.CIRCLE)) {
                drawCircle(canvas);
            } else {
                drawRoundedRectangle(canvas);
            }
            if (mAnimationEnabled) {
                if (mAnimCounter == mFocusAnimationMaxValue) {
                    mStep = -1 * mFocusAnimationStep;
                } else if (mAnimCounter == 0) {
                    mStep = mFocusAnimationStep;
                }
                mAnimCounter = mAnimCounter + mStep;
                postInvalidate();
            }
        }
    }

    /**
     * Draws focus circle
     *
     * @param canvas canvas to draw
     */
    private void drawCircle(@NonNull Canvas canvas) {
        canvas.drawCircle(mCalculator.getCircleCenterX(), mCalculator.getCircleCenterY(),
                mCalculator.circleRadius(mAnimCounter, mAnimMoveFactor), mErasePaint);

        if (mFocusBorderSize > 0) {
            mPath.reset();
            mPath.moveTo(mCalculator.getCircleCenterX(), mCalculator.getCircleCenterY());
            mPath.addCircle(mCalculator.getCircleCenterX(), mCalculator.getCircleCenterY(),
                    mCalculator.circleRadius(mAnimCounter, mAnimMoveFactor), Path.Direction.CW);
            canvas.drawPath(mPath, mCircleBorderPaint);
        }
    }

    /**
     * Draws focus rounded rectangle
     *
     * @param canvas canvas to draw
     */
    private void drawRoundedRectangle(@NonNull Canvas canvas) {
        float left = mCalculator.roundRectLeft(mAnimCounter, mAnimMoveFactor);
        float top = mCalculator.roundRectTop(mAnimCounter, mAnimMoveFactor);
        float right = mCalculator.roundRectRight(mAnimCounter, mAnimMoveFactor);
        float bottom = mCalculator.roundRectBottom(mAnimCounter, mAnimMoveFactor);

        rectF.set(left, top, right, bottom);
        canvas.drawRoundRect(rectF, mRoundRectRadius, mRoundRectRadius, mErasePaint);

        if (mFocusBorderSize > 0) {
            mPath.reset();
            mPath.moveTo(mCalculator.getCircleCenterX(), mCalculator.getCircleCenterY());
            mPath.addRoundRect(rectF, mRoundRectRadius, mRoundRectRadius, Path.Direction.CW);
            canvas.drawPath(mPath, mCircleBorderPaint);
        }
    }

    public void setFocusAnimationParameters(int maxValue, int step) {
        mFocusAnimationMaxValue = maxValue;
        mFocusAnimationStep = step;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBitmap!=null && !mBitmap.isRecycled()){
            mBitmap.recycle();
            mBitmap=null;
        }
    }
}
