package com.softteco.roadlabpro.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.softteco.roadlabpro.R;

import java.util.List;

/**
 * Created by ppp on 07.04.2015.
 */
public class BaseFragmentActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void replaceFragment(Fragment fragment, final boolean addToBackStack) {
        final String fragmentName = fragment.getClass().getName();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            ft.addToBackStack(fragmentName);
        }
        ft.replace(getFragmentTransactionReplacementResource(), fragment, fragmentName);
        if (!(this.isDestroyed() || isFinishing())) {
            ft.commitAllowingStateLoss();
        }
    }

    public void clearFragmentBackStack() {
        final FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStackImmediate();
        }
    }

    protected int getFragmentTransactionReplacementResource() {
        return R.id.ac_main_container;
    }

    public void popBackStackToTop() {
        final FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
    }

    public Fragment popBackStackToTopWithFragment() {
        final FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        List<Fragment> fragments = fm.getFragments();
        if (fragments == null
                || fragments.size() == 0
                || fm.getBackStackEntryCount() == 0) {
            return null;
        }
        return fragments.get(fm.getBackStackEntryCount() - 1);
    }
}
