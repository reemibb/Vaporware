package com.example.vaporware;

import android.content.Context;
import android.content.SharedPreferences;

public class SongHolder {

    private static final String PREF_NAME = "UserPrefs";
    private static final String SONG_IMG = "image";
    private static final String SONG_TITLE = "song";
    private static final String SONG_ARTIST = "artist";
    private static final String SONG_URL = "url";
    private static final String SONG_PROGRESS = "progress";
    private SharedPreferences sharedPreferences;

    public SongHolder(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSongDetails(int image, String title, String artist, int songResourceId, int progress) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SONG_IMG, image);
        editor.putString(SONG_TITLE, title);
        editor.putString(SONG_ARTIST, artist);
        editor.putInt(SONG_URL, songResourceId);
        editor.putInt(SONG_PROGRESS, progress);
        editor.apply();
    }

    public int getImage() {
        return sharedPreferences.getInt(SONG_IMG, 0);
    }
    public String getTitle() {
        return sharedPreferences.getString(SONG_TITLE, null);
    }
    public String getArtist() {
        return sharedPreferences.getString(SONG_ARTIST, null);
    }
    public int getLink() {
        return sharedPreferences.getInt(SONG_URL, 0);
    }
    public int getProgress() {
        return sharedPreferences.getInt(SONG_PROGRESS, 0);
    }

}
