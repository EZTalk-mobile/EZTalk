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

import com.example.project_ez_talk.ui.auth.welcome.WelcomeActivity;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import com.example.project_ez_talk.utils.Preferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


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


    }

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