package com.example.project_ez_talk.ui.auth.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.login.LoginActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";

    private ImageView btnBack;
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginNow;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> performRegistration());

        tvLoginNow.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        clearErrors();

        String username = getText(etUsername);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateInputs(username, email, password, confirmPassword)) return;

        showLoading(true);

        // Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                // Create user profile in Firestore with email
                                createUserProfileInFirestore(user, username, email);
                            });
                        }
                    } else {
                        showLoading(false);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void createUserProfileInFirestore(FirebaseUser firebaseUser, String username, String email) {
        Log.d(TAG, "createUserProfileInFirestore: Creating profile for " + email);

        String userId = firebaseUser.getUid();

        // Create user data map with all required fields
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("email", email);  // CRITICAL: Save email to Firestore!
        userData.put("name", username);
        userData.put("online", true);
        userData.put("avatarUrl", "");
        userData.put("status", "online");
        userData.put("profilePicture", "");
        userData.put("bio", "");
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastSeen", System.currentTimeMillis());

        // Save to Firestore users collection
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "createUserProfileInFirestore: Success! Saved email: " + email);
                    // Now send email verification
                    sendEmailVerification(firebaseUser, username, email);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createUserProfileInFirestore: Failed", e);
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this,
                            "Failed to create profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Delete the auth user since profile creation failed
                    firebaseUser.delete();
                });
    }

    private void sendEmailVerification(FirebaseUser user, String username, String email) {
        Log.d(TAG, "sendEmailVerification: Sending verification to " + email);

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendEmailVerification: Verification email sent");
                        // Save user data temporarily
                        Preferences.setUserEmail(this, email);
                        Preferences.setUsername(this, username);

                        Toast.makeText(this,
                                getString(R.string.verification_email_sent) + " " + email,
                                Toast.LENGTH_LONG).show();

                        // Navigate to LoginActivity with email pre-filled
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e(TAG, "sendEmailVerification: Failed", task.getException());
                        Toast.makeText(this,
                                getString(R.string.failed_send_verification) + ": " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();

                        // Still allow user to proceed but they need to verify later
                        Toast.makeText(this,
                                getString(R.string.account_created_verify_later),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to LoginActivity with email pre-filled
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.username_required));
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            tilUsername.setError(getString(R.string.username_min_length));
            etUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.email_required));
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.invalid_email_format));
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.password_required));
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            tilPassword.setError(getString(R.string.password_min_length));
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.confirm_password_required));
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void handleRegistrationError(Exception exception) {
        if (exception != null && exception.getMessage() != null) {
            String msg = exception.getMessage();
            if (msg.contains("email address is already in use")) {
                tilEmail.setError(getString(R.string.email_already_registered));
                etEmail.requestFocus();
            } else if (msg.contains("network error")) {
                Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_LONG).show();
            } else if (msg.contains("weak password")) {
                tilPassword.setError(getString(R.string.password_too_weak));
                etPassword.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.registration_failed) + ": " + msg, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.registration_failed_try_again), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void showLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? getString(R.string.creating_account) : getString(R.string.register));

        etUsername.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        tvLoginNow.setEnabled(!loading);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}