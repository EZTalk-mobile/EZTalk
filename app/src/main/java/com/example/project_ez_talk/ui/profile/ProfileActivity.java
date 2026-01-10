package com.example.project_ez_talk.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.helper.SupabaseStorageManager;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.login.LoginActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private MaterialCardView cvProfilePicture;
    private ImageView ivProfilePicture;
    private FloatingActionButton fabEditPhoto;
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile;
    private CardView cvProfileOptions;
    private LinearLayout llAccount, llPrivacy, llSettings;
    private MaterialButton btnLogout;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    // Image picker
    private ActivityResultLauncher<String> imagePickerLauncher;
    // ✅ Activity result launcher for EditProfileActivity
    private ActivityResultLauncher<Intent> editProfileLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupImagePicker();
        setupEditProfileLauncher(); // ✅ Setup edit profile launcher
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
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfilePicture.setImageURI(uri);
                        uploadProfilePhotoToSupabase(uri);
                    }
                }
        );
    }

    // ✅ Setup launcher for EditProfileActivity
    private void setupEditProfileLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "EditProfile result received, code: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        // Profile was updated, reload everything
                        Log.d(TAG, "✅ Profile updated, reloading...");

                        // Force reload from Firebase
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            Log.d(TAG, "Force reloading image for user: " + userId);
                            loadProfileImageFromFirebase(userId);
                        }
                    } else {
                        Log.d(TAG, "Result not OK, not reloading");
                    }
                }
        );
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Load from Firebase
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            String userId = currentUser.getUid();

            // Set user info
            tvUserName.setText(displayName != null && !displayName.isEmpty() ?
                    displayName : "User");
            tvUserEmail.setText(email != null ? email : "No email");

            // Load profile photo from Firebase Realtime Database
            loadProfileImageFromFirebase(userId);

            // Also save to preferences
            Preferences.setUsername(this, displayName != null ? displayName : "User");
            Preferences.setUserEmail(this, email != null ? email : "");
        } else {
            // Load from Preferences as fallback
            String name = Preferences.getUsername(this);
            String email = Preferences.getUserEmail(this);

            tvUserName.setText(name != null && !name.isEmpty() ? name : "User");
            tvUserEmail.setText(email != null && !email.isEmpty() ? email : "No email");

            // ✅ Try to load image from preferences
            String photoUrl = Preferences.getUserPhotoUrl(this);
            if (photoUrl != null && !photoUrl.isEmpty()) {
                loadImageWithGlide(photoUrl);
            }
        }
    }

    /**
     * Load profile image from Firebase (where we stored the Supabase URL)
     */
    private void loadProfileImageFromFirebase(String userId) {
        Log.d(TAG, "📸 loadProfileImageFromFirebase called for userId: " + userId);

        usersRef.child(userId).child("profileImageUrl")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.d(TAG, "📸 Firebase snapshot received, exists: " + snapshot.exists());
                        String imageUrl = snapshot.getValue(String.class);
                        Log.d(TAG, "📸 Image URL from Firebase: " + imageUrl);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Log.d(TAG, "📸 Loading image from URL: " + imageUrl);
                            loadImageWithGlide(imageUrl);
                        } else {
                            Log.d(TAG, "⚠️ No image URL found in Firebase");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "❌ Failed to load image URL: " + error.getMessage());
                    }
                });
    }

    /**
     * Load image using Glide
     */
    private void loadImageWithGlide(String imageUrl) {
        Log.d(TAG, "Loading image with Glide: " + imageUrl);

        Glide.with(this)
                .load(imageUrl)
                .skipMemoryCache(true) // ✅ Skip cache to force fresh load
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // ✅ No disk cache
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                                Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e(TAG, "Glide load failed: " + (e != null ? e.getMessage() : "unknown"));
                        if (e != null) e.logRootCauses(TAG);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d(TAG, "✅ Image loaded successfully!");
                        return false;
                    }
                })
                .into(ivProfilePicture);
    }

    /**
     * Upload profile photo to Supabase
     */
    private void uploadProfilePhotoToSupabase(Uri imageUri) {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        showLoading(true);
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();

        SupabaseStorageManager.uploadProfileImage(
                imageUri,
                userId,
                new SupabaseStorageManager.UploadCallback() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        Log.d(TAG, "Upload successful: " + publicUrl);
                        // Save the Supabase URL to Firebase
                        saveImageUrlToFirebase(userId, publicUrl);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Upload failed: " + error);
                        showLoading(false);
                        Toast.makeText(ProfileActivity.this,
                                "Failed to upload photo: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Save the Supabase image URL to Firebase Realtime Database
     */
    private void saveImageUrlToFirebase(String userId, String imageUrl) {
        usersRef.child(userId).child("profileImageUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Image URL saved to Firebase");
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this,
                            "Profile photo updated!",
                            Toast.LENGTH_SHORT).show();
                    // Save to preferences too
                    Preferences.setUserPhotoUrl(this, imageUrl);
                    // ✅ Reload the image
                    loadImageWithGlide(imageUrl);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save URL: " + e.getMessage());
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this,
                            "Failed to update photo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        // Edit photo
        fabEditPhoto.setOnClickListener(v -> openImagePicker());

        // Edit profile - ✅ Use launcher instead of startActivity
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
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

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        fabEditPhoto.setEnabled(!loading);
        btnEditProfile.setEnabled(!loading);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile in case it was edited
        if (currentUser != null) {
            Log.d(TAG, "onResume - reloading profile");
            loadUserProfile();
        }
    }
}