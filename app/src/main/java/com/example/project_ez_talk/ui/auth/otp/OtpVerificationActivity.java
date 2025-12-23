package com.example.project_ez_talk.ui.auth.otp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.home.HomeActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OtpVerificationActivity extends BaseActivity {

    private ImageView btnBack;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private MaterialButton btnVerify;
    private TextView tvResend, tvSubtitle;
    private String email;
    private String username;
    private boolean requiresEmailCheck;
    private FirebaseAuth mAuth;
    private CountDownTimer resendTimer;
    private int resendCountdown = 60; // 60 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");
        requiresEmailCheck = getIntent().getBooleanExtra("requiresEmailCheck", false);

        initViews();
        setupOtpInputs();
        setupListeners();

        // Update subtitle with email
        if (email != null) {
            tvSubtitle.setText("We've sent a verification link to\n" + email);
        }

        // Auto-focus on first input
        etOtp1.requestFocus();

        // Start resend countdown
        startResendTimer();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);
        tvSubtitle = findViewById(R.id.tvSubtitle);
    }

    private void setupOtpInputs() {
        // Add text watchers for auto-advance
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, null));

        // Add backspace handlers
        setupBackspaceHandler(etOtp1, null);
        setupBackspaceHandler(etOtp2, etOtp1);
        setupBackspaceHandler(etOtp3, etOtp2);
        setupBackspaceHandler(etOtp4, etOtp3);
    }

    private void setupBackspaceHandler(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.requestFocus();
                    previous.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            if (requiresEmailCheck) {
                checkEmailVerification();
            } else {
                verifyOtp();
            }
        });

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendVerificationEmail();
            }
        });
    }

    private void checkEmailVerification() {
        showLoading(true);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showLoading(false);
            Toast.makeText(this, "Session expired. Please register again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reload user to get latest email verification status
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser reloadedUser = mAuth.getCurrentUser();
                if (reloadedUser != null && reloadedUser.isEmailVerified()) {
                    // Email is verified!
                    onVerificationSuccess();
                } else {
                    showLoading(false);
                    Toast.makeText(this,
                            "Email not verified yet. Please check your inbox and click the verification link.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                showLoading(false);
                Toast.makeText(this,
                        "Failed to check verification status: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp() {
        String otp = getOtpCode();

        if (otp.length() != 4) {
            Toast.makeText(this, "Please enter complete code", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // For demo/testing: Accept 1234 as valid OTP
        if (otp.equals("1234")) {
            onVerificationSuccess();
        } else {
            showLoading(false);
            Toast.makeText(this, "Invalid code. Please try again.", Toast.LENGTH_SHORT).show();
            clearOtp();
            etOtp1.requestFocus();
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Verification email resent to " + email,
                                    Toast.LENGTH_SHORT).show();
                            clearOtp();
                            etOtp1.requestFocus();
                            startResendTimer();
                        } else {
                            Toast.makeText(this,
                                    "Failed to resend email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void onVerificationSuccess() {
        // Save login state
        Preferences.setLoggedIn(this, true);
        if (email != null) {
            Preferences.setUserEmail(this, email);
        }
        if (username != null) {
            Preferences.setUsername(this, username);
        }

        Toast.makeText(this, "Verification successful! Welcome to EZ Talk!", Toast.LENGTH_SHORT).show();

        // Navigate to HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startResendTimer() {
        tvResend.setEnabled(false);
        resendCountdown = 60;

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendCountdown = (int) (millisUntilFinished / 1000);
                tvResend.setText("Resend (" + resendCountdown + "s)");
            }

            @Override
            public void onFinish() {
                tvResend.setText(R.string.resend);
                tvResend.setEnabled(true);
            }
        };
        resendTimer.start();
    }

    private String getOtpCode() {
        return etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString();
    }

    private void clearOtp() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
    }

    private void showLoading(boolean loading) {
        btnVerify.setEnabled(!loading);
        btnVerify.setText(loading ? "Verifying..." : getString(R.string.verify));

        etOtp1.setEnabled(!loading);
        etOtp2.setEnabled(!loading);
        etOtp3.setEnabled(!loading);
        etOtp4.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }

    private class OtpTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            if (text.length() == 1) {
                // Move to next field
                if (nextView != null) {
                    nextView.requestFocus();
                } else {
                    // Last field - hide keyboard
                    currentView.clearFocus();
                }
            } else if (text.length() > 1) {
                // Handle paste - take only first character
                currentView.setText(String.valueOf(text.charAt(0)));
                currentView.setSelection(1);
            }
        }
    }
}