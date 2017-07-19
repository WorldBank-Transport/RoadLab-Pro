package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.ImagePagerAdapter;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.ui.VoiceRecordPlayer;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.view.CircleImageView;
import com.softteco.roadlabpro.view.TypefaceTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DetailsIssueFragment is an extends of {@link AbstractWBFragment}.
 */
public class TagDetailsFragment extends AbstractWBFragmentWithProjectTitle
    implements VoiceRecordPlayer.VoicePlayerListener, View.OnClickListener {

    public static final String TAG_MODEL_KEY = "TAG_MODEL_KEY";

    private TagModel model;
    private VoiceRecordPlayer voicePlayer;
    private View recordControlsLayout;
    private RelativeLayout recordControlsRelativeLayout;
    private ImageView voiceRecordStop;
    private ImageView voiceRecordPlay;
    private CircleImageView voiceRecordPlayBtn;
    private TextView voiceCommentsText;
    public static TagDetailsFragment newInstance(final BaseModel model) {
        TagDetailsFragment fragment = new TagDetailsFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_MODEL_KEY, model);
        fragment.setArguments(bundle);
        return fragment;
    }

    public TagDetailsFragment() {
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_details_issue;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_TAG_DETAILS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = (TagModel) getArguments().getSerializable(TAG_MODEL_KEY);
        if (model != null) {
            ((TextView) view.findViewById(R.id.fr_details_value_location)).setText(
            String.format("%f%s%f", model.getLatitude(), ", ", model.getLongitude()));
            ((TextView) view.findViewById(R.id.fr_details_value_notes)).setText(model.getNotes());
            TextView roadConditionText = ((TextView) view.findViewById(R.id.fr_details_value_type));
            recordControlsLayout = view.findViewById(R.id.record_controls_layout);
            recordControlsRelativeLayout = (RelativeLayout) view.findViewById(R.id.recordControlsRelativeLayout);
            voiceCommentsText = (TextView) view.findViewById(R.id.fr_details_voice_comments);
            recordControlsRelativeLayout.setVisibility(View.GONE);
            voiceRecordPlay = (ImageView) view.findViewById(R.id.voice_record_play);
            voiceRecordStop = (ImageView) view.findViewById(R.id.voice_record_stop);
            voiceRecordPlayBtn = (CircleImageView) view.findViewById(R.id.dialog_voice_play_record);
            int conditionColor = model.getRoadConditionColor(getContext());
            roadConditionText.setTextColor(conditionColor);
            String conditionStr = TagModel.RoadCondition.getName(model.getRoadCondition());
            roadConditionText.setText(conditionStr);
            Date tagDate = new Date(model.getDate());
            ((TextView) view.findViewById(R.id.fr_details_value_reported)).setText(
              DateUtil.format(tagDate, DateUtil.Format.DDMMYYYHHMM));
            final List<String> urlData = getTagPhotos(model);
            final ViewPager viewPager = (ViewPager) view.findViewById(R.id.fr_details_image_pager);
            viewPager.setAdapter(new ImagePagerAdapter(getMainActivity(), urlData));
            final ImageView imgPrev = (ImageView) view.findViewById(R.id.fr_details_prev_image);
            imgPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curItem = viewPager.getCurrentItem();
                    if (curItem > 0) {
                        curItem--;
                    }
                    viewPager.setCurrentItem(curItem);
                }
            });
            final ImageView imgNext = (ImageView) view.findViewById(R.id.fr_details_next_image);
            imgNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curItem = viewPager.getCurrentItem();
                    if (curItem < urlData.size()) {
                        curItem++;
                    }
                    viewPager.setCurrentItem(curItem);
                }
            });
            final TypefaceTextView txtCountPhotos = (TypefaceTextView) view.findViewById(R.id.fr_details_count_photos);
            final String photos = (urlData.size() > 1) ? getString(R.string.title_pager_plural_photos) : getString(R.string.title_pager_photos);
            txtCountPhotos.setText(urlData.size() + " " + photos);
            if (!TextUtils.isEmpty(model.getAudioFile())) {
                voiceCommentsText.setVisibility(View.VISIBLE);
                recordControlsLayout.setVisibility(View.VISIBLE);
                voiceRecordPlayBtn.setOnClickListener(this);
                voicePlayer = new VoiceRecordPlayer(model.getAudioFile());
                voicePlayer.setListener(this);
            }
        }
    }

    @Override
    protected CharSequence updateTitleAction(FolderModel project, RoadModel road) {
        CharSequence title = super.updateTitleAction(project, road);
        long nextId = getUniqueIdValue();
        title = String.format(Constants.NEW_TAG_SCREN_TITLE_FORMAT, nextId) + title;
        return title;
    }

    private long getUniqueIdValue() {
        long nextId = 0;
        if (model != null) {
            nextId = model.getId();
        } else {
            nextId = MeasurementsDataHelper.getInstance().getTagsNextId();
        }
        return nextId;
    }

    private void setCompletedUI() {
        voiceRecordPlay.setVisibility(View.VISIBLE);
        voiceRecordStop.setVisibility(View.INVISIBLE);
    }

    private void setPlayingUI() {
        voiceRecordPlay.setVisibility(View.INVISIBLE);
        voiceRecordStop.setVisibility(View.VISIBLE);
    }

    private List<String> getTagPhotos(TagModel model) {
        final List<String> urlData = new ArrayList<>();
        if (model == null || model.getImages() == null) {
            return urlData;
        }
        String path = null;
        String fileName = null;
        if (!model.getImages()[0].equals("")) {
            fileName = model.getImages()[0];
            path = FileUtils.getImageFilePathUrl(fileName);
            urlData.add(path);
        }
        if (!model.getImages()[1].equals("")) {
            fileName = model.getImages()[1];
            path = FileUtils.getImageFilePathUrl(fileName);
            urlData.add(path);        }
        if (!model.getImages()[2].equals("")) {
            fileName = model.getImages()[2];
            path = FileUtils.getImageFilePathUrl(fileName);
            urlData.add(path);
        }
        return urlData;
    }

    @Override
    public void onRecord(boolean started) {
    }

    @Override
    public void onPlay(boolean started) {
        if (started) {
            setPlayingUI();
        }
    }

    @Override
    public void onPaused() {
        setCompletedUI();
    }

    @Override
    public void onCompleted() {
        setCompletedUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_voice_play_record:
                if (voicePlayer != null) {
                    voicePlayer.onPlay(true);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (voicePlayer != null) {
            voicePlayer.onStop();
        }
    }
}
