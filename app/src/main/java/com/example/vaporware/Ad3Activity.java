package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Ad3Activity extends AppCompatActivity {
    private int[] buttonIds = {
            R.id.ellip_btn1,
            R.id.ellip_btn2,
            R.id.ellip_btn3,
            R.id.ellip_btn4,
            R.id.ellip_btn5,
            R.id.ellip_btn6,
            R.id.ellip_btn7,
            R.id.ellip_btn8,
            R.id.ellip_btn9,
            R.id.ellip_btn10,
            R.id.ellip_btn11,
            R.id.ellip_btn12,
            R.id.ellip_btn13
    };

    private MusicHolder musicHolder;
    private DatabaseReference playlistsRef;
    private TextView[] positionsTextViews = new TextView[13];

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ad3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playlistsRef = FirebaseDatabase.getInstance().getReference("playlists");
        musicHolder = new MusicHolder(this);

        setupEllipsisButtons();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Ad3Activity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        Button dotButton = findViewById(R.id.dot_button);
        dotButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, dotButton);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.album_dots, popupMenu.getMenu());
            popupMenu.show();
        });

        int[] positionIds = {
                R.id.positions1, R.id.positions2, R.id.positions3,
                R.id.positions4, R.id.positions5, R.id.positions6,
                R.id.positions7, R.id.positions8, R.id.positions9,
                R.id.positions10, R.id.positions11, R.id.positions12,
                R.id.positions13
        };

        for (int i = 0; i < positionsTextViews.length; i++) {
            positionsTextViews[i] = findViewById(positionIds[i]);
        }
    }

    private void setupEllipsisButtons() {
        for (int i = 0; i < buttonIds.length; i++) {
            final int index = i;
            findViewById(buttonIds[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String songTitle = positionsTextViews[index].getText().toString();
                    String songArtist = "Ariana Grande";
                    showOptionsDialog(songTitle, songArtist);
                }
            });
        }
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
            showPlaylistsDialog(songTitle, songArtist);
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

    private void showPlaylistsDialog(String songTitle, String songArtist) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_playlists, null);
        LinearLayout playlistsContainer = dialogView.findViewById(R.id.playlists_container);

        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String playlistId = snapshot.getKey();
                    String playlistName = snapshot.child("name").getValue(String.class);
                    long songCount = snapshot.child("songs").getChildrenCount();

                    View playlistItemView = getLayoutInflater().inflate(R.layout.playlist_item_dialog, null);
                    TextView playlistNameTextView = playlistItemView.findViewById(R.id.playlist_name);
                    TextView songCountTextView = playlistItemView.findViewById(R.id.song_count);
                    playlistNameTextView.setText(playlistName);
                    songCountTextView.setText(String.valueOf(songCount));

                    playlistItemView.setOnClickListener(v -> {
                        addSongToPlaylist(songTitle, songArtist, playlistId, songCount);
                        dialog.dismiss();
                    });

                    playlistsContainer.addView(playlistItemView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void addSongToPlaylist(String songTitle, String songArtist, String playlistId, long songCount) {
        DatabaseReference playlistSongsRef = playlistsRef.child(playlistId).child("songs");
        String songId = playlistSongsRef.push().getKey();
        if (songId != null) {
            playlistSongsRef.child(songId).setValue(new Song(songTitle, songArtist, R.drawable.ag, 0));
            playlistsRef.child(playlistId).child("songCount").setValue(songCount + 1);
            Toast.makeText(this, songTitle + " added to playlist", Toast.LENGTH_SHORT).show();
        }
    }
}