package com.example.vaporware;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MusicHolder {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;

    public MusicHolder(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
    }

    public void saveUserDetails(String title, String artist) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_ARTIST, artist);
        editor.apply();

        // Save to Firebase
        String songId = databaseReference.push().getKey();
        Song song = new Song(title, artist);
        if (songId != null) {
            databaseReference.child(songId).setValue(song);
        }
    }

    public String getTitle() {
        return sharedPreferences.getString(KEY_TITLE, null);
    }

    public String getArtist() {
        return sharedPreferences.getString(KEY_ARTIST, null);
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static class Song {
        public String title;
        public String artist;

        public Song() {
            // Default constructor required for calls to DataSnapshot.getValue(Song.class)
        }

        public Song(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }
    }
}