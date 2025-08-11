package com.example.vaporware;

public class SongState {
    private String title;
    private String artist;
    private String imageUrl;
    private String mp3Url;
    private int playbackPosition;

    // Default constructor required for calls to DataSnapshot.getValue(SongState.class)
    public SongState() {
    }

    public SongState(String title, String artist, String imageUrl, String mp3Url, int playbackPosition) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.mp3Url = mp3Url;
        this.playbackPosition = playbackPosition;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public int getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(int playbackPosition) {
        this.playbackPosition = playbackPosition;
    }
}
