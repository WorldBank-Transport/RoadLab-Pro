package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.util.FileUtils;

public class PopupMeasurementMapFragment
    extends BaseMeasurementMapFragment implements GoogleMap.OnMapClickListener, View.OnClickListener {

    private RelativeLayout popupLayout;
    private ImageView popupImage;
    private TextView popupName;
    private TextView popupDescription;
    private MeasurementItem selectedItem;

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        popupLayout = (RelativeLayout) view.findViewById(R.id.popup_layout);
        popupImage = (ImageView) view.findViewById(R.id.popup_image);
        popupName = (TextView) view.findViewById(R.id.popup_name);
        popupDescription = (TextView) view.findViewById(R.id.popup_description);
    }

    @Override
    protected void initMap(GoogleMap map) {
        super.initMap(map);
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_map_with_popup;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MeasurementItem item = getMapHelper().getItemByMarker(marker);
        showPopup(item, true);
        return true;
    }

    public void showPopup(MeasurementItem item, boolean show) {
        if (popupLayout != null) {
            popupLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                popupLayout.setOnClickListener(this);
            } else {
                popupLayout.setOnClickListener(null);
            }
        }
        if (show && item != null) {
            selectedItem = item;
            MeasurementItemType type = item.getType();
            if (type == null) {
                type = MeasurementItemType.BUMP;
            }
            if (popupImage != null) {
                int iconResId = getPopupIconResId(item);
                popupImage.setImageResource(iconResId);
            }
            if (popupName != null) {
                String title = getTitle(item);
                popupName.setText(type.name() +
                (TextUtils.isEmpty(title) ? "" : ": " + title));
            }
            if (popupDescription != null) {
                popupDescription.setText(item.getDescription());
            }
        } else {
            selectedItem = null;
        }
    }

    private int getPopupIconResId(MeasurementItem item) {
        if (item != null && item.getType() != null) {
            switch (item.getType()) {
                case BUMP:
                case INTERVAl:
                case GEO_TAG:
                    return item.getType().getMapIcon();
                case TAG:
                    return getMapHelper().getTagIconResId(((TagModel)item).getRoadCondition());
            }
        }
        return 0;
    }

    private String getTitle(MeasurementItem item) {
        //return MeasurementsDataHelper.getInstance().getItemTitleStr(item);
        return FileUtils.getExportItemName(getContext(), item.getId(), item.getTime(), item.getType());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        showPopup(null, false);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.popup_layout:
                if (selectedItem != null) {
                    onPopupClicked(selectedItem);
                }
                break;
        }
    }

    protected void onPopupClicked(MeasurementItem item) {
    }
}
