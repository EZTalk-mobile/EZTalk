package com.example.project_ez_talk.ui.auth.forgot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private MaterialToolbar toolbar;
    private EditText etEmail;
    private MaterialButton btnSendCode;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        progressBar = findViewById(R.id.progressBar);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        btnSendCode.setOnClickListener(v -> sendResetEmail());

        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Clear any previous errors
        etEmail.setError(null);

        showLoading(true);

        // Send password reset email via Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Success - show message and redirect
                        Toast.makeText(this,
                                "Password reset email sent to " + email + "\n\nPlease check your inbox and spam folder.",
                                Toast.LENGTH_LONG).show();

                        // Navigate back to login
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle errors
                        handleResetError(task.getException());
                    }
                });
    }

    private void handleResetError(Exception exception) {
        if (exception != null && exception.getMessage() != null) {
            String errorMsg = exception.getMessage().toLowerCase();

            if (errorMsg.contains("no user") ||
                    errorMsg.contains("user not found") ||
                    errorMsg.contains("no user record") ||
                    errorMsg.contains("user-not-found")) {

                etEmail.setError("No account found with this email");
                etEmail.requestFocus();
                Toast.makeText(this,
                        "No account exists with this email address.",
                        Toast.LENGTH_LONG).show();

            } else if (errorMsg.contains("badly formatted") ||
                    errorMsg.contains("invalid-email")) {

                etEmail.setError("Invalid email format");
                etEmail.requestFocus();
                Toast.makeText(this,
                        "Please enter a valid email address.",
                        Toast.LENGTH_SHORT).show();

            } else if (errorMsg.contains("network")) {
                Toast.makeText(this,
                        "Network error. Please check your internet connection and try again.",
                        Toast.LENGTH_LONG).show();

            } else if (errorMsg.contains("too-many-requests")) {
                Toast.makeText(this,
                        "Too many attempts. Please try again in a few minutes.",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this,
                        "Failed to send reset email: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                    "Failed to send reset email. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean loading) {
        btnSendCode.setEnabled(!loading);
        btnSendCode.setText(loading ? "Sending..." : getString(R.string.send_code));
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        etEmail.setEnabled(!loading);
        tvBackToLogin.setEnabled(!loading);
    }
}