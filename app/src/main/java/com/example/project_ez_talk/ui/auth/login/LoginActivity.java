package com.example.project_ez_talk.ui.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.service.IncomingCallListenerService;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.forgot.ForgotPasswordActivity;
import com.example.project_ez_talk.ui.auth.signup.RegisterActivity;
import com.example.project_ez_talk.ui.home.HomeActivity;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private MaterialCardView btnGoogle, btnFacebook, btnApple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupClickListeners();

        // Auto-fill email from intent (from RegisterActivity) or saved preferences
        String prefilledEmail = getIntent().getStringExtra("email");
        if (!TextUtils.isEmpty(prefilledEmail)) {
            etUsername.setText(prefilledEmail);
            etUsername.setSelection(prefilledEmail.length());
            etPassword.requestFocus();
        } else {
            String savedEmail = Preferences.getUserEmail(this);
            if (!TextUtils.isEmpty(savedEmail)) {
                etUsername.setText(savedEmail);
                etPassword.requestFocus();
            }
        }

        // Only auto login if NOT coming from RegisterActivity
        if (prefilledEmail == null && mAuth.getCurrentUser() != null && Preferences.isLoggedIn(this)) {
            goToHome();
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnApple = findViewById(R.id.btnApple);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        findViewById(R.id.tvRegisterNow).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Facebook and Apple buttons - placeholder for future implementation
        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Facebook login coming soon", Toast.LENGTH_SHORT).show());
        btnApple.setOnClickListener(v ->
                Toast.makeText(this, "Apple login coming soon", Toast.LENGTH_SHORT).show());
    }

    // ============ EMAIL/PASSWORD LOGIN ============
    private void attemptLogin() {
        clearErrors();

        String username = getText(etUsername);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.email_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.password_required));
            return;
        }

        showLoading(true, false);

        String email = username.contains("@") ? username : username + "@eztalk.com";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false, false);

                    if (task.isSuccessful()) {
                        Preferences.setLoggedIn(this, true);
                        Preferences.setUserEmail(this, email);
                        Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                        // ✅ START INCOMING CALL LISTENER SERVICE
                        startIncomingCallListenerService();

                        goToHome();
                    } else {
                        String msg = task.getException() != null ?
                                task.getException().getMessage() : getString(R.string.login_failed);

                        if (msg.contains("no user record") || msg.contains("badly formatted")) {
                            tilUsername.setError(getString(R.string.user_not_found));
                        } else if (msg.contains("password is invalid")) {
                            tilPassword.setError(getString(R.string.wrong_password));
                        } else {
                            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ============ GOOGLE SIGN-IN ============
    private void signInWithGoogle() {
        showLoading(true, true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName(), account.getEmail());
                }
            } catch (ApiException e) {
                showLoading(false, true);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String displayName, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if this is a new user
                            boolean isNewUser = task.getResult().getAdditionalUserInfo() != null &&
                                    task.getResult().getAdditionalUserInfo().isNewUser();

                            if (isNewUser) {
                                // New user - create profile in Firestore
                                createGoogleUserProfileInFirestore(user, displayName, email);
                            } else {
                                // Existing user - just login
                                loginSuccess(email);
                            }
                        }
                    } else {
                        showLoading(false, true);
                        Toast.makeText(LoginActivity.this,
                                "Firebase authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createGoogleUserProfileInFirestore(FirebaseUser firebaseUser, String displayName, String email) {
        String userId = firebaseUser.getUid();

        // Set display name if not already set
        if (firebaseUser.getDisplayName() == null || firebaseUser.getDisplayName().isEmpty()) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            firebaseUser.updateProfile(profileUpdates);
        }

        // Create user profile in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("online", true);
        userData.put("userId", userId);
        userData.put("email", email);
        userData.put("name", displayName != null ? displayName : "User");
        userData.put("status", "online");
        userData.put("profilePicture", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        userData.put("bio", "");
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastSeen", System.currentTimeMillis());
        userData.put("signInMethod", "google");

        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    loginSuccess(email);
                })
                .addOnFailureListener(e -> {
                    // Still allow login even if profile creation fails
                    loginSuccess(email);
                });
    }

    private void loginSuccess(String email) {
        showLoading(false, true);
        Preferences.setLoggedIn(this, true);
        Preferences.setUserEmail(this, email);
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        // ✅ START INCOMING CALL LISTENER SERVICE
        startIncomingCallListenerService();

        goToHome();
    }

    /**
     * ✅ START INCOMING CALL LISTENER SERVICE
     * This is the critical method - starts the service that listens for incoming calls
     */
    private void startIncomingCallListenerService() {
        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "📱 Starting IncomingCallListenerService");
        Log.d(TAG, "════════════════════════════════════════");

        try {
            Intent serviceIntent = new Intent(LoginActivity.this, IncomingCallListenerService.class);
            startService(serviceIntent);
            Log.d(TAG, "✅ IncomingCallListenerService started successfully!");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting IncomingCallListenerService: " + e.getMessage());
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLoading(boolean loading, boolean isGoogleSignIn) {
        if (isGoogleSignIn) {
            btnGoogle.setEnabled(!loading);
        } else {
            btnLogin.setEnabled(!loading);
            btnLogin.setText(loading ? getString(R.string.logging_in) : getString(R.string.login));
        }
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}