package com.mycca.custom.CustomImageSlider.Animations;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

import com.mycca.R;


/**
 * A demo class to show how to use {@link com.mycca.custom.CustomImageSlider.Animations.BaseAnimationInterface}
 * to make  your custom animation in {@link com.mycca.custom.CustomImageSlider.Tricks.ViewPagerEx.PageTransformer} action.
 */
public class DescriptionAnimation implements BaseAnimationInterface {

    @Override
    public void onPrepareCurrentItemLeaveScreen(View current) {
        View descriptionLayout = current.findViewById(R.id.description_layout);
        if (descriptionLayout != null) {
            current.findViewById(R.id.description_layout).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * When next item is coming to show, let's hide the description layout.
     *
     * @param next
     */
    @Override
    public void onPrepareNextItemShowInScreen(View next) {
        View descriptionLayout = next.findViewById(R.id.description_layout);
        if (descriptionLayout != null) {
            next.findViewById(R.id.description_layout).setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onCurrentItemDisappear(View view) {

    }

    /**
     * When next item show in ViewPagerEx, let's make an animation to show the
     * description layout.
     *
     * @param view
     */
    @Override
    public void onNextItemAppear(View view) {

        View descriptionLayout = view.findViewById(R.id.description_layout);
        if (descriptionLayout != null) {
            float layoutY = descriptionLayout.getY();
            view.findViewById(R.id.description_layout).setVisibility(View.VISIBLE);
            ValueAnimator animator = ObjectAnimator.ofFloat(
                    descriptionLayout,
                    "y",
                    layoutY + descriptionLayout.getHeight(),
                    layoutY).setDuration(500);
            animator.start();
        }
    }
}
