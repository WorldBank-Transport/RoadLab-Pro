package com.softteco.roadlabpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.HelpPagerAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by Vadim Alenin on 3/24/2015.
 */
public class HelpActivity extends Activity implements View.OnClickListener {

    public static final String OPEN_MAIN_SCREEN = "OPEN_MAIN_SCREEN";

    private static final int LAST_POSITION = 7;

    private int pagerNumber = 0;

    private HelpPagerAdapter adapter;
    private ViewPager viewPager;
    private Typeface robotoRegular;
    private Typeface robotoMedium;
    private TextView txtStep;
    private TextView txtTitle;
    private TextView txtDescription;
    private ImageView imgArrowForButton;
    private LinearLayout pageIndicator;
    private ImageButton imgAdd;
    private boolean openMainScreen = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
        initUi();
        getExtras();
    }

    private void getExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            openMainScreen = getIntent().getExtras().getBoolean(OPEN_MAIN_SCREEN);
        }
    }

    private void initUi() {
        final String[] textHelp = getResources().getStringArray(R.array.help_pager_item);
        txtTitle = (TextView) findViewById(R.id.help_lable);
        txtDescription = (TextView) findViewById(R.id.help_description);
        txtStep = (TextView) findViewById(R.id.help_step_lable);
        imgAdd = (ImageButton) findViewById(R.id.button_add);
        robotoRegular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        robotoMedium = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        txtTitle.setTypeface(robotoMedium);
        txtTitle.setText(getString(R.string.help_lable_how));
        txtStep.setText(getString(R.string.help_step, 1));
        txtDescription.setText(textHelp[0]);
        txtStep.setTypeface(robotoRegular);
        txtDescription.setTypeface(robotoRegular);
        viewPager = (ViewPager) findViewById(R.id.ac_help_pager);
        adapter = new HelpPagerAdapter(this);
        viewPager.setAdapter(adapter);
        imgArrowForButton = (ImageView) findViewById(R.id.arrow_two);

        findViewById(R.id.ac_help_bnt_skip).setOnClickListener(this);
        imgAdd.setOnClickListener(this);
        pageIndicator = (LinearLayout) findViewById(R.id.ac_help_indicator);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagerNumber = position;
                refreshText(position, textHelp);
                refreshDots();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void refreshText(int position, String[] textHelp) {
        txtDescription.setText(textHelp[position]);
        imgArrowForButton.setVisibility(position == 1 ? View.VISIBLE : View.INVISIBLE);
        //txtStep.setText(position == LAST_POSITION ? getString(R.string.help_finish) : getString(R.string.help_step, position + 1));
        //txtTitle.setText(position == LAST_POSITION ? getString(R.string.help_label_finish) : getString(R.string.help_lable_how));
        txtStep.setText(getString(R.string.help_step, position + 1));
        txtTitle.setText(getString(R.string.help_lable_how));
    }

    private void refreshDots() {
        for (int i = 0; i < pageIndicator.getChildCount(); i++) {
            ImageView im = (ImageView) pageIndicator.getChildAt(i);
            im.setImageResource(pagerNumber == i ? R.drawable.selected_dot : R.drawable.unselected_dot);
        }
    }

    /**
     * The method launches a main screen and terminate the current screen.
     */
    public void goToNextScreen() {
        PreferencesUtil.getInstance(this).setShowHelp(false);
        if (openMainScreen) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.ac_help_bnt_skip:
                goToNextScreen();
                break;
            case R.id.button_add:
                switch (pagerNumber) {
                    case 1:
                        viewPager.setCurrentItem(++pagerNumber, true);
                        break;
                    case LAST_POSITION:
                        goToNextScreen();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
