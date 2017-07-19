package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.UIUtils;

import java.util.List;

/**
 * Created by Vadim ALenin on 5/8/2015.
 */
public class ImagePagerAdapter extends PagerAdapter {
    private Context context;

    private List<String> data;
    private Animation animation;

    public ImagePagerAdapter(Context context, List<String> url) {
        this.context = context;
        data = url;
        animation = AnimationUtils.loadAnimation(context, R.anim.repeat_rotate);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //RequestQueueHelper.getInstance(context).getImageLoader().displayImage(data.get(position), imageView);
        UIUtils.setImageFromLink(imageView, data.get(position));
//        if (URLUtil.isHttpsUrl(data.get(position))) {
//
//        } else {
//            final Bitmap bitmap = FileUtils.loadFileFromExternalStorage(context, data.get(position));
//            if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);
//            }
//        }
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
