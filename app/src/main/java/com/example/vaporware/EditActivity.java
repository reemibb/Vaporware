package com.example.vaporware;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private FirebaseAuth mAuth;
    private EditText emailField;
    private EditText usernameField;
    private EditText passwordField;
    private EditText dobField;
    private EditText displayField;
    private FirebaseUser currentUser;
    private Button editButton;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the user is not logged in
        }
        emailField = findViewById(R.id.editTextTextEmailAddress);
        usernameField = findViewById(R.id.editTextUsername);
        passwordField = findViewById(R.id.passwordEditText);
        dobField = findViewById(R.id.editTextDD);
        displayField = findViewById(R.id.editTextText);
        profileImage = findViewById(R.id.imageView3);
        editButton = findViewById(R.id.edit_button);

        loadUserData();

        editButton.setOnClickListener(v -> openFileChooser());

        dobField.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditActivity.this,
                    (view, selectedYear, selectedMonth,selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dobField.setText(date);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // Retrieve user details from Firebase Realtime Database
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        emailField.setText(user.email);
                        usernameField.setText(user.name);
                        passwordField.setText(user.password);
                        dobField.setText(user.dob);
                        displayField.setText(user.display);

                        // Load profile image if available
                        if (user.profileImageUrl != null) {
                            Glide.with(EditActivity.this)
                                    .load(user.profileImageUrl)
                                    .transform(new CircleCrop())
                                    .into(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });

        }

        Button backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        Button saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            String name = usernameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String dob = dobField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String display = displayField.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                usernameField.setError("Name is required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailField.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(dob)) {
                dobField.setError("DOB is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordField.setError("Password is required");
                return;
            }
            if (TextUtils.isEmpty(display)) {
                displayField.setError("Display name is required");
                return;
            }

            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Fetch existing profile image URL before updating user data
                mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        // Create updated user object with profile image URL
                        User updatedUser = new User(name, email, dob, password, display, profileImageUrl);

                        mDatabase.child("users").child(userId).setValue(updatedUser)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
                                        intent.putExtra("displayName", display);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(EditActivity.this, "Failed to update details", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EditActivity.this, "Failed to fetch profile image URL", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImageToFirebase(bitmap);  // Convert and save Base64 string
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadImageToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Save Base64 string to Firebase
        mDatabase.child("profileImageUrl").setValue(base64Image)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(EditActivity.this, "Profile image updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(EditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
    }


    private void loadUserData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);
                    String display = snapshot.child("display").getValue(String.class);
                    String base64Image = snapshot.child("profileImageUrl").getValue(String.class);

                    usernameField.setText(name);
                    emailField.setText(email);
                    dobField.setText(dob);
                    passwordField.setText(password);
                    displayField.setText(display);

                    if (base64Image != null) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Glide.with(EditActivity.this)
                                .load(decodedBitmap)
                                .transform(new CircleCrop())
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

