package com.softteco.roadlabpro.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.util.UIUtils;

public abstract class BaseTabFragment extends AbstractWBFragment {

    private final String TAG = BaseTabFragment.class.getName();

    private FragmentTabHost mTabHost;

    public BaseTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = createView(inflater, container, savedInstanceState);
        mTabHost = getTabHost(mainView);
        if (mTabHost == null) {
            return mainView;
        }
        initTabs(mTabHost);
        for(int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            View view =  mTabHost.getTabWidget().getChildAt(i);
            // Look for the title view to ensure this is an indicator and not a divider.
            TextView tv = (TextView)view.findViewById(android.R.id.title);
            if(tv == null) {
                continue;
            }
            tv.setTextSize(14);
            Drawable d = UIUtils.getTintedDrawable(getResources(),
            R.drawable.apptheme_tab_indicator_holo, R.color.type_issue_blue);
            view.setBackground(d);
        }
        return mainView;
    }

    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new FragmentTabHost(getActivity());
    }

    protected FragmentTabHost getTabHost(View view) {
        mTabHost = (FragmentTabHost) view;
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.road_tab_content);
        return (FragmentTabHost) view;
    }

    public Fragment getCurrentFragment() {
        final Fragment fragment = getChildFragmentManager().findFragmentById(R.id.road_tab_content);
        return fragment;
    }

    protected abstract void initTabs(FragmentTabHost mTabHost);

    protected abstract void checkArgs();

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_road;
    }


    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_ROADS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}
