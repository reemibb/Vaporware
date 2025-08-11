package com.example.vaporware;

public class Song {
    private String title;
    private String artist;
    private int image;
    private int resourceId;

    // Default constructor required for calls to DataSnapshot.getValue(Song.class)
    public Song() {
    }

    public Song(String title, String artist, int image, int resourceId) {
        this.title = title;
        this.artist = artist;
        this.image = image;
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}