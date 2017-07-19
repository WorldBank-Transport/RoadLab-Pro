package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.softteco.roadlabpro.R;

/**
 * Created by Vadim Alenin on 4/22/2015.
 */
public class HelpPagerAdapter extends PagerAdapter {

    private Context context;

    private int[] arrDrawable = new int[]{
            R.drawable.step_one,
            R.drawable.step_two,
            R.drawable.step_three,
            R.drawable.step_four,
            R.drawable.step_five,
            R.drawable.step_six,
            R.drawable.step_seven,
            R.drawable.step_eight
            //,R.drawable.step_final
    };

    public HelpPagerAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrDrawable.length;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.tutoreal_pager_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.help_img);
        imageView.setImageResource(arrDrawable[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }
}
