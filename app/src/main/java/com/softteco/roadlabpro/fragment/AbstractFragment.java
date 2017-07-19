package com.softteco.roadlabpro.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softteco.roadlabpro.activity.MainActivity;

/**
 * AbstractFragment is an extends of {@link android.support.v4.app.Fragment}.
 */
public abstract class AbstractFragment extends Fragment {

    /**
     * The method returns id layout for the current @see {@link android.support.v4.app.Fragment}.
     *
     * @return id layout
     */
    public abstract int getLayoutFragmentResources();

    /**
     * The method returns id menu for the current @see {@link android.support.v4.app.Fragment}.
     *
     * @return id menu
     */
    public abstract int getMenuFragmentResources();

    /**
     * The method returns type fragment for the current @see {@link android.support.v4.app.Fragment}.
     *
     * @return type fragment
     */
    public abstract int getTypeFragment();

    /**
     * The method returns indicator for navigation drawer for the current @see {@link android.support.v4.app.Fragment}.
     *
     * @return indicator. if true to enable, false to disable
     */
    public abstract boolean isHomeIndicatorMenu();

    /**
     * The method cast @see {@link android.app.Activity}.
     *
     * @return @see {@link com.softteco.roadlabpro.activity.MainActivity}
     * @see {@link com.softteco.roadlabpro.activity.MainActivity}.
     */
    public MainActivity getMainActivity() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        }
        return null;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(getLayoutFragmentResources(), container, false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (getMenuFragmentResources() != -1) {
            inflater.inflate(getMenuFragmentResources(), menu);
        } else {
            menu.clear();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMainActivity().onSectionAttached(getTypeFragment());
        getMainActivity().setHomeIndicator(isHomeIndicatorMenu());
    }
}
