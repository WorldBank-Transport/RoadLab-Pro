package com.softteco.roadlabpro.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.activity.MainActivity;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.dao.TagDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.ui.MakePhotoDialog;
import com.softteco.roadlabpro.ui.VoiceRecordDialog;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.BitmapUtil;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.DigitsConverter;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.KeyboardUtils;
import com.softteco.roadlabpro.util.PathUtils;
import com.softteco.roadlabpro.util.StringUtil;
import com.softteco.roadlabpro.util.UIUtils;
import com.softteco.roadlabpro.view.CircleImageView;
import com.softteco.roadlabpro.view.TypefaceTextView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class NewTagFragment extends AbstractWBFragmentWithProjectTitle
    implements GPSDetector.GpsMapListener, View.OnClickListener {

    public static final String ARG_TAG_MODEL = "ARG_TAG_MODEL";
    public static final String TAG = "NewTagFragment";

    private static final int IMAGE_PICK = 1;
    private static final int IMAGE_CAPTURE = 2;

    private int indexPhotoState = -1;
    private int roadCondition = -1;

    private double curAltitude;

    private String[] fileNames = {"", "", ""};
    private String mVoiceCommentPath;
    private String mVoiceCommentTemporalPath;
    private boolean wasTagSaved;
    private String mRoadName;
    private String mFolderName;
    private TextView txtTime;
    private TextView txtDate;
    private TextView txtIri;
    private TypefaceTextView txtGeoValue;
    private EditText txtNotes;
    private TextView selectedConditionText;
    private TextView tagUniqueIdText;
    private View imgPhotoLayoutOne;
    private View imgPhotoLayoutTwo;
    private View imgPhotoLayoutThree;
    private View voiceComment;
    private View voiceCommentAdded;
    private ImageButton getGpsBtn;
    private TextView setPhotoWarning;
    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    private MakePhotoDialog makePhotoDialog;
    private TagModel newTag;
    private TagModel savedTag;
    private long date;
    private long time;
    private int iri;
    private Uri takePhotoUri;
    private volatile boolean locationHasSet;
    private boolean updateMode = false;
    private boolean isEmployeeAccount = false;
    private TagDAO tagDAO;
    private CircleImageView goodConditionButton;
    private CircleImageView fairConditionButton;
    private CircleImageView poorConditionButton;
    private CircleImageView badConditionButton;

    private CircleImageView goodConditionButtonSelected;
    private CircleImageView fairConditionButtonSelected;
    private CircleImageView poorConditionButtonSelected;
    private CircleImageView badConditionButtonSelected;

    public static NewTagFragment newInstance() {
        return newInstance(null);
    }

    public static NewTagFragment newInstance(Bundle args) {
        NewTagFragment fragment = new NewTagFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public NewTagFragment() {
        /**/
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_new_tag;
    }

    @Override
    public int getMenuFragmentResources() {
        return R.menu.abc_menu_send_report;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_REPORT_ISSUE;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagDAO = new TagDAO(getMainActivity());
        checkArgs();
        initPathToDataSave();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_bar_save);
            if (updateMode && item != null) {
                item.setTitle(R.string.update_hint);
            }
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        initializeView(view);
        initializeListenerView();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideKeyboard(getActivity());
        RAApplication.getInstance().getGpsDetector().removeGpsMapListener(this);
        if (!wasTagSaved) {
            if (updateMode && newTag != null && newTag.getAudioFile() != null
             && newTag.getAudioFile().equals(mVoiceCommentPath)) {
                return;
            }
            FileUtils.deleteAudioFile(mVoiceCommentPath);
        }
    }

    private void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_TAG_MODEL) != null) {
            newTag = (TagModel) getArguments().getSerializable(ARG_TAG_MODEL);
            updateMode = true;
        }
    }

    public void initPathToDataSave() {
        RoadDAO roadDAO = new RoadDAO(getActivity());
        FolderDAO folderDAO = new FolderDAO(getActivity());
        long folderId = RAApplication.getInstance().getCurrentFolderId();
        long roadId = RAApplication.getInstance().getCurrentRoadId();
        RoadModel road = roadDAO.getRoad(folderId, roadId);
        FolderModel folder = folderDAO.getFolder(folderId);
        if (road != null) {
            mRoadName = road.getName();
        }
        if (folder != null) {
            mFolderName = folder.getName();
        }
    }

    private void fillViews() {
        if (newTag == null) {
            return;
        }
        if (updateMode && newTag.getRoadCondition() != null) {
            setRoadCondition(newTag.getRoadCondition());
        }
        if (updateMode && !TextUtils.isEmpty(newTag.getAudioFile())) {
            String audioPath = newTag.getAudioFile();
            addRecord(audioPath);
        }
        setGpsText(newTag.getLatitude(), newTag.getLongitude());
        if (newTag.getRoadCondition() != null) {
            roadCondition = newTag.getRoadCondition().getId();
        }
        date = newTag.getTime();
        time = newTag.getDate();
        txtIri.setText(String.valueOf(newTag.getIri()));
        txtDate.setText(DateUtil.format(new Date(date), DateUtil.Format.DDMMYYY));
        txtTime.setText(DateUtil.format(new Date(time), DateUtil.Format.HHMM));
        txtNotes.setText(newTag.getNotes());
        String[] imagesArray = newTag.getImages();
        fileNames = imagesArray;
        if (imagesArray != null) {
            indexPhotoState = 0;
            if (imagesArray.length > 0 && imagesArray.length >= 1) {
                imgPhotoLayoutOne.setVisibility(View.VISIBLE);
                rememberCurImageView(imgPhotoLayoutOne, null);
                UIUtils.setImageFromLink(getImage(imgPhotoLayoutOne), imagesArray[0]);
            }
            if (imagesArray.length > 0 && imagesArray.length >= 2 && !TextUtils.isEmpty(imagesArray[1])) {
                indexPhotoState++;
                imgPhotoLayoutTwo.setVisibility(View.VISIBLE);
                rememberCurImageView(imgPhotoLayoutTwo, null);
                UIUtils.setImageFromLink(getImage(imgPhotoLayoutTwo), imagesArray[1]);
            }
            if (imagesArray.length > 0 && imagesArray.length >= 3 && !TextUtils.isEmpty(imagesArray[2])) {
                indexPhotoState++;
                imgPhotoLayoutThree.setVisibility(View.VISIBLE);
                rememberCurImageView(imgPhotoLayoutThree, null);
                UIUtils.setImageFromLink(getImage(imgPhotoLayoutThree), imagesArray[2]);
            }
            if (!TextUtils.isEmpty(imagesArray[0])) {
                if (TextUtils.isEmpty(imagesArray[1]))  {
                    imgPhotoLayoutTwo.setVisibility(View.VISIBLE);
                } else if (!TextUtils.isEmpty(imagesArray[1]) && TextUtils.isEmpty(imagesArray[2]))  {
                    imgPhotoLayoutThree.setVisibility(View.VISIBLE);
                }
            } else {
                imgPhotoLayoutOne.setVisibility(View.VISIBLE);
                final View curImgDelete = imgPhotoLayoutOne.findViewById(R.id.report_issue_delete_add_photo);
                curImgDelete.setVisibility(View.GONE);
            }
        }
        if (updateMode) {
            txtDate.setEnabled(false);
            txtTime.setEnabled(false);
        }
    }

    private ImageView getImage(final View view) {
        return (ImageView) view.findViewById(R.id.report_issue_picture_add_photo);
    }

    private void initializeView(final View view) {
        final Calendar nowDate = Calendar.getInstance();
        time = date = nowDate.getTimeInMillis();
        goodConditionButton = (CircleImageView) view.findViewById(R.id.fr_new_tag_green_circle);
        fairConditionButton = (CircleImageView) view.findViewById(R.id.fr_new_tag_yellow_circle);
        poorConditionButton = (CircleImageView) view.findViewById(R.id.fr_new_tag_orange_circle);
        badConditionButton = (CircleImageView) view.findViewById(R.id.fr_new_tag_red_circle);

        goodConditionButtonSelected = (CircleImageView) view.findViewById(R.id.fr_new_tag_green_circle_border);
        fairConditionButtonSelected = (CircleImageView) view.findViewById(R.id.fr_new_tag_yellow_circle_border);
        poorConditionButtonSelected = (CircleImageView) view.findViewById(R.id.fr_new_tag_orange_circle_border);
        badConditionButtonSelected = (CircleImageView) view.findViewById(R.id.fr_new_tag_red_circle_border);

        txtIri = (EditText) view.findViewById(R.id.fr_report_add_iri);

        txtDate = (TextView) view.findViewById(R.id.fr_report_value_date);
        txtDate.setText(DateUtil.format(nowDate.getTime(), DateUtil.Format.DDMMYYY));
        txtTime = (TextView) view.findViewById(R.id.fr_report_value_time);
        txtTime.setText(DateUtil.format(nowDate.getTime(), DateUtil.Format.HHMM));
        txtGeoValue = (TypefaceTextView) view.findViewById(R.id.fr_report_value_geo);
        txtNotes = (EditText) view.findViewById(R.id.fr_report_add_notes);
        getGpsBtn = (ImageButton) view.findViewById(R.id.fr_report_choose_location);
        imgPhotoLayoutOne = view.findViewById(R.id.fr_report_issue_add_photo_one);
        imgPhotoLayoutTwo = view.findViewById(R.id.fr_report_issue_add_photo_two);
        imgPhotoLayoutThree = view.findViewById(R.id.fr_report_issue_add_photo_three);
        selectedConditionText = (TextView) view.findViewById(R.id.fr_new_tag_road_selected_condition);
        tagUniqueIdText = (TextView) view.findViewById(R.id.fr_new_tag_road_unique_id);
        imgPhotoLayoutTwo.setVisibility(View.GONE);
        imgPhotoLayoutThree.setVisibility(View.GONE);
        voiceComment = view.findViewById(R.id.add_voice_comment_custom_view);
        voiceCommentAdded = view.findViewById(R.id.add_voice_comment_custom_view_voice_added);
        voiceCommentAdded.setVisibility(View.INVISIBLE);
        setRoadConditionText();
        fillViews();
        setIriTextListener();
        if(updateMode) {
            getGpsBtn.setVisibility(View.GONE);
            setGpsText(newTag.getLatitude(), newTag.getLongitude(), false);
        } else {
        	setGpsLocation(true);
        }
    }

    private void setIriTextListener() {
        if (txtIri == null) {
            return;
        }
        txtIri.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    double iri = DigitsConverter.toDouble(s.toString());
                    setRoadConditionByIri(iri);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initializeListenerView() {
        getGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGpsLocation(true);
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                selectTimePicker();
            }
        });
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                selectDatePicker();
            }
        });

        goodConditionButton.setOnClickListener(this);
        fairConditionButton.setOnClickListener(this);
        poorConditionButton.setOnClickListener(this);
        badConditionButton.setOnClickListener(this);

        imgPhotoLayoutOne.findViewById(R.id.report_issue_picture_add_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!fileNames[0].equals("")) {
                    createDialog(getString(R.string.message_dialog_change_picture),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            }, null);
                } else {
                    indexPhotoState = 0;
                    openMakePhotoDialog();
                }
            }
        });
        imgPhotoLayoutTwo.findViewById(R.id.report_issue_picture_add_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!fileNames[1].equals("")) {
                    createDialog(getString(R.string.message_dialog_change_picture),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            }, null);
                } else {
                    indexPhotoState = 1;
                    openMakePhotoDialog();
                }
            }
        });
        imgPhotoLayoutThree.findViewById(R.id.report_issue_picture_add_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!fileNames[2].equals("")) {
                    createDialog(getString(R.string.message_dialog_change_picture),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            }, null);
                } else {
                    indexPhotoState = 2;
                    openMakePhotoDialog();
                }
            }
        });
        txtNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    saveTag();
                    handled = true;
                }
                return handled;
            }
        });
        voiceComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVoiceRecordDialog();
            }
        });
        voiceCommentAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVoiceCommentPath = "";
                voiceCommentAdded.setVisibility(View.INVISIBLE);
                voiceComment.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fr_new_tag_green_circle:
                setRoadCondition(TagModel.RoadCondition.GOOD);
                break;
            case R.id.fr_new_tag_yellow_circle:
                setRoadCondition(TagModel.RoadCondition.FAIR);
                break;
            case R.id.fr_new_tag_orange_circle:
                setRoadCondition(TagModel.RoadCondition.POOR);
                break;
            case R.id.fr_new_tag_red_circle:
                setRoadCondition(TagModel.RoadCondition.BAD);
                break;
        }
    }

    private void setRoadCondition(TagModel.RoadCondition condition) {
        switch (condition) {
            case GOOD:
                selectRoadCondition(goodConditionButtonSelected, 0);
                break;
            case FAIR:
                selectRoadCondition(fairConditionButtonSelected, 1);
                break;
            case POOR:
                selectRoadCondition(poorConditionButtonSelected, 2);
                break;
            case BAD:
                selectRoadCondition(badConditionButtonSelected, 3);
                break;
        }
        setRoadConditionText(roadCondition);
    }

    private void setRoadConditionByIri(double iri) {
        TagModel.RoadCondition roadCondition = TagModel.RoadCondition.getRoadConditionByIri(iri);
        if (newTag != null) {
            newTag.setRoadCondition(roadCondition);
        }
        setRoadCondition(roadCondition);
    }

    private void selectRoadCondition(CircleImageView clickedItem, int rCondition) {
        removeAllBordersFromRoadConditions();
        clickedItem.setVisibility(View.VISIBLE);
        roadCondition = rCondition;
    }

    private void removeAllBordersFromRoadConditions() {
        goodConditionButtonSelected.setVisibility(View.INVISIBLE);
        fairConditionButtonSelected.setVisibility(View.INVISIBLE);
        poorConditionButtonSelected.setVisibility(View.INVISIBLE);
        badConditionButtonSelected.setVisibility(View.INVISIBLE);
    }

    private void saveTag() {
        if (isCorrectAllData()) {
            saveTagToDB();
        } else {
            createDialog(getString(R.string.message_dialog_no_correct_filled_data),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    }, null);
        }
    }

    private void saveTagToDBAction() {
        final TagModel tagModel = new TagModel();
        tagModel.setFolderId(RAApplication.getInstance().getCurrentFolderId());
        tagModel.setRoadId(RAApplication.getInstance().getCurrentRoadId());
        tagModel.setSingle(!RAApplication.getInstance().isRecordStarted());
        tagModel.setUploaded(false);
        tagModel.setNotes(StringUtil.replaceAllNewLineCharacter(txtNotes.getText().toString()));
        if (!setTagIri(tagModel)) {
            return;
        }
        setVoiceCommentPath(tagModel);
        setTagRoadCondition(tagModel);
        tagModel.setImages(new String[]{fileNames[0], fileNames[1], fileNames[2]});
        tagModel.setTime(time);
        tagModel.setDate(date);
        LatLng loc = getLocation();
        if (loc != null) {
            tagModel.setLatitude(loc.latitude);
            tagModel.setLongitude(loc.longitude);
            tagModel.setAltitude(curAltitude);
        }
        new AsyncTask<TagModel, Void, TagModel>() {
            @Override
            protected TagModel doInBackground(TagModel[] params) {
                long tagId = tagDAO.put(params[0]);
                TagModel tagModel = tagDAO.getItemById(tagId);
                return tagModel;
            }
            @Override
            protected void onPostExecute(TagModel tag) {
                //refreshUiAfterSave(tag);
            }
        }.execute(tagModel);
    }

    private void updateTagToDBAction() {
        newTag.setUploaded(false);
        newTag.setNotes(StringUtil.replaceAllNewLineCharacter(txtNotes.getText().toString()));
        if (!setTagIri(newTag)) {
            return;
        }
        setVoiceCommentPath(newTag);
        setTagRoadCondition(newTag);
        newTag.setImages(new String[]{fileNames[0], fileNames[1], fileNames[2]});
        new AsyncTask<TagModel, Void, Void>() {
            @Override
            protected Void doInBackground(TagModel[] params) {
                tagDAO.updateItem(params[0]);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                //refreshUiAfterSave(newTag);
            }
        }.execute(newTag);
    }

    private void saveTagToDB() {
        if (newTag != null) {
            updateTagToDBAction();
        } else {
            saveTagToDBAction();
        }
        wasTagSaved = true;
        getMainActivity().onBackPressed();
    }

    private LatLng getLocation() {
        double latitude = 0;
        double longitude = 0;
        int indexOf = 0;
        String coordinatesStr = "";
        if (!TextUtils.isEmpty(txtGeoValue.getText())) {
            coordinatesStr = txtGeoValue.getText().toString();
            indexOf = coordinatesStr.indexOf(",");
            latitude = DigitsConverter.toDouble(coordinatesStr.substring(0, indexOf));
            longitude = DigitsConverter.toDouble(coordinatesStr.substring(indexOf + 2));
        }
        LatLng loc = new LatLng(latitude, longitude);
        return loc;
    }

