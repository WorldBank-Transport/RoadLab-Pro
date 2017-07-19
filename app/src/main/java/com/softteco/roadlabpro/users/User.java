package com.softteco.roadlabpro.users;

import android.content.Context;
import android.util.Patterns;

import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by Vadim Alenin on 6/1/2015.
 */
public class User {
    public String email;
    public String orgName;
    public String sub;
    public String givenName;
    public boolean roadOrg;
    public long nbf;
    public long exp;

    public String pass;

    public void save(Context context) {
        PreferencesUtil.getInstance().setUserEmail(email);
        PreferencesUtil.getInstance().setHasEmployeeLogin(roadOrg);
    }

    public void restore(Context context) {
        email = PreferencesUtil.getInstance().getUserEmail();
        roadOrg = PreferencesUtil.getInstance().hasEmployeeLogin();
    }

    public void clear(Context context) {
        email = orgName = pass = "";
        PreferencesUtil.getInstance().setUserEmail(email);
        PreferencesUtil.getInstance().setHasEmployeeLogin(false);
    }

    public boolean isExistUser() {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
