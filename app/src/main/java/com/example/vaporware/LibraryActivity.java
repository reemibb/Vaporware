package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.io.IOException;

public class LibraryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DrawerLayout drawerLayout;
    private Button manageButton;
    private Button cancelButtonPlaylist1;
    private Button cancelButtonPlaylist2;
    private Button cancelButtonPlaylist3;
    private Button cancelButtonPlaylist4;
    private Button cancelButtonPlaylist5;
    private boolean isManaging = false;
    private LinearLayout playlistsContainer;
    private DatabaseReference playlistsRef;
    private Uri imageUri;
    private ImageView playlistImageView;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_library);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer1_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        playlistsRef = database.getReference("playlists");

        drawerLayout = findViewById(R.id.drawer1_layout);
        manageButton = findViewById(R.id.manage_button);
        playlistsContainer = findViewById(R.id.playlists);
        cancelButtonPlaylist1 = findViewById(R.id.cancel_button_playlist1);
        cancelButtonPlaylist2 = findViewById(R.id.cancel_button_playlist2);
        cancelButtonPlaylist3 = findViewById(R.id.cancel_button_playlist3);
        cancelButtonPlaylist4 = findViewById(R.id.cancel_button_playlist4);
        cancelButtonPlaylist5 = findViewById(R.id.cancel_button_playlist5);

        manageButton.setOnClickListener(v -> toggleManageMode());

        cancelButtonPlaylist1.setOnClickListener(v -> deletePlaylist(cancelButtonPlaylist1, "playlist1"));
        cancelButtonPlaylist2.setOnClickListener(v -> deletePlaylist(cancelButtonPlaylist2, "playlist2"));
        cancelButtonPlaylist3.setOnClickListener(v -> deletePlaylist(cancelButtonPlaylist3, "playlist3"));
        cancelButtonPlaylist4.setOnClickListener(v -> deletePlaylist(cancelButtonPlaylist4, "playlist4"));
        cancelButtonPlaylist5.setOnClickListener(v -> deletePlaylist(cancelButtonPlaylist5, "playlist5"));

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> showCreatePlaylistPopup());

        DrawerLayout drawerLayout = findViewById(R.id.drawer1_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        findViewById(R.id.menuButton).setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
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

            if (id == R.id.nav_home) {
                startActivity(new Intent(LibraryActivity.this, HomeActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(LibraryActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(LibraryActivity.this, HelpActivity.class));
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(LibraryActivity.this, MainActivity.class);
                startActivity(intent);
            }
            return true;
        });

        Button dotButton = findViewById(R.id.dot_button);

        dotButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, dotButton);

            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_dots, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_about) {
                    Toast.makeText(this, "About selected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        Button song_button = findViewById(R.id.song_button);
        song_button.setOnClickListener(view -> {
            Intent intent = new Intent(LibraryActivity.this, SongActivity.class);
            startActivity(intent);
        });


        loadPlaylistsFromFirebase();
    }

    private void loadPlaylistsFromFirebase() {
        playlistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playlistsContainer.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String playlistName = snapshot.child("name").getValue(String.class);
                    String playlistId = snapshot.getKey();
                    String imageUri = snapshot.child("imageUri").getValue(String.class);
                    long songCount = snapshot.child("songs").getChildrenCount();
                    addPlaylistToLayout(playlistName, playlistId, imageUri, songCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void addPlaylistToLayout(String playlistName, String playlistId, String imageUri, long songCount) {
        View playlistLayout = getLayoutInflater().inflate(R.layout.playlist_item, playlistsContainer, false);
        TextView playlistNameTextView = playlistLayout.findViewById(R.id.playlist_name);
        playlistNameTextView.setText(playlistName);
        TextView songCountTextView = playlistLayout.findViewById(R.id.song_count);
        songCountTextView.setText(String.valueOf(songCount));
        ImageView playlistImageView = playlistLayout.findViewById(R.id.playlist_image);
        if (imageUri != null) {
            Picasso.get().load(imageUri).into(playlistImageView);
        }
        Button cancelButton = playlistLayout.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> deletePlaylist(cancelButton, playlistId));
        playlistsContainer.addView(playlistLayout);
    }

    private void showCreatePlaylistPopup() {
        final View popupView = getLayoutInflater().inflate(R.layout.popup_create_playlist, null);
        final EditText playlistNameEditText = popupView.findViewById(R.id.editTextPlaylistName);
        playlistImageView = popupView.findViewById(R.id.imageViewPlaylist);
        Button createPlaylistButton = popupView.findViewById(R.id.createPlaylistButton);
        Button cancelCreatePlaylistButton = popupView.findViewById(R.id.cancelCreatePlaylistButton);

        playlistImageView.setOnClickListener(v -> openImageChooser());

        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
        builder.setView(popupView);

        final AlertDialog dialog = builder.create();

        cancelCreatePlaylistButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        createPlaylistButton.setOnClickListener(v -> {
            String playlistName = playlistNameEditText.getText().toString().trim();

            if (playlistName.isEmpty()) {
                Toast.makeText(LibraryActivity.this, "Please enter a playlist name", Toast.LENGTH_SHORT).show();
            } else {
                addPlaylistToFirebase(playlistName);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                playlistImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPlaylistToFirebase(String playlistName) {
        String playlistId = playlistsRef.push().getKey();
        if (playlistId != null) {
            playlistsRef.child(playlistId).child("name").setValue(playlistName);
            if (imageUri != null) {
                playlistsRef.child(playlistId).child("imageUri").setValue(imageUri.toString());
            }
        }
    }

    private void toggleManageMode() {
        isManaging = !isManaging;
        int visibility = isManaging ? View.VISIBLE : View.GONE;
        for (int i = 0; i < playlistsContainer.getChildCount(); i++) {
            View playlistLayout = playlistsContainer.getChildAt(i);
            Button cancelButton = playlistLayout.findViewById(R.id.cancel_button);
            cancelButton.setVisibility(visibility);
        }
    }

    private void deletePlaylist(Button cancelButton, String playlistId) {
        View playlistLayout = (View) cancelButton.getParent();
        playlistsContainer.removeView(playlistLayout);
        playlistsRef.child(playlistId).removeValue();
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