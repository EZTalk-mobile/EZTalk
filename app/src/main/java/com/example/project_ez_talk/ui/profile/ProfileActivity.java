package com.example.project_ez_talk.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.login.LoginActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends BaseActivity {

    // UI Components
    private MaterialCardView cvProfilePicture;
    private ImageView ivProfilePicture;
    private FloatingActionButton fabEditPhoto;
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile;
    private CardView cvProfileOptions;
    private LinearLayout llAccount, llPrivacy, llSettings;
    private MaterialButton btnLogout;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Image picker
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        setupImagePicker();
        loadUserProfile();
        setupClickListeners();
    }

    private void initViews() {
        cvProfilePicture = findViewById(R.id.cvProfilePicture);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        fabEditPhoto = findViewById(R.id.fabEditPhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        cvProfileOptions = findViewById(R.id.cvProfileOptions);
        llAccount = findViewById(R.id.llAccount);
        llPrivacy = findViewById(R.id.llPrivacy);
        llSettings = findViewById(R.id.llSettings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfilePicture.setImageURI(uri);
                        uploadProfilePhoto(uri);
                    }
                }
        );
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Load from Firebase
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            // Set user info
            tvUserName.setText(displayName != null && !displayName.isEmpty() ?
                    displayName : "User");
            tvUserEmail.setText(email != null ? email : "No email");

            // Load profile photo if available
            if (photoUrl != null) {
                // TODO: Use Glide or Picasso to load image
                // Glide.with(this).load(photoUrl).into(ivProfilePicture);
            }

            // Also save to preferences
            Preferences.setUsername(this, displayName != null ? displayName : "User");
            Preferences.setUserEmail(this, email != null ? email : "");
        } else {
            // Load from Preferences as fallback
            String name = Preferences.getUsername(this);
            String email = Preferences.getUserEmail(this);

            tvUserName.setText(name != null && !name.isEmpty() ? name : "User");
            tvUserEmail.setText(email != null && !email.isEmpty() ? email : "No email");
        }
    }

    private void setupClickListeners() {
        // Edit photo
        fabEditPhoto.setOnClickListener(v -> openImagePicker());

        // Edit profile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Account settings
        llAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Account settings", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to account settings
        });

        // Privacy settings
        llPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy settings", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to privacy settings
        });

        // General settings
        llSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadProfilePhoto(Uri imageUri) {
        // Show loading
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();

        // Update Firebase profile photo
        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUri)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Profile photo updated!",
                                    Toast.LENGTH_SHORT).show();

                            // Save to preferences
                            Preferences.setUserPhotoUrl(this, imageUri.toString());
                        } else {
                            Toast.makeText(this,
                                    "Failed to update photo: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear preferences
        Preferences.clearAll(this);

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile in case it was edited
        loadUserProfile();
    }
}