package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.AppUtil;

/**
 * AboutFragment is an extends of {@link com.softteco.roadlabpro.fragment.AbstractWBFragment}.
 */
public class AboutFragment extends AbstractWBFragment implements View.OnClickListener {

    public AboutFragment() {
        /**/
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = ((TextView) view.findViewById(R.id.about_1));
        TextView desc = ((TextView) view.findViewById(R.id.about_2));
        TextView version = ((TextView) view.findViewById(R.id.fr_about_title_version));
        TextView appBetter = ((TextView) view.findViewById(R.id.fr_about_app_better));
        TextView showTutorial = (TextView) view.findViewById(R.id.fr_about_show_tutorial);
        TextView showManual = (TextView) view.findViewById(R.id.fr_about_show_app_manual);
//      TextView privacy = (TextView) view.findViewById(R.id.fr_about_privacy_policy);
        version.setText(String.format(getString(R.string.title_fr_about_version), ActivityUtil.getVersionApplication(getActivity())));
        showTutorial.setOnClickListener(this);
        showManual.setOnClickListener(this);
//        privacy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//
//            }
//        });
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_about;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_ABOUT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fr_about_show_tutorial:
                AppUtil.showTutorial(getActivity(), false);
                break;
            case R.id.fr_about_show_app_manual:
                replaceFragment(new PdfViewFragment(), true);
                break;
        }
    }
}