//    private void refreshUiAfterSave(TagModel tag) {
//        fileNames[0] = fileNames[1] = fileNames[2] = "";
//    }

    private void setTagRoadCondition(TagModel tagModel) {
        if (tagModel == null) {
            return;
        }
        TagModel.RoadCondition roadConditionEnum;
        switch (roadCondition) {
            case 0:
                roadConditionEnum = TagModel.RoadCondition.GOOD;
                break;
            case 1:
                roadConditionEnum = TagModel.RoadCondition.FAIR;
                break;
            case 2:
                roadConditionEnum = TagModel.RoadCondition.POOR;
                break;
            case 3:
                roadConditionEnum = TagModel.RoadCondition.BAD;
                break;
            default:
                roadConditionEnum = TagModel.RoadCondition.NONE;
                break;
        }
        tagModel.setRoadCondition(roadConditionEnum);
    }

    private void setRoadConditionText() {
        setRoadConditionText(-1);
    }

    private void setRoadConditionText(int conditionId) {
        TagModel.RoadCondition condition = TagModel.RoadCondition.getRoadQualityById(conditionId);
        if (condition != null) {
            String conditionValue = TagModel.RoadCondition.getName(condition);
            String roadConditionStr = RAApplication.getInstance().getString(R.string.title_fr_new_road_selected_condition, conditionValue);
            selectedConditionText.setVisibility(View.VISIBLE);
            selectedConditionText.setText(roadConditionStr);
        } else {
            selectedConditionText.setVisibility(View.GONE);
        }
    }

    private void setVoiceCommentPath(TagModel tagModel) {
        tagModel.setAudioFile(mVoiceCommentPath);
    }

    private boolean setTagIri(TagModel tagModel) {
        String sIri = !TextUtils.isEmpty(txtIri.getText()) ? txtIri.getText().toString() : "";
        if (sIri.isEmpty()) {
            tagModel.setIri(0.0f);
        } else {
            try {
                tagModel.setIri(Float.parseFloat(StringUtil.replaceAllNewLineCharacter(sIri)));
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(),
                getString(R.string.toast_error_check_entered_data), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //setRoadConditionByIri(tagModel.getIri());
        return true;
    }

    /**
     * The method create new {@link AlertDialog}.
     *
     * @param message for {@link AlertDialog}
     * @param lYes    @see {@link DialogInterface.OnClickListener}
     * @param lNo     @see {@link DialogInterface.OnClickListener}
     */
    private void createDialog(final String message, final DialogInterface.OnClickListener lYes,
                              final DialogInterface.OnClickListener lNo) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (lNo == null) {
            builder.setMessage(message).setPositiveButton(getActivity().getString(R.string.ok), lYes);
        } else {
            builder.setMessage(message)
                    .setPositiveButton(getActivity().getString(R.string.yes), lYes)
                    .setNegativeButton(getActivity().getString(R.string.no),
                            lNo);
        }
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).onSectionAttached(ScreenItems.SCREEN_NEW_TAG);
    }

    @Override
    protected CharSequence updateTitleAction(FolderModel project, RoadModel road) {
        CharSequence title = super.updateTitleAction(project, road);
        long nextId = updateUniqueIdValue();
        title = String.format(Constants.NEW_TAG_SCREN_TITLE_FORMAT, nextId) + title;
        return title;
    }

    private long updateUniqueIdValue() {
        long nextId = 0;
        if (newTag != null) {
            nextId = newTag.getId();
        } else {
            nextId = MeasurementsDataHelper.getInstance().getTagsNextId();
        }
        setUniqueId(nextId);
        return nextId;
    }

    private void setUniqueId(final long id) {
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setUniqueIdAction(id);
            }
        });
    }

    private void setUniqueIdAction(long id) {
        String idValue = String.valueOf(id);
        String uniqueIdStr = RAApplication.getInstance().getString(R.string.title_fr_new_road_unique_id, idValue);
        tagUniqueIdText.setVisibility(View.VISIBLE);
        tagUniqueIdText.setText(Html.fromHtml(uniqueIdStr));
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK:
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            if (!imageFromGallery(uri)) {
                                return;
                            }
                            locationHasSet = false;
                            setGpsLocation();
                        }
                    }
                    break;
                case IMAGE_CAPTURE:
                    if (takePhotoUri != null && !imageFromCamera(takePhotoUri)) {
                        takePhotoUri = null;
                        return;
                    }
                    locationHasSet = false;
                    setGpsLocation();
                    takePhotoUri = null;
                    break;
                default:
                    break;
            }
            if (indexPhotoState == 0) {
                imgPhotoLayoutTwo.setVisibility(View.VISIBLE);
            } else {
                if (indexPhotoState == 1) {
                    imgPhotoLayoutThree.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_save: {
                saveTag();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The method processes an image file made through camera which is located on the #path and update UI.
     */
    private boolean imageFromCamera(final Uri uri) {
        processBitmap(uri);
        return true;
    }

    /**
     * The method processes an image file selected from the gallery and update UI.
     */
    private boolean imageFromGallery(final Uri uri) {
        processBitmap(uri);
        return true;
    }

    private String processPhotoUri(final Uri uri) {
        final String path = PathUtils.getPath(getActivity(), uri);
        int rotate = getOrientationFromExifInterface(uri);
        if (rotate >= 0) {
            BitmapUtil.rotateImage(path, rotate);
        }
        return path;
    }

    private int getOrientationFromExifInterface(Uri uri) {
        try {
            ExifInterface exif = new ExifInterface(uri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_NORMAL:
                    return 0;
                default:
                    return -1;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * The method updates UI.
     *
     * @param bitmap @see {@link Bitmap}
     */
    private void updateUIForPhoto(final Bitmap bitmap) {
        final View curView = getViewFromIndexPhotoState();
        rememberCurImageView(curView, bitmap);
        saveImageFile(bitmap);
    }

    private void processBitmap(final Uri uri) {
        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                String processedImage = processPhotoUri(uri);
                final Bitmap bitmap = BitmapUtil.decodeBitmap(processedImage,
                BitmapUtil.DEFAULT_IMAGE_MAX_SIZE, BitmapUtil.DEFAULT_IMAGE_MAX_SIZE);
                return bitmap;
            }
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    updateUIForPhoto(bitmap);
                } else {
                    Toast.makeText(getMainActivity(), getMainActivity().
                    getString(R.string.file_not_exist), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void saveImageFile(final Bitmap bitmap) {
        new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... params) {
                String filePath = FileUtils.saveImageFile(bitmap);
                return filePath;
            }
            @Override
            protected void onPostExecute(String filePath) {
                fileNames[indexPhotoState] = filePath;
            }
        }.execute();
    }

    private void rememberCurImageView(View view, Bitmap bmp) {
        final ImageView curImg = (ImageView) view.findViewById(R.id.report_issue_picture_add_photo);
        final View curImgDelete = view.findViewById(R.id.report_issue_delete_add_photo);
        if (bmp != null) {
            curImg.setImageBitmap(bmp);
        }
        curImgDelete.setVisibility(View.VISIBLE);
        curImgDelete.setOnClickListener(new View.OnClickListener() {
            private final int index = indexPhotoState;
            @Override
            public void onClick(final View v) {
                curImg.setImageResource(R.drawable.add_new_photo_group);
                curImgDelete.setVisibility(View.GONE);
                FileUtils.deleteImageFile(fileNames[index]);
                fileNames[index] = "";
                if (index == 1) {
                    Log.d(TAG, " 1th image was deleted");
                    if (fileNames[2].equals("")) {
                        imgPhotoLayoutThree.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, " 1th image was deleted, and 2th become 1th");
                        final ImageView imgPhoto = (ImageView) imgPhotoLayoutThree.findViewById(R.id.report_issue_picture_add_photo);
                        final View imgPhotoDelete = imgPhotoLayoutThree.findViewById(R.id.report_issue_delete_add_photo);
                        curImg.setImageDrawable(imgPhoto.getDrawable());
                        curImgDelete.setVisibility(View.VISIBLE);
                        imgPhoto.setImageResource(R.drawable.add_new_photo_group);
                        imgPhotoDelete.setVisibility(View.GONE);
                        //FileUtils.deleteImageFile(fileNames[2]);
                        fileNames[1] = fileNames[2];
                        fileNames[2] = "";
                    }
                } else {
                    if (!fileNames[2].equals("")) {
                        Log.d(TAG, " 0th image was deleted");
                        final ImageView imgPhotoThree = (ImageView) imgPhotoLayoutThree.findViewById(R.id.report_issue_picture_add_photo);
                        final View imgPhotoDeleteThree = imgPhotoLayoutThree.findViewById(R.id.report_issue_delete_add_photo);
                        final ImageView imgPhotoTwo = (ImageView) imgPhotoLayoutTwo.findViewById(R.id.report_issue_picture_add_photo);
                        curImg.setImageDrawable(imgPhotoTwo.getDrawable());
                        curImgDelete.setVisibility(View.VISIBLE);
                        imgPhotoTwo.setImageDrawable(imgPhotoThree.getDrawable());
                        imgPhotoThree.setImageResource(R.drawable.add_new_photo_group);
                        imgPhotoDeleteThree.setVisibility(View.GONE);
                        //FileUtils.deleteImageFile(fileNames[2]);
                        //FileUtils.deleteImageFile(fileNames[1]);
                        fileNames[0] = fileNames[1];
                        fileNames[1] = "";
                        fileNames[1] = fileNames[2];
                        fileNames[2] = "";

                    } else {
                        if (!fileNames[1].equals("")) {
                            if (fileNames[0].equals("")) {
                                Log.d(TAG, " 0 image was deleted, 1th become 0th");
                                imgPhotoLayoutThree.setVisibility(View.GONE);
                                final ImageView imgPhotoTwo = (ImageView) imgPhotoLayoutTwo.findViewById(R.id.report_issue_picture_add_photo);
                                final View imgPhotoDeleteTwo = imgPhotoLayoutTwo.findViewById(R.id.report_issue_delete_add_photo);
                                imgPhotoDeleteTwo.setVisibility(View.GONE);
                                curImg.setImageDrawable(imgPhotoTwo.getDrawable());
                                curImgDelete.setVisibility(View.VISIBLE);
                                imgPhotoTwo.setImageResource(R.drawable.add_new_photo_group);
                                //FileUtils.deleteImageFile(fileNames[1]);
                                fileNames[0] = fileNames[1];
                                fileNames[1] = "";
                            } else {
                                Log.d(TAG, " just 2 image was deleted");
                                fileNames[2] = "";
                            }
                        } else {
                            Log.d(TAG, " 0 image was deleted, all images - null");
                            final ImageView imgPhotoTwo = (ImageView) imgPhotoLayoutTwo.findViewById(R.id.report_issue_picture_add_photo);
                            final View imgPhotoDeleteTwo = imgPhotoLayoutTwo.findViewById(R.id.report_issue_delete_add_photo);
                            imgPhotoTwo.setImageResource(R.drawable.add_new_photo_group);
                            imgPhotoDeleteTwo.setVisibility(View.GONE);
                            imgPhotoLayoutTwo.setVisibility(View.GONE);
                            //FileUtils.deleteImageFile(fileNames[0]);
                            fileNames[0] = "";
                        }
                    }
                }
            }
        });
    }

    private View getViewFromIndexPhotoState() {
        if (indexPhotoState == 2) {
            return imgPhotoLayoutThree;
        } else {
            if (indexPhotoState == 1) {
                return imgPhotoLayoutTwo;
            } else {
                return imgPhotoLayoutOne;
            }
        }
    }

    private void openMakePhotoDialog() {
        if (makePhotoDialog == null) {
            makePhotoDialog = new MakePhotoDialog(getMainActivity(), new MakePhotoDialog.MakePhotoListener() {
                @Override
                public void onRequestOpenCamera() {
                    openCamera();
                }

                @Override
                public void onRequestOpenGallery() {
                    openGallery();
                }
            });
        }
        makePhotoDialog.show();
    }

    /**
     * The method opens the camera ANDROID devices.
     */
    private void openCamera() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final String path = DateUtil.format(Calendar.getInstance().getTime(), DateUtil.Format.DDMMYYYHHMM);
            final File file = new File(Environment.getExternalStorageDirectory(), path);
            takePhotoUri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);
            startActivityForResult(intent, IMAGE_CAPTURE);
        } else {
            //Toast.makeText(getActivity(), getString(R.string.error_no_sd_card), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The method opens the gallery ANDROID devices.
     */
    private void openGallery() {
        final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getActivity().getString(R.string.title_intent_open_gallery)), IMAGE_PICK);
    }

    /**
     * The method shows {@link DatePickerDialog}.
     */
    private void selectDatePicker() {
        final int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        final int month = Calendar.getInstance().get(Calendar.MONTH);
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        if (datePickerDialog == null) {
            datePickerDialog = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(final DatePicker view, final int year,
                                              final int monthOfYear, final int dayOfMonth) {
                            final Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            date = calendar.getTimeInMillis();
                            txtDate.setText(DateUtil.format(calendar.getTime(), DateUtil.Format.DDMMYYY));
                        }
                    }, year, month, day);
        }
        datePickerDialog.show();
    }

    /**
     * The method checks the validity of the data(images, notes, latitude, longitude)
     * entered on the screen.
     *
     * @return if true correct data, false overwhize
     */
    private boolean isCorrectAllData() {
        boolean isIriCorrect = true;
        String sIri = txtIri.getText().toString();
        if(sIri.indexOf('.', sIri.indexOf('.') + 1) != -1) {
            isIriCorrect = false;
            System.out.print("More than one dots at IRI"); //there are two or more dots in this string
        }
        return isIriCorrect;
    }


    /**
     * The method shows {@link TimePickerDialog}.
     */
    private void selectTimePicker() {
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int minute = Calendar.getInstance().get(Calendar.MINUTE);
        if (timePickerDialog == null) {
            timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(final TimePicker view, final int hourOfDay,
                                              final int minute) {
                            final Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            time = calendar.getTimeInMillis();
                            txtTime.setText(DateUtil.format(calendar.getTime(), DateUtil.Format.HHMM));
                        }
                    }, hour, minute, true);
        }
        timePickerDialog.show();
    }

