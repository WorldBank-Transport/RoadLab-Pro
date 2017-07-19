package com.softteco.roadlabpro.ui;

import com.softteco.roadlabpro.R;

public enum SaveMeasurementSelectionTypes {
    CURRENT_PROJECT(R.string.current_project_text),
    SELECT (R.string.select_text),
    SELECT_LATER(R.string.select_later_text);

    private int nameId;

    SaveMeasurementSelectionTypes (int nameId) {
        this.nameId = nameId;
    }

    public int getNameId() {
        return nameId;
    }
}
