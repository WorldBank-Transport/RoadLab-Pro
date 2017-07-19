package com.softteco.roadlabpro.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.FontsStorage;

/**
 * Class for custom button (fonts)
 */
public class CustomButton extends Button {

	public CustomButton(final Context context) {
		super(context);
		init(context, null, 0);
	}

	public CustomButton(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public CustomButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
    private void init(final Context context, final AttributeSet attrs, final int defStyle) {
   	    if (!isInEditMode() && attrs != null) {
   	       TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TypefaceTextView, 0, 0);
   	       String customFont = a.getString(R.styleable.TypefaceTextView_font);
   	       if (!TextUtils.isEmpty(customFont)) {
   	    	   setFont(customFont);
   	       }
   	    }
    }
    
    public void setFont(final String font) {
		if (font != null) {
			if (!isInEditMode()) {
				FontsStorage.applyFont(getContext(), font, this);
			}
		}
	}
    
    public void setTextSize(final int size) {
    	setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }
}
