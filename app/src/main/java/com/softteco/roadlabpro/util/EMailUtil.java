package com.softteco.roadlabpro.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Utility class for work with e-mail.
 */
public class EMailUtil {

    public static final String EMAIL_MIME_TYPE = "text/plain";
    private static final String TAG = EMailUtil.class.getSimpleName();

    public static void sendAttachFileMail(Context c, String[] path,
			String subject, String type) {
		final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType(type);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, subject);
        ArrayList<Uri> uris = new ArrayList<Uri>();

        for (String file : path) {
            File fileIn = new File(file);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        emailIntent.putExtra(Intent.EXTRA_STREAM, uris);
        try {
            c.startActivity(Intent.createChooser(emailIntent, subject));
        } catch (Exception e) {
            Log.e(TAG, "sendAttachFileMail", e);
        }
	}

    public static void sendEmail(Context cxt, String to, String subject, String body) {
        final Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        if (to != null && to.length() > 0) {
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        }
        sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        try {
            cxt.startActivity(Intent.createChooser(sendIntent, "Send feedback"));
        } catch (Exception e) {
            Log.e(TAG, "sendEmail", e);
        }
    }
}
