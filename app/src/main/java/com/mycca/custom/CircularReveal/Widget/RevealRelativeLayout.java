package com.mycca.custom.CircularReveal.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mycca.custom.CircularReveal.Animation.RevealViewGroup;
import com.mycca.custom.CircularReveal.Animation.ViewRevealManager;

public class RevealRelativeLayout extends RelativeLayout implements RevealViewGroup {
    private ViewRevealManager manager;

    public RevealRelativeLayout(Context context) {
        this(context, null);
    }

    public RevealRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        manager = new ViewRevealManager();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        try {
            canvas.save();
            manager.transform(canvas, child);
            return super.drawChild(canvas, child, drawingTime);
        } finally {
            canvas.restore();
        }
    }

    public void setViewRevealManager(ViewRevealManager manager) {
        if (manager == null) {
            throw new NullPointerException("ViewRevealManager is null");
        }
        this.manager = manager;
    }

    @Override
    public ViewRevealManager getViewRevealManager() {
        return manager;
    }
}
