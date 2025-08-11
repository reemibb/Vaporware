package com.example.vaporware;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {

    private Button playButton, nextButton, prevButton;
    private Button shuffleButton, repeatButton;
    private boolean isShuffle = false; //default state
    private boolean isRepeat = false;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    private TextView currentTimeText;
    private TextView totalTimeText;
    private TextView songTitle, songArtist;
    private ImageView songimg;
    private int currentSongIndex = 0;

    private final int[] songs = {R.raw.we_cant_be_friends, R.raw.good_luck_babe, R.raw.birds_of_a_feather};
    private final String[] songNames = {"we can't be friends", "Good Luck, Babe!", "BIRDS OF A FEATHER"};
    private final String[] artists = {"Ariana Grande", "Chappell Roan", "Billie Eilish"};
    private final int[] songImages = {R.drawable.ag, R.drawable.chappell, R.drawable.birds};
    private SongHolder songHolder;
    private SeekBar volumeSeekBar;
    private AudioManager audioManager;
    private SubHolder subHolder;
    private Button profile_btn;
    private MusicHolder musicHolder;
    private TextView artist_txt;
    private TextView song_txt;

    // Firebase database reference
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");

        // Retrieve the song state
        retrieveSongState();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        musicHolder = new MusicHolder(this);
        song_txt = findViewById(R.id.play_title);
        artist_txt = findViewById(R.id.play_artist);
        profile_btn = findViewById(R.id.profile_button);

        /*profile_btn.setOnClickListener(v -> {
            String songTitle = song_txt.getText().toString();
            String songArtist = artist_txt.getText().toString();
            showOptionsDialog(songTitle, songArtist);
        });*/

        subHolder = new SubHolder(this);
        String storedSub = subHolder.getSubscription();

        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        songHolder = new SongHolder(this);

        nextButton = findViewById(R.id.next_btn);
        prevButton = findViewById(R.id.previous_btn);
        songTitle = findViewById(R.id.play_title);
        songimg = findViewById(R.id.play_img);
        songArtist = findViewById(R.id.play_artist);

        nextButton.setOnClickListener(v -> playNext());
        prevButton.setOnClickListener(v -> playPrevious());

        Button down_btn = findViewById(R.id.menuButton);
        down_btn.setOnClickListener(v -> {
            Intent intent = new Intent(PlayerActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, R.anim.slide_down);
        });

        playButton = findViewById(R.id.play_btn);
        seekBar = findViewById(R.id.seekBar2);
        currentTimeText = findViewById(R.id.time1_text);
        totalTimeText = findViewById(R.id.time2_text);

        mediaPlayer = new MediaPlayer();

        playButton.setOnClickListener(v -> {
            togglePlayback();
        });

        //stops mediaplayer when the audio finishes
        mediaPlayer.setOnCompletionListener(mp -> {
            playButton.setBackgroundResource(R.drawable.play);
            seekBar.setProgress(0);
            handler.removeCallbacks(updateSeekBarRunnable);
            currentTimeText.setText(formatTime(0)); //resets time
        });

        //to update seekbar as the audio plays
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeText.setText(formatTime(mediaPlayer.getCurrentPosition())); //updates current time
                    handler.postDelayed(this, 100);
                }
                // Save the playback position
                saveCurrentSongState();
            }
        };

        //to allow user to change progress
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBar();
            }
        });

        shuffleButton = findViewById(R.id.shuffle_btn);
        repeatButton = findViewById(R.id.repeat_btn);

        shuffleButton.setOnClickListener(v -> {
                toggleShuffle();
        });

        repeatButton.setOnClickListener(v -> {
                toggleRepeat();
        });

        int image = songImages[currentSongIndex];
        String title = songTitle.getText().toString().trim();
        String artist = songArtist.getText().toString().trim();
        int songResourceId = songs[currentSongIndex];
        int progress = mediaPlayer.getCurrentPosition();

        songHolder.saveSongDetails(image, title, artist, songResourceId, progress);
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex]);
        songTitle.setText(songNames[currentSongIndex]);
        songArtist.setText(artists[currentSongIndex]);
        songimg.setImageResource(songImages[currentSongIndex]);
        seekBar.setMax(mediaPlayer.getDuration());

        // Save the song state
        saveCurrentSongState();

        mediaPlayer.setOnCompletionListener(mp -> {
            //to replay the same song
            if (isRepeat) {
                setupMediaPlayer();
                togglePlayback();
            } else {
                playNext();
            }
        });
    }

    private void saveCurrentSongState() {
        String title = songNames[currentSongIndex];
        String artist = artists[currentSongIndex];
        int songImage = songImages[currentSongIndex];
        int songResourceId = songs[currentSongIndex];
        int playbackPosition = mediaPlayer.getCurrentPosition();

        SongState songState = new SongState(title, artist, String.valueOf(songImage), String.valueOf(songResourceId), playbackPosition);
        databaseReference.child("currentSongState").setValue(songState);
    }

    private void retrieveSongState() {
        databaseReference.child("currentSongState").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SongState songState = snapshot.getValue(SongState.class);
                    if (songState != null) {
                        updateUIWithSongState(songState);
                        playAudioFromState(songState);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PlayerActivity.this, "Failed to load song state", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithSongState(SongState songState) {
        songTitle.setText(songState.getTitle());
        songArtist.setText(songState.getArtist());
        Glide.with(PlayerActivity.this).load(songState.getImageUrl()).into(songimg);
    }

    private void playAudioFromState(SongState songState) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.setDataSource(songState.getMp3Url());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(songState.getPlaybackPosition());
            mediaPlayer.start();
            playButton.setBackgroundResource(R.drawable.pause);
            updateSeekBar();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            playButton.setBackgroundResource(R.drawable.play);
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.removeCallbacks(updateSeekBarRunnable);
        });
    }

    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setBackgroundResource(R.drawable.play);
        } else {
            mediaPlayer.start();
            playButton.setBackgroundResource(R.drawable.pause);
            updateSeekBar();
        }
    }

    private void playNext() {
        if (isShuffle) {
            currentSongIndex = new Random().nextInt(songs.length);
        } else if (!isRepeat) {
            currentSongIndex = (currentSongIndex + 1) % songs.length;
        }
        setupMediaPlayer();
        togglePlayback();
    }

    private void playPrevious() {
        currentSongIndex = (currentSongIndex - 1 + songs.length) % songs.length;
        setupMediaPlayer();
        togglePlayback();
    }

    private void updateSeekBar() {
        if (updateSeekBarRunnable != null) {
            handler.postDelayed(updateSeekBarRunnable, 100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        shuffleButton.setBackgroundResource(isShuffle ? R.drawable.shuffle_on : R.drawable.shuffle);
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        repeatButton.setBackgroundResource(isRepeat ? R.drawable.repeat_on : R.drawable.repeat);
    }

    private void showSubscriptionMessage() {
        Toast.makeText(this, "Subscribe to get full access to all features", Toast.LENGTH_SHORT).show();
    }

    private void showOptionsDialog(String songTitle, String songArtist) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        dialog.setContentView(dialogView);

        dialogView.findViewById(R.id.add_to_favorites).setOnClickListener(view -> {
            musicHolder.saveUserDetails(songTitle, songArtist);
            Toast.makeText(this, songTitle + " added to favorites", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.add_to_playlist).setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.share).setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.view_artist_page).setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}