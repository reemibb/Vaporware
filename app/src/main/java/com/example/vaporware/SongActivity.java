package com.example.vaporware;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SongActivity extends AppCompatActivity {

    private MusicHolder musicHolder;
    private boolean isManaging = false;
    private LinearLayout playlistsContainer;

    @SuppressLint({"NonConstantResourceId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_song);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        musicHolder = new MusicHolder(this);

        playlistsContainer = findViewById(R.id.songs);

        Button dotButton = findViewById(R.id.dot_button);
        dotButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, dotButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.liked_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit) {
                    toggleManageMode();
                    return true;
                } else if (item.getItemId() == R.id.sort) {
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        loadSongsFromFirebase();
    }

    private void toggleManageMode() {
        isManaging = !isManaging;
        int visibility = isManaging ? View.VISIBLE : View.GONE;
        for (int i = 0; i < playlistsContainer.getChildCount(); i++) {
            View songLayout = playlistsContainer.getChildAt(i);
            Button cancelButton = songLayout.findViewById(R.id.cancel_button_playlist1);
            if (cancelButton != null) {
                cancelButton.setVisibility(visibility);
                cancelButton.setOnClickListener(v -> deletePlaylist(songLayout));
            }
        }
    }

    private void deletePlaylist(View songLayout) {
        playlistsContainer.removeView(songLayout);
        // Optionally, remove the song from Firebase as well
    }

    private void loadSongsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playlistsContainer.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MusicHolder.Song song = snapshot.getValue(MusicHolder.Song.class);
                    if (song != null) {
                        addSongToLayout(song);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void addSongToLayout(MusicHolder.Song song) {
        View songLayout = getLayoutInflater().inflate(R.layout.song_item, playlistsContainer, false);
        TextView titleField = songLayout.findViewById(R.id.song1);
        TextView artistField = songLayout.findViewById(R.id.artist1);
        titleField.setText(song.title);
        artistField.setText(song.artist);
        playlistsContainer.addView(songLayout);
    }
}