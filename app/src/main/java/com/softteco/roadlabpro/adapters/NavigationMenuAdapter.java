package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.softteco.roadlabpro.R;

import com.softteco.roadlabpro.menu.NavMenuItem;

import java.util.List;

/**
 * Created by Aleksey on 06.05.2015.
 */
public class NavigationMenuAdapter extends BaseAdapter {

    private List<NavMenuItem> items;
    private Context context;

    private final Typeface robotoRegular;

    public NavigationMenuAdapter(final Context context, final List<NavMenuItem> items) {
        this.items = items;
        this.context = context;
        this.robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(final int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    public void setNewData(final List<NavMenuItem> navMenuItems) {
        this.items = navMenuItems;
    }

    public static class ViewHolder {
        public ImageView icon;
        private TextView title;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup group) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.navigation_menu_item, group, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) view.findViewById(R.id.menu_row_icon);
            holder.title = (TextView) view.findViewById(R.id.menu_row_text);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        NavMenuItem item = (NavMenuItem) getItem(position);
        holder.title.setText(item.getTitleId());
        if (item.getIconId() != -1) {
            holder.icon.setImageResource(item.getIconId());
        }

        holder.title.setTypeface(robotoRegular);
        return view;
    }

    public List<NavMenuItem> getItems() {
        return items;
    }

}
