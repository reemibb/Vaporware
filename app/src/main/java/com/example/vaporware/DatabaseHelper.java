package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserManagement.db";
    private static final int DATABASE_VERSION = 1; // Increment this if you modify the schema

    // Table and column names for users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_PASSWORD = "password";

    // Table and column names for songs
    public static final String TABLE_SONGS = "songs";
    public static final String COLUMN_SONG_ID = "id";
    public static final String COLUMN_SONG_TITLE = "title";
    public static final String COLUMN_SONG_ARTIST = "artist";
    public static final String COLUMN_SONG_DURATION = "duration";
    public static final String COLUMN_SONG_IMAGE = "image";
    public static final String COLUMN_SONG_MP3 = "mp3";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating database tables...");
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_DOB + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create songs table
        String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_SONGS + " (" +
                COLUMN_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SONG_TITLE + " TEXT, " +
                COLUMN_SONG_ARTIST + " TEXT, " +
                COLUMN_SONG_DURATION + " INTEGER, " +
                COLUMN_SONG_IMAGE + " BLOB, " +
                COLUMN_SONG_MP3 + " BLOB)";
        db.execSQL(CREATE_SONGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);

        // Create tables again
        onCreate(db);
    }

    // Insert a song into the songs table (with image)
    public boolean insertSong(String title, String artist, int duration, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SONG_TITLE, title);
        values.put(COLUMN_SONG_ARTIST, artist);
        values.put(COLUMN_SONG_DURATION, duration);
        values.put(COLUMN_SONG_IMAGE, image); // Add the image as a BLOB

        long result = db.insert(TABLE_SONGS, null, values);
        db.close();
        return result != -1; // Returns true if the song was inserted successfully
    }

    // Retrieve a song's image by its ID
    @SuppressLint("Range")
    public byte[] getSongImage(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, new String[]{COLUMN_SONG_IMAGE},
                COLUMN_SONG_ID + "=?", new String[]{String.valueOf(songId)},
                null, null, null);

        byte[] image = null;
        if (cursor.moveToFirst()) {
            image = cursor.getBlob(cursor.getColumnIndex(COLUMN_SONG_IMAGE));
        }
        cursor.close();
        db.close();
        return image;
    }

    // Other methods for users table (unchanged)
    public boolean insertUser(String username, String email, String dob, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DOB, dob);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkMail(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_USERNAME + " = ?";
        String[] whereArgs = {username};
        int result = db.delete(TABLE_USERS, whereClause, whereArgs);
        db.close();
        return result > 0;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    // Get all songs
    public Cursor getAllSongs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SONGS, null);
    }
    public Cursor getPopularSongs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SONGS + " LIMIT 4", null); // Adjust the query as needed
    }
    public boolean insertSong(String title, String artist, int duration, byte[] image, byte[] mp3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SONG_TITLE, title);
        values.put(COLUMN_SONG_ARTIST, artist);
        values.put(COLUMN_SONG_DURATION, duration);
        values.put(COLUMN_SONG_IMAGE, image); // Add the image as a BLOB
        values.put(COLUMN_SONG_MP3, mp3); // Add the MP3 file as a BLOB

        long result = db.insert(TABLE_SONGS, null, values);
        db.close();
        return result != -1; // Returns true if the song was inserted successfully
    }
    @SuppressLint("Range")
    public byte[] getSongMp3(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, new String[]{COLUMN_SONG_MP3},
                COLUMN_SONG_ID + "=?", new String[]{String.valueOf(songId)},
                null, null, null);

        byte[] mp3 = null;
        if (cursor.moveToFirst()) {
            mp3 = cursor.getBlob(cursor.getColumnIndex(COLUMN_SONG_MP3));
        }
        cursor.close();
        db.close();
        return mp3;
    }
}