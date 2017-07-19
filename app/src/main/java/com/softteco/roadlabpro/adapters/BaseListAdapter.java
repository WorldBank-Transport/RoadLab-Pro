package com.softteco.roadlabpro.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter extends BaseAdapter {

    private Object source;
    protected LayoutInflater layoutInflater;
    private Context context;

    public BaseListAdapter(final Context context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public BaseListAdapter(final Context context, final List list) {
        this(context);
        setSource(list);
    }

    public View inflateView(int res, View convertView, ViewGroup parent) {
        if (convertView == null) {
            return layoutInflater.inflate(res, parent, false);
        }
        return convertView;
    }

    public void setSource(Object source) {
        setSource(source, false);
    }

    public void setSource(Object source, boolean useExistingList) {
        if (useExistingList) {
            List list = getList();
            if (list == null) {
                list = new ArrayList();
            }
            if (source instanceof List) {
                list.clear();
                list.addAll((List) source);
            }
        } else {
            this.source = source;
        }
    }

    public Object getSource() {
        return source;
    }

    public List getList() {
        return (List) getSource();
    }

    @Override
    public int getCount() {
        if (getList() != null) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (getList() != null && position < getList().size()) {
        	return getList().get(position);
        }
    	return null;
    }

    public void clearList() {
    	if (getList() != null) {
    		getList().clear();
    	}
    }

    public Context getContext() {
        return context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }
}
