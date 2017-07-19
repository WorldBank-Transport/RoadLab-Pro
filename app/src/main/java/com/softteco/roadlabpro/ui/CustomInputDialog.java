package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.view.TypefaceTextView;

/**
 * Created by bogdan on 04.04.2016.
 */
public class CustomInputDialog extends Dialog {

    protected CustomInputDialogListener listener;
    private Button okButton;
    protected String title;
    protected EditText txtValue;
    protected CharSequence value;

    public CustomInputDialog(final Context context, final String titleDialog,
        final CustomInputDialogListener photoListener) {
        super(context);
        listener = photoListener;
        title = titleDialog;
    }

    public CustomInputDialog(final Context context, final String titleDialog,
        final String value, final CustomInputDialogListener photoListener) {
        this(context, titleDialog, photoListener);
        seTextValue(value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId());
        setCancelable(true);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        txtValue = (EditText) findViewById(R.id.dlg_enter_id_value);
        setupValueTextChangeListener();
        seTextValue(this.value);
        final TypefaceTextView txtTitle = (TypefaceTextView) findViewById(R.id.dlg_enter_id_header_title);
        txtTitle.setText(title);
        okButton = (Button) findViewById(R.id.dlg_enter_id_btn_ok);
        setOkButtonState(!TextUtils.isEmpty(this.value));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "";
                if (getTextValue() != null) {
                    text = getTextValue();
                }
                if (listener != null) {
                    listener.onRequestClose(text);
                }
                dismiss();
            }
        });
        findViewById(R.id.dlg_enter_id_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    protected int getLayoutId() {
        return R.layout.dialog_enter_id;
    }

    private void setupValueTextChangeListener() {
        if (txtValue != null) {
            txtValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    CustomInputDialog.this.value = s;
                    setOkButtonState(!TextUtils.isEmpty(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setOkButtonState(boolean enable) {
        if (okButton != null) {
           okButton.setEnabled(enable);
        }
    }

    public String getTextValue() {
        if (txtValue != null && txtValue.getText() != null) {
            return txtValue.getText().toString();
        }
        return "";
    }

    public void seTextValue(CharSequence value) {
        this.value = value;
        if (txtValue != null) {
            txtValue.setText(value);
        }
    }

    public interface CustomInputDialogListener {
        void onRequestClose(String value);
    }
}
