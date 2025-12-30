package com.example.project_ez_talk.ui.profile;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.helper.SupabaseStorageManager;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@SuppressWarnings("ALL")
public class EditProfileActivity extends BaseActivity {

    private static final String TAG = "EditProfileActivity";

    private MaterialToolbar toolbar;
    private CardView cvProfilePicture;
    private ImageView ivProfilePicture;
    private FloatingActionButton fabChangePhoto;
    private EditText etFullName, etUsername, etEmail, etPhone, etStatus;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupImagePicker();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cvProfilePicture = findViewById(R.id.cvProfilePicture);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        fabChangePhoto = findViewById(R.id.fabChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etStatus = findViewById(R.id.etStatus);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfilePicture.setImageURI(uri);
                    }
                }
        );
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        fabChangePhoto.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    @SuppressLint("SetTextI18n")
    private void loadUserData() {
        if (currentUser != null) {
            // Load from Firebase
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            etFullName.setText(displayName != null ? displayName : "");
            etEmail.setText(email != null ? email : "");
        }

        // Load from preferences
        String username = Preferences.getUsername(this);
        String phone = Preferences.getUserPhone(this);

        if (username != null && !username.isEmpty()) {
            etUsername.setText(username);
        }
        if (phone != null && !phone.isEmpty()) {
            etPhone.setText(phone);
        }

        // Status is local only for now
        etStatus.setText("Hey there! I'm using EZ Talk");
    }

    private void saveProfile() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String status = etStatus.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            etEmail.requestFocus();
            return;
        }

        showLoading(true);

        // Check if image was selected
        if (selectedImageUri != null) {
            // Upload image to Supabase first
            uploadImageAndSaveProfile(fullName, username, email, phone, status);
        } else {
            // No image selected, just update profile
            updateProfileWithoutImage(fullName, username, email, phone, status);
        }
    }

    /**
     * Upload image to Supabase, then save profile
     */
    private void uploadImageAndSaveProfile(String fullName, String username,
                                           String email, String phone, String status) {
        if (currentUser == null) {
            showLoading(false);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        SupabaseStorageManager.uploadProfileImage(
                selectedImageUri,
                userId,
                new SupabaseStorageManager.UploadCallback() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        Log.d(TAG, "Image uploaded: " + publicUrl);
                        // ✅ Skip Firebase Auth update, go straight to saving
                        saveProfileData(fullName, username, email, phone, status, publicUrl);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Image upload failed: " + error);
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to upload image: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Save all profile data (simplified version)
     */
    private void saveProfileData(String fullName, String username,
                                 String email, String phone, String status, String imageUrl) {
        if (currentUser == null) {
            showLoading(false);
            return;
        }

        String userId = currentUser.getUid();

        // Update Firebase Auth display name only (skip photo URI)
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    // Continue regardless of success/failure
                    // Save to Firebase Realtime Database
                    if (imageUrl != null) {
                        usersRef.child(userId).child("profileImageUrl").setValue(imageUrl);
                    }

                    // Save to SharedPreferences
                    Preferences.setUsername(this, username);
                    Preferences.setUserEmail(this, email);
                    Preferences.setUserPhone(this, phone);
                    if (imageUrl != null) {
                        Preferences.setUserPhotoUrl(this, imageUrl);
                    }

                    showLoading(false);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
    }

    /**
     * Update profile without image
     */
    private void updateProfileWithoutImage(String fullName, String username,
                                           String email, String phone, String status) {
        if (currentUser == null) {
            showLoading(false);
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    // Save to SharedPreferences
                    Preferences.setUsername(this, username);
                    Preferences.setUserEmail(this, email);
                    Preferences.setUserPhone(this, phone);

                    showLoading(false);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setText(loading ? "" : getString(R.string.save));
        btnSave.setEnabled(!loading);

        etFullName.setEnabled(!loading);
        etUsername.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etStatus.setEnabled(!loading);
        fabChangePhoto.setEnabled(!loading);
    }

    public CardView getCvProfilePicture() {
        return cvProfilePicture;
    }

    public void setCvProfilePicture(CardView cvProfilePicture) {
        this.cvProfilePicture = cvProfilePicture;
    }
}