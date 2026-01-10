package com.example.project_ez_talk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_ez_talk.R;
<<<<<<< HEAD
import com.example.project_ez_talk.model.CallData;
import com.example.project_ez_talk.ui.auth.welcome.WelcomeActivity;
import com.example.project_ez_talk.ui.call.incoming.IntegratedIncomingCallActivity;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import com.example.project_ez_talk.utils.Preferences;
import com.example.project_ez_talk.webrtc.FirebaseSignaling;
=======

import com.example.project_ez_talk.ui.auth.welcome.WelcomeActivity;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import com.example.project_ez_talk.utils.Preferences;
>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
<<<<<<< HEAD
import com.google.firebase.auth.FirebaseUser;
=======

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

/**
 * ✅ COMPLETE MainActivity with FIXED incoming call listener
 * Uses singleton FirebaseSignaling to ensure only ONE listener is active
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private NavController navController;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCenter;
    private MaterialToolbar toolbar;
    private ImageView ivSearch, ivNotification;

    // ✅ CRITICAL: Firebase Signaling for incoming calls (SINGLETON)
<<<<<<< HEAD
    private FirebaseSignaling firebaseSignaling;
=======

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "════════════════════════════════════════");
        Log.d(TAG, "   📱 MAIN ACTIVITY CREATED");
        Log.d(TAG, "════════════════════════════════════════");

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null || !Preferences.isLoggedIn(this)) {
            Log.d(TAG, "❌ User not logged in - redirecting to WelcomeActivity");
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        setupToolbar();
        setupFab();

<<<<<<< HEAD
        // ✅ CRITICAL: Initialize Firebase Signaling for incoming calls
        initializeIncomingCallListener();
    }

    /**
     * ✅ Initialize incoming call listener
     * This runs ONCE when MainActivity is created
     * It listens for incoming calls and shows the notification screen
     */
    private void initializeIncomingCallListener() {
        Log.d(TAG, "🔔 Initializing incoming call listener...");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "❌ User not authenticated");
            return;
        }

        String currentUserId = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() :
                (currentUser.getEmail() != null ? currentUser.getEmail() : "User");

        Log.d(TAG, "👤 Current User: " + currentUserName + " (" + currentUserId + ")");

        // ✅ FIXED: Use SINGLETON instance instead of creating new
        firebaseSignaling = FirebaseSignaling.getInstance();

        // Initialize Firebase Signaling
        firebaseSignaling.init(currentUserId, new FirebaseSignaling.OnSuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Firebase Signaling initialized successfully");

                // Start listening for incoming calls
                listenForIncomingCalls(currentUserId, currentUserName);
            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Failed to initialize Firebase Signaling");
                Toast.makeText(MainActivity.this,
                        "Failed to initialize call listener",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Listen for incoming calls
     * When someone calls this user, show the incoming call screen
     * FIXED: Only show incoming screen for OFFER signals
     */
    private void listenForIncomingCalls(String currentUserId, String currentUserName) {
        Log.d(TAG, "👂 Setting up incoming call listener...");

        firebaseSignaling.observeIncomingCalls(new FirebaseSignaling.OnCallDataListener() {
            @Override
            public void onCallDataReceived(CallData callData) {
                Log.d(TAG, "════════════════════════════════════════");
                Log.d(TAG, "   📨 SIGNAL RECEIVED");
                Log.d(TAG, "════════════════════════════════════════");
                Log.d(TAG, "Type: " + callData.getType());
                Log.d(TAG, "From: " + callData.getSenderId());

                // ✅ FIXED: Only process OFFER signals (actual incoming calls)
                // Ignore ACCEPT, REJECT, END - those are handled in VoiceCallActivity
                if (callData.getType() != CallData.Type.OFFER) {
                    Log.d(TAG, "⚠️ Ignoring " + callData.getType() + " signal");
                    return;
                }

                String callerId = callData.getSenderId();
                String callerName = callData.getData() != null ? callData.getData() : "Unknown";
                String callType = callData.getCallType() != null ? callData.getCallType() : "voice";

                Log.d(TAG, "📱 Incoming from: " + callerName + " (" + callerId + ")");
                Log.d(TAG, "🎤 Type: " + callType);

                // Show incoming call screen
                showIncomingCallScreen(callerId, callerName, callType, currentUserId);
            }

            /**
             * @param callData
             */
            @Override
            public void onOffer(CallData callData) {

            }

            /**
             * @param callData
             */
            @Override
            public void onAnswer(CallData callData) {

            }

            /**
             * @param callData
             */
            @Override
            public void onIceCandidate(CallData callData) {

            }

            /**
             * @param callData
             */
            @Override
            public void onAccept(CallData callData) {

            }

            /**
             * @param callData
             */
            @Override
            public void onReject(CallData callData) {

            }

            @Override
            public void onError() {
                Log.e(TAG, "❌ Error listening for incoming calls");
            }
        });

        Log.d(TAG, "✅ Incoming call listener started");
    }

    /**
     * ✅ Show incoming call screen
     */
    private void showIncomingCallScreen(String callerId, String callerName,
                                        String callType, String currentUserId) {
        Log.d(TAG, "🔔 Opening incoming call screen for: " + callerName);

        Intent intent = new Intent(this, IntegratedIncomingCallActivity.class);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_ID, callerId);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_NAME, callerName);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALLER_AVATAR, "");
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CALL_TYPE, callType);
        intent.putExtra(IntegratedIncomingCallActivity.EXTRA_CURRENT_USER_ID, currentUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        Log.d(TAG, "✅ Incoming call screen launched");
    }

    /**
     * Initialize UI views
     */
=======

    }

>>>>>>> 61984a43d5c4b52195ebbb52041a92899843b7f3
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNavigation);
        fabCenter = findViewById(R.id.fabCenter);
        ivSearch = findViewById(R.id.ivSearch);
        ivNotification = findViewById(R.id.ivNotification);

        Log.d(TAG, "✅ Views initialized");
    }

    /**
     * Setup navigation between fragments
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Update toolbar title based on current destination
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destinationId = destination.getId();

                // Update toolbar title
                if (destinationId == R.id.navigation_home) {
                    toolbar.setTitle(R.string.home);
                } else if (destinationId == R.id.navigation_chats) {
                    toolbar.setTitle(R.string.chats);
                } else if (destinationId == R.id.navigation_calls) {
                    toolbar.setTitle(R.string.calls);
                } else if (destinationId == R.id.navigation_contacts) {
                    toolbar.setTitle(R.string.contacts);
                } else if (destinationId == R.id.navigation_profile) {
                    toolbar.setTitle(R.string.profile);
                }

                // Show/hide bottom nav and FAB for specific destinations
                boolean showBottomNav = destinationId == R.id.navigation_home ||
                        destinationId == R.id.navigation_chats ||
                        destinationId == R.id.navigation_calls ||
                        destinationId == R.id.navigation_contacts ||
                        destinationId == R.id.navigation_profile;

                bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
                fabCenter.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            });

            Log.d(TAG, "✅ Navigation setup complete");
        }
    }

    /**
     * Setup toolbar buttons and actions
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);

        // Search button click - Opens SearchActivity
        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            Log.d(TAG, "🔍 Search button clicked");
        });

        // Notification button click
        ivNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "🔔 Notification button clicked");
            // TODO: Show notifications or navigate to NotificationActivity
        });

        Log.d(TAG, "✅ Toolbar setup complete");
    }

    /**
     * Setup FAB (Floating Action Button)
     */
    private void setupFab() {
        fabCenter.setOnClickListener(v -> {
            Log.d(TAG, "➕ FAB clicked - opening add friend dialog");
            openAddFriendDialog();
        });

        Log.d(TAG, "✅ FAB setup complete");
    }

    /**
     * Open add friend dialog
     */
    private void openAddFriendDialog() {
        AddFriendDialog dialog = new AddFriendDialog();
        dialog.show(getSupportFragmentManager(), "add_friend_dialog");
    }

    /**
     * Handle back navigation
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * Cleanup when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔌 MainActivity.onDestroy called");

        // ✅ IMPORTANT: KEEP LISTENING EVEN IF ACTIVITY IS DESTROYED
        // The singleton will continue listening in the background
        // Only cleanup if app is truly closing (isChangingConfigurations checks for config changes)

        if (isChangingConfigurations()) {
            Log.d(TAG, "Configuration changing, keeping Firebase Signaling active");
        } else {
            Log.d(TAG, "Activity destroyed, but Firebase Signaling listener stays active (singleton)");
        }

        // ✅ DO NOT CALL CLEANUP - let singleton handle it
        // The listener must stay active even if MainActivity is destroyed
    }

    /**
     * Activity lifecycle - when app comes to foreground
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ MainActivity.onResume - App is now visible");
    }

    /**
     * Activity lifecycle - when app goes to background
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ MainActivity.onPause - App is now hidden");
    }

    /**
     * Activity lifecycle - when activity regains focus
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "▶️ MainActivity.onStart");
    }

    /**
     * Activity lifecycle - when activity loses focus
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "⏹️ MainActivity.onStop");
    }
}