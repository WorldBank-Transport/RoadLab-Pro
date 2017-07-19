package com.softteco.roadlabpro.ui;


import android.content.Context;
import android.os.Bundle;

public class EnterIDDialog extends CustomInputDialog {


    public EnterIDDialog(Context context, String titleDialog, CustomInputDialogListener photoListener) {
        super(context, titleDialog, photoListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
