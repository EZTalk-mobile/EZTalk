package com.example.project_ez_talk.ui.auth.reset;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private EditText etNewPassword, etConfirmPassword;
    private ImageView ivToggleNewPassword, ivToggleConfirmPassword;
    private ImageView ivCheckLength, ivCheckUppercase, ivCheckNumber;
    private TextView tvCheckLength, tvCheckUppercase, tvCheckNumber;
    private MaterialButton btnResetPassword;
    private ProgressBar progressBar;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;
    private String oobCode; // For Firebase password reset

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        // Get reset code from intent (if using Firebase email link)
        oobCode = getIntent().getStringExtra("oobCode");

        initViews();
        setupListeners();
        setupPasswordValidation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        ivCheckLength = findViewById(R.id.ivCheckLength);
        ivCheckUppercase = findViewById(R.id.ivCheckUppercase);
        ivCheckNumber = findViewById(R.id.ivCheckNumber);
        tvCheckLength = findViewById(R.id.tvCheckLength);
        tvCheckUppercase = findViewById(R.id.tvCheckUppercase);
        tvCheckNumber = findViewById(R.id.tvCheckNumber);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        setSupportActionBar(toolbar);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        ivToggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(etNewPassword, ivToggleNewPassword, true));

        ivToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword, false));

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void setupPasswordValidation() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());

                // Clear error when user starts typing
                etNewPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user starts typing
                etConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePassword(String password) {
        // Check length (at least 8 characters)
        boolean hasValidLength = password.length() >= 8;
        updateRequirement(ivCheckLength, tvCheckLength, hasValidLength);

        // Check uppercase letter
        boolean hasUppercase = password.matches(".*[A-Z].*");
        updateRequirement(ivCheckUppercase, tvCheckUppercase, hasUppercase);

        // Check number
        boolean hasNumber = password.matches(".*\\d.*");
        updateRequirement(ivCheckNumber, tvCheckNumber, hasNumber);
    }

    private void updateRequirement(ImageView icon, TextView text, boolean valid) {
        int color = valid ?
                ContextCompat.getColor(this, R.color.secondary_green) :
                ContextCompat.getColor(this, R.color.text_tertiary);

        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    private void togglePasswordVisibility(EditText editText, ImageView icon, boolean isNewPassword) {
        if (isNewPassword) {
            isPasswordVisible = !isPasswordVisible;
            editText.setTransformationMethod(isPasswordVisible ? null : new PasswordTransformationMethod());
            icon.setImageResource(isPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            editText.setTransformationMethod(isConfirmPasswordVisible ? null : new PasswordTransformationMethod());
            icon.setImageResource(isConfirmPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        }

        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(newPassword, confirmPassword)) {
            return;
        }

        showLoading(true);

        // Check if user is logged in (changing password from settings)
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // User is logged in - direct password update
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Password updated successfully!",
                                    Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        } else {
                            handleResetError(task.getException());
                        }
                    });
        } else if (oobCode != null && !oobCode.isEmpty()) {
            // Using password reset link from email
            mAuth.confirmPasswordReset(oobCode, newPassword)
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Password reset successful!\n\nPlease login with your new password.",
                                    Toast.LENGTH_LONG).show();
                            navigateToLogin();
                        } else {
                            handleResetError(task.getException());
                        }
                    });
        } else {
            // No user and no reset code
            showLoading(false);
            Toast.makeText(this,
                    "Invalid reset session. Please request a new password reset.",
                    Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }

    private boolean validateInputs(String newPassword, String confirmPassword) {
        // Clear previous errors
        etNewPassword.setError(null);
        etConfirmPassword.setError(null);

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Password is required");
            etNewPassword.requestFocus();
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError("Too short");
            etNewPassword.requestFocus();
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            etNewPassword.setError("Missing uppercase");
            etNewPassword.requestFocus();
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.matches(".*\\d.*")) {
            etNewPassword.setError("Missing number");
            etNewPassword.requestFocus();
            Toast.makeText(this, "Password must contain at least one number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void handleResetError(Exception exception) {
        if (exception != null && exception.getMessage() != null) {
            String errorMsg = exception.getMessage().toLowerCase();

            if (errorMsg.contains("requires-recent-login")) {
                Toast.makeText(this,
                        "For security, please log in again before changing your password.",
                        Toast.LENGTH_LONG).show();
                navigateToLogin();

            } else if (errorMsg.contains("weak-password")) {
                etNewPassword.setError("Too weak");
                etNewPassword.requestFocus();
                Toast.makeText(this,
                        "Password is too weak. Please use a stronger password.",
                        Toast.LENGTH_LONG).show();

            } else if (errorMsg.contains("network")) {
                Toast.makeText(this,
                        "Network error. Please check your connection and try again.",
                        Toast.LENGTH_LONG).show();

            } else if (errorMsg.contains("expired")) {
                Toast.makeText(this,
                        "Reset link has expired. Please request a new one.",
                        Toast.LENGTH_LONG).show();
                navigateToLogin();

            } else {
                Toast.makeText(this,
                        "Failed to reset password: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                    "Failed to reset password. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnResetPassword.setText(loading ? "Resetting..." : getString(R.string.reset_password));
        btnResetPassword.setEnabled(!loading);

        etNewPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        ivToggleNewPassword.setEnabled(!loading);
        ivToggleConfirmPassword.setEnabled(!loading);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}