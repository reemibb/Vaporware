package com.example.vaporware;

import android.content.Context;
import android.content.SharedPreferences;


public class SubHolder {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_SUB = "premium";
    private SharedPreferences sharedPreferences;

    public SubHolder(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserDetails(String subscription) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SUB, subscription);
        editor.apply();
    }

    public String getSubscription() {
        return sharedPreferences.getString(KEY_SUB, null);
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
