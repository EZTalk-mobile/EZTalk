package com.example.project_ez_talk.ui.profile;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

@SuppressWarnings("ALL")
public class EditProfileActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private CardView cvProfilePicture;
    private ImageView ivProfilePicture;
    private FloatingActionButton fabChangePhoto;
    private EditText etFullName, etUsername, etEmail, etPhone, etStatus;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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
            Uri photoUrl = currentUser.getPhotoUrl();

            etFullName.setText(displayName != null ? displayName : "");
            etEmail.setText(email != null ? email : "");

            // Load photo if available
            if (photoUrl != null) {
                // TODO: Use Glide to load image
                // Glide.with(this).load(photoUrl).into(ivProfilePicture);
            }
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

        // Update Firebase profile
        if (currentUser != null) {
            UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName);

            // Add photo if selected
            if (selectedImageUri != null) {
                profileUpdatesBuilder.setPhotoUri(selectedImageUri);
            }

            UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update email if changed
                            String currentEmail = currentUser.getEmail();
                            if (!email.equals(currentEmail)) {
                                updateEmail(email, fullName, username, phone, status);
                            } else {
                                saveToPreferences(fullName, username, email, phone, status);
                            }
                        } else {
                            showLoading(false);
                            Toast.makeText(this,
                                    "Failed to update profile: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // No Firebase user, just save locally
            saveToPreferences(fullName, username, email, phone, status);
        }
    }

    private void updateEmail(String newEmail, String fullName, String username, String phone, String status) {
        if (currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveToPreferences(fullName, username, newEmail, phone, status);
                        } else {
                            showLoading(false);
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error";

                            if (error.toLowerCase().contains("requires-recent-login")) {
                                Toast.makeText(this,
                                        "Please log in again to change your email",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this,
                                        "Failed to update email: " + error,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void saveToPreferences(String fullName, String username, String email, String phone, String status) {
        // Save to SharedPreferences
        Preferences.setUsername(this, username);
        Preferences.setUserEmail(this, email);
        Preferences.setUserPhone(this, phone);

        if (selectedImageUri != null) {
            Preferences.setUserPhotoUrl(this, selectedImageUri.toString());
        }

        showLoading(false);
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

        // Return result
        setResult(RESULT_OK);
        finish();
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