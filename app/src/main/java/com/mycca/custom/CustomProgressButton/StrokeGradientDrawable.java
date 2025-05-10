package com.mycca.custom.CustomProgressButton;

import android.graphics.drawable.GradientDrawable;

public class StrokeGradientDrawable {

    private int mStrokeWidth;
    private int mStrokeColor;

    private GradientDrawable mGradientDrawable;

    StrokeGradientDrawable(GradientDrawable drawable) {
        mGradientDrawable = drawable;
    }

    private int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        mGradientDrawable.setStroke(strokeWidth, getStrokeColor());
    }

    private int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        mGradientDrawable.setStroke(getStrokeWidth(), strokeColor);
    }

    public GradientDrawable getGradientDrawable() {
        return mGradientDrawable;
    }
}
