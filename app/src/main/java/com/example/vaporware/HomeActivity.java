package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button playButton;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private SongHolder songHolder;
    private String currentDataSource; // To store the current data source URL

    private DatabaseReference databaseReference;

    @SuppressLint({"NonConstantResourceId", "RtlHardcoded"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");

        // Retrieve the song state
        retrieveSongState();

        // Initialize UI components and listeners
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        songHolder = new SongHolder(this);

        ImageView imageField = findViewById(R.id.play_img);
        TextView titleField = findViewById(R.id.song_name);
        TextView artistField = findViewById(R.id.artist_name);

        ImageButton pfp_button = findViewById(R.id.profile_button);
        pfp_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String base64Image = snapshot.child("profileImageUrl").getValue(String.class);
                        if (base64Image != null) {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            Glide.with(HomeActivity.this)
                                    .load(decodedBitmap)
                                    .transform(new CircleCrop())
                                    .into(pfp_button);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });
        }

        ImageButton ad3_button = findViewById(R.id.ad3_button);
        ad3_button.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, Ad3Activity.class);
            startActivity(intent);
        });

        Button pop_see = findViewById(R.id.pop_see);
        pop_see.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, PopularActivity.class);
            startActivity(intent);
        });

        Button playlist_see = findViewById(R.id.playlist_see);
        playlist_see.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, LibraryActivity.class);
            startActivity(intent);
        });

        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontalScrollView);
        horizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> Log.d("ScrollView", "Scrolled to X: " + scrollX + ", Y: " + scrollY));

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        findViewById(R.id.menuButton).setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_library) {
                Toast.makeText(this, "Library selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_createPL) {
                Toast.makeText(this, "Create Playlist selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_managePL) {
                Toast.makeText(this, "Manage Playlist selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_help) {
                Toast.makeText(this, "Help and Feedback selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_logout) {
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_library) {
                startActivity(new Intent(HomeActivity.this, LibraryActivity.class));
            } else if (id == R.id.nav_createPL) {
                startActivity(new Intent(HomeActivity.this, LibraryActivity.class));
            } else if (id == R.id.nav_managePL) {
                startActivity(new Intent(HomeActivity.this, LibraryActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(HomeActivity.this, HelpActivity.class));
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
            return true;
        });

        ImageButton play_img = findViewById(R.id.play_img);
        play_img.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, PlayerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);
        });

        playButton = findViewById(R.id.play_button);
        seekBar = findViewById(R.id.seekBar);

        mediaPlayer = new MediaPlayer();

        playButton.setOnClickListener(v -> {
            togglePlayback();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            playButton.setBackgroundResource(R.drawable.play_button);
            seekBar.setProgress(0);
            handler.removeCallbacks(updateSeekBarRunnable);
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 100);
                }
                // Save the playback position
                saveCurrentSongState();
            }
        };

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

        // Set up button click listeners for popular songs
        ImageButton pop1_button = findViewById(R.id.pop1_button);
        pop1_button.setOnClickListener(view -> playSong("pop1"));

        ImageButton pop2_button = findViewById(R.id.pop2_button);
        pop2_button.setOnClickListener(view -> playSong("pop2"));

        ImageButton pop3_button = findViewById(R.id.pop3_button);
        pop3_button.setOnClickListener(view -> playSong("pop3"));

    }

    private void playSong(String songId) {
        databaseReference.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String artist = snapshot.child("artist").getValue(String.class);
                    String imageUrl = snapshot.child("image").getValue(String.class);
                    String mp3Url = snapshot.child("mp3").getValue(String.class);

                    if (mp3Url == null || mp3Url.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "Invalid song URL", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update the UI with song details
                    TextView titleField = findViewById(R.id.song_name);
                    TextView artistField = findViewById(R.id.artist_name);
                    ImageView imageField = findViewById(R.id.play_img);

                    titleField.setText(title);
                    artistField.setText(artist);
                    Glide.with(HomeActivity.this).load(imageUrl).into(imageField);

                    // Play the song
                    playAudio(mp3Url);
                } else {
                    Toast.makeText(HomeActivity.this, "Song not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load song", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playAudio(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playButton.setBackgroundResource(R.drawable.pause);
            updateSeekBar();

            // Store the current data source URL
            currentDataSource = url;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            playButton.setBackgroundResource(R.drawable.play_button);
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.removeCallbacks(updateSeekBarRunnable);
        });

        // Save the song state
        saveCurrentSongState();
    }

    private void saveCurrentSongState() {
        String title = ((TextView) findViewById(R.id.song_name)).getText().toString();
        String artist = ((TextView) findViewById(R.id.artist_name)).getText().toString();
        String imageUrl = ""; // You need to get the image URL from your source
        int playbackPosition = mediaPlayer.getCurrentPosition();

        SongState songState = new SongState(title, artist, imageUrl, currentDataSource, playbackPosition);
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
                Toast.makeText(HomeActivity.this, "Failed to load song state", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithSongState(SongState songState) {
        TextView titleField = findViewById(R.id.song_name);
        TextView artistField = findViewById(R.id.artist_name);
        ImageView imageField = findViewById(R.id.play_img);

        titleField.setText(songState.getTitle());
        artistField.setText(songState.getArtist());
        Glide.with(HomeActivity.this).load(songState.getImageUrl()).into(imageField);
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

            // Store the current data source URL
            currentDataSource = songState.getMp3Url();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            playButton.setBackgroundResource(R.drawable.play_button);
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.removeCallbacks(updateSeekBarRunnable);
        });
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.postDelayed(updateSeekBarRunnable, 100);
        }
    }

    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setBackgroundResource(R.drawable.play_button);
        } else {
            mediaPlayer.start();
            playButton.setBackgroundResource(R.drawable.pause);
            updateSeekBar();
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}