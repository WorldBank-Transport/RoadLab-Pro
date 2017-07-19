package com.softteco.roadlabpro.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


/**
 * Created by ppp on 08.05.2015.
 */
public class UIUtils {

    public static int dp2px(Context context, int dp) {
        if (context != null) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static Drawable getTintedDrawable(Resources res, int drawableResId, int color) {
        return getTintedDrawable(res, drawableResId, color, true);
    }

    public static Drawable getTintedDrawable(Resources res, int drawableResId, int color, boolean convertResColor) {
        Drawable drawable = res.getDrawable(drawableResId);
        if (convertResColor) {
            color = res.getColor(color);
        }
        return getTintedDrawable(drawable, color);
    }

    public static Drawable getTintedDrawable(Drawable drawable, int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void setImageFromLink(final ImageView imageView, final String url) {
        if (imageView == null) {
            return;
        }
        String filePath = url;
        if (!TextUtils.isEmpty(url) && !URLUtil.isHttpsUrl(url)) {
            if (!filePath.contains(FileUtils.FILE_PREFIX)) {
                filePath = FileUtils.getImageFilePathUrl(url);
            }
        }
        ImageLoader.getInstance().displayImage(filePath, imageView);
    }

    /** color[0] = B
    // color[1] = G
    // color[2] = R
    // color[3] = A
    */
    public static int[] separateColorRgb(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        int[] argb = new int[4];
        argb[0] = b;
        argb[1] = g;
        argb[2] = r;
        argb[3] = a;
        return argb;
    }
}
