package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class ProfileActivity extends AppCompatActivity {

    private TextView subscriptionStatus, displayField;
    private Button cancelPaymentButton;
    private LinearLayout paymentPlansContainer;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private SubHolder subHolder;
    private DisplayName displayName;
    private ImageView profileImage; // Add the ImageView for profile image

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        subHolder = new SubHolder(this);
        subscriptionStatus = findViewById(R.id.textView7);
        cancelPaymentButton = findViewById(R.id.cancel_button);
        paymentPlansContainer = findViewById(R.id.payment_plans_container);
        displayName = new DisplayName(this);

        profileImage = findViewById(R.id.imageView3); // Initialize the profile image view

        // Initialize the TextView for display name
        TextView displayField = findViewById(R.id.textView6);

        // 1️⃣ Check if there's an updated display name coming from EditActivity
        String updatedDisplayName = getIntent().getStringExtra("displayName");
        if (updatedDisplayName != null) {
            displayField.setText(updatedDisplayName);
        } else {
            // 2️⃣ If no updated value, load it from Firebase
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String savedDisplayName = dataSnapshot.child("display").getValue(String.class);
                            if (savedDisplayName != null) {
                                displayField.setText(savedDisplayName);
                            }

                            String base64Image = dataSnapshot.child("profileImageUrl").getValue(String.class);
                            if (base64Image != null) {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(ProfileActivity.this)
                                        .load(decodedBitmap)
                                        .transform(new CircleCrop())
                                        .into(profileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ProfileActivity.this, "Failed to load display name", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        setupButtons();
    }

    private void fetchDisplayName() {
        databaseReference.child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.getValue(String.class);
                    displayField.setText(displayName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load display name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        // Logout button
        AppCompatButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setPaintFlags(logoutButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Cancel subscription button
        cancelPaymentButton.setOnClickListener(view -> {
            subscriptionStatus.setText("Free Version");
            cancelPaymentButton.setVisibility(View.GONE);

            // Save subscription as "Free Version"
            subHolder.saveUserDetails("Free Version");

            // Show payment plans
            showPaymentPlans();
        });

        // Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Edit profile button
        Button editButton = findViewById(R.id.pfp_edit);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditActivity.class);
            startActivity(intent);
        });

        // Menu button
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
    }

    private void showPaymentPlans() {
        paymentPlansContainer.setVisibility(View.VISIBLE);
        paymentPlansContainer.removeAllViews();

        String[] plans = {"Monthly Plan - $9.99", "Yearly Plan - $99.99"};
        for (String plan : plans) {
            Button planButton = new Button(this);
            planButton.setText(plan);
            planButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            planButton.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            planButton.setOnClickListener(view -> subscribeToPlan());
            paymentPlansContainer.addView(planButton);
        }
    }

    private void subscribeToPlan() {
        subscriptionStatus.setText("Premium");
        cancelPaymentButton.setVisibility(View.VISIBLE);
        paymentPlansContainer.setVisibility(View.GONE);

        // Save subscription as "Premium"
        subHolder.saveUserDetails("Premium");
    }
}