package com.mycca.custom.FancyShowCase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by faruktoptas on 09/03/17.
 * Util class for {@link FancyShowCaseView}
 */

class Utils {

    /**
     * Circular reveal animation condition
     *
     * @return true if enabled
     */
    static boolean shouldShowCircularAnimation() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Calculates focus point values
     *
     * @param view               view to focus
     * @param circleRadiusFactor radius factor of circle
     * @return x, y, radius values for the circle
     */
    static int[] calculateFocusPointValues(@Nullable  View view, double circleRadiusFactor, int adjustHeight) {
        int[] point = new int[3];
        if (view != null) {
            int[] viewPoint = new int[2];
            view.getLocationInWindow(viewPoint);

            point[0] = viewPoint[0] + view.getWidth() / 2;
            point[1] = viewPoint[1] + view.getHeight() / 2 - adjustHeight;
            int radius = (int) ((int) (Math.hypot(view.getWidth(), view.getHeight()) / 2) * circleRadiusFactor);
            point[2] = radius;
            return point;
        }
        return null;
    }

    /**
     * Draws focus circle
     *
     * @param bitmap bitmap to draw
     * @param point  circle point
     * @param radius circle radius
     */
    static void drawFocusCircle(@NonNull Bitmap bitmap, @NonNull int[] point, int radius) {
        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Canvas c = new Canvas(bitmap);
        c.drawCircle(point[0], point[1], radius, p);
    }

    /**
     * Returns statusBar height
     *
     * @param context context to access resources
     * @return statusBar height
     */
    static int getStatusBarHeight(@NonNull Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