//    private void clearUI() {
//        txtNotes.setText("");
//        txtIri.setText("");
//        final ImageView imgOne = (ImageView) imgPhotoLayoutOne.findViewById(R.id.report_issue_picture_add_photo);
//        final View imgDeleteOne = imgPhotoLayoutOne.findViewById(R.id.report_issue_delete_add_photo);
//        final ImageView imgTwo = (ImageView) imgPhotoLayoutTwo.findViewById(R.id.report_issue_picture_add_photo);
//        final View imgDeleteTwo = imgPhotoLayoutTwo.findViewById(R.id.report_issue_delete_add_photo);
//        final ImageView imgThree = (ImageView) imgPhotoLayoutThree.findViewById(R.id.report_issue_picture_add_photo);
//        final View imgDeleteThree = imgPhotoLayoutThree.findViewById(R.id.report_issue_delete_add_photo);
//
//        imgOne.setImageResource(R.drawable.add_new_photo_group);
//        imgDeleteOne.setVisibility(View.GONE);
//        imgTwo.setImageResource(R.drawable.add_new_photo_group);
//        imgDeleteTwo.setVisibility(View.GONE);
//        imgThree.setImageResource(R.drawable.add_new_photo_group);
//        imgDeleteThree.setVisibility(View.GONE);
//        imgPhotoLayoutTwo.setVisibility(View.GONE);
//        imgPhotoLayoutThree.setVisibility(View.GONE);
//
//        final String[] items = getResources().getStringArray(R.array.fr_report_spinner_items);
//        final Date nowDate = new Date();
//        time = date = nowDate.getTime();
//        txtDate.setText(DateUtil.format(nowDate, DateUtil.Format.DDMMYYY));
//        txtTime.setText(DateUtil.format(nowDate, DateUtil.Format.HHMM));
//        roadCondition = 0;
//        removeAllBordersFromRoadConditions();
//    }

    public void setGpsLocation() {
        setGpsLocation(false);
    }

    public void setGpsLocation(final boolean manual) {
        Location loc = RAApplication.getInstance().getGpsDetector().getLocation();
        if (loc == null) {
            loc = RAApplication.getInstance().getGpsDetector().getLastKnownLocation();
        }
        setGpsLocation(loc, manual);
    }

    public void setGpsLocation(final Location loc, final boolean manual) {
        Log.i(TAG, "setGpsLocation: loc: " + (loc != null ? loc.toString() : "loc == null"));
        if (loc != null) {
            curAltitude = loc.getAltitude();
            if (isAdded() && (manual || !locationHasSet)) {
                setGpsText(loc.getLatitude(), loc.getLongitude());
            }
            if (!manual) {
                locationHasSet = true;
            }
            RAApplication.getInstance().getGpsDetector().removeGpsMapListener(this);
        } else {
            RAApplication.getInstance().getGpsDetector().setGpsMapListener(this);
        }
    }

    private void setGpsText(final double lat, final double lon) {
        setGpsText(lat, lon, true);
    }

    private void setGpsText(final double lat, final double lon, boolean enabled) {
        if (txtGeoValue != null) {
            txtGeoValue.setText(lat + ", " + lon);
            txtGeoValue.setEnabled(enabled);
        }
    }

    @Override
    public void onGpsMapListener(final Location location) {
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setGpsLocation(location, false);
            }
        });
    }

    public void openVoiceRecordDialog() {
        new VoiceRecordDialog(getActivity(),
                getString(R.string.add_new_record),
                FileUtils.getRandomAudioFileName(),
                new VoiceRecordDialog.VoiceRecordDialogListener() {
            @Override
            public void onRequestClose(String path) {
                addRecord(path);
            }
        }).show();
    }

    private void addRecord(String path) {
        voiceComment.setVisibility(View.INVISIBLE);
        voiceCommentAdded.setVisibility(View.VISIBLE);
        mVoiceCommentPath = path;
    }
}
