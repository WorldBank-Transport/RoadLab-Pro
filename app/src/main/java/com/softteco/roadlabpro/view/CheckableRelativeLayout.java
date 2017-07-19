package com.softteco.roadlabpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by Aleksey on 06.05.2015.
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    private boolean checked;

    public CheckableRelativeLayout(final Context context) {
        super(context);
    }

    public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRelativeLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(final boolean checked) {
        this.checked = checked;

        refreshDrawableState();
        //Propagate to childs
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof ViewGroup) {
                final int childsofChild = ((ViewGroup) child).getChildCount();
                for (int j = 0; j < childsofChild; j++) {
                    View v = ((ViewGroup) child).getChildAt(j);
                    v.setSelected(checked);
                }
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void toggle() {
        this.checked = !this.checked;
    }
}
