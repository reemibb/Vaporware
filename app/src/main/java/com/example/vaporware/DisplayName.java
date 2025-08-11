package com.example.vaporware;

import android.content.Context;
import android.content.SharedPreferences;

public class DisplayName {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_NAME = "name";
    private SharedPreferences sharedPreferences;

    public DisplayName(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserDetails(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();  // Apply changes asynchronously
    }

    public String getDisplay() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
