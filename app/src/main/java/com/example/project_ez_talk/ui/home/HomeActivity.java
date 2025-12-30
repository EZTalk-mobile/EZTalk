package com.example.project_ez_talk.ui.home;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.project_ez_talk.ui.SearchActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.welcome.WelcomeActivity;
import com.example.project_ez_talk.ui.call.incoming.IncomingCallActivity;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import com.example.project_ez_talk.utils.Preferences;
import com.example.project_ez_talk.webTRC.DataModelType;
import com.example.project_ez_talk.webTRC.MainRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * HomeActivity - Main container for the app after login
 * Uses activity_main.xml layout with bottom navigation
 * Features draggable FAB that can be moved anywhere on screen
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";

    private NavController navController;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCenter;
    private MaterialToolbar toolbar;
    private ImageView ivSearch, ivNotification;

    // WebRTC for incoming calls
    private MainRepository mainRepository;
    private String currentUserId;

    // Variables for draggable FAB
    private float dX = 0f;
    private float dY = 0f;
    private float initialX = 0f;
    private float initialY = 0f;
    private boolean isDragging = false;
    private static final float DRAG_THRESHOLD = 10f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "=== HomeActivity onCreate() started ===");

        if (FirebaseAuth.getInstance().getCurrentUser() == null || !Preferences.isLoggedIn(this)) {
            Log.d(TAG, "User not authenticated, redirecting to WelcomeActivity");
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "✅ User authenticated: " + currentUserId);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "Initializing views...");
        initViews();
        Log.d(TAG, "Setting up navigation...");
        setupNavigation();
        Log.d(TAG, "Setting up toolbar...");
        setupToolbar();
        Log.d(TAG, "Setting up draggable FAB...");
        setupDraggableFab();

        // Setup WebRTC for incoming calls
        Log.d(TAG, "Setting up incoming call listener...");
        setupIncomingCallListener();
        Log.d(TAG, "✅ HomeActivity onCreate() completed");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNavigation);
        fabCenter = findViewById(R.id.fabCenter);
        ivSearch = findViewById(R.id.ivSearch);
        ivNotification = findViewById(R.id.ivNotification);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destinationId = destination.getId();

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

                boolean showBottomNav = destinationId == R.id.navigation_home ||
                        destinationId == R.id.navigation_chats ||
                        destinationId == R.id.navigation_calls ||
                        destinationId == R.id.navigation_contacts ||
                        destinationId == R.id.navigation_profile;

                bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
                fabCenter.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        // Search icon click - Opens SearchActivity
        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        ivNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableFab() {
        fabCenter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - initialX);
                        float deltaY = Math.abs(event.getRawY() - initialY);

                        if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                            isDragging = true;

                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            int screenWidth = getResources().getDisplayMetrics().widthPixels;
                            int screenHeight = getResources().getDisplayMetrics().heightPixels;
                            int fabWidth = view.getWidth();
                            int fabHeight = view.getHeight();

                            if (newX < 0) newX = 0;
                            if (newX > screenWidth - fabWidth) newX = screenWidth - fabWidth;
                            if (newY < 0) newY = 0;
                            if (newY > screenHeight - fabHeight) newY = screenHeight - fabHeight;

                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            view.performClick();
                            onFabClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    /**
     * Handle FAB click - Open AddFriendDialog
     */
    private void onFabClick() {
        try {
            AddFriendDialog dialog = new AddFriendDialog();
            dialog.setCancelable(true);
            dialog.show(getSupportFragmentManager(), "AddFriendDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Error opening dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup incoming call listener
     * This allows the user to receive video call notifications
     */
    private void setupIncomingCallListener() {
        Log.d(TAG, "=== setupIncomingCallListener() started ===");
        Log.d(TAG, "Current user ID: " + currentUserId);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "❌ Cannot setup incoming call listener - user ID is null");
            return;
        }

        // Get MainRepository instance
        mainRepository = MainRepository.getInstance();
        Log.d(TAG, "MainRepository instance obtained");

        // Login to WebRTC signaling
        Log.d(TAG, "Attempting to login to WebRTC signaling...");
        mainRepository.login(currentUserId, this, () -> {
            Log.d(TAG, "✅ Successfully logged into WebRTC signaling!");
            Log.d(TAG, "User is now online and ready to receive calls");

            // Subscribe to incoming call events
            Log.d(TAG, "Subscribing to incoming call events...");
            subscribeForLatestEvent();
            Log.d(TAG, "✅ Incoming call listener setup complete!");
        });
    }

    /**
     * Subscribe for incoming call events
     */
    private void subscribeForLatestEvent() {
        Log.d(TAG, "=== subscribeForLatestEvent() started ===");

        mainRepository.subscribeForLatestEvent(model -> {
            Log.d(TAG, "📩 Event received!");
            Log.d(TAG, "Event type: " + model.getType());
            Log.d(TAG, "Sender: " + model.getSender());
            Log.d(TAG, "Target: " + model.getTarget());

            if (model.getType() == DataModelType.StartCall) {
                Log.d(TAG, "🔔 Incoming call detected from: " + model.getSender());

                // Get caller info from Firestore
                String callerId = model.getSender();
                Log.d(TAG, "Fetching caller info from Firestore for: " + callerId);

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(callerId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String callerName = "Unknown User";
                            String callerAvatar = "";

                            if (documentSnapshot.exists()) {
                                callerName = documentSnapshot.getString("name");
                                callerAvatar = documentSnapshot.getString("profileImage");
                                Log.d(TAG, "✅ Caller info retrieved: " + callerName);
                            } else {
                                Log.w(TAG, "⚠️ Caller document not found in Firestore");
                            }

                            // Launch IncomingCallActivity
                            Log.d(TAG, "🚀 Launching IncomingCallActivity...");
                            Intent intent = new Intent(HomeActivity.this, IncomingCallActivity.class);
                            intent.putExtra("caller_id", callerId);
                            intent.putExtra("caller_name", callerName);
                            intent.putExtra("caller_avatar", callerAvatar);
                            intent.putExtra("call_type", "video");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            Log.d(TAG, "✅ IncomingCallActivity launched successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Failed to get caller info: " + e.getMessage());
                            // Launch IncomingCallActivity anyway with default info
                            Intent intent = new Intent(HomeActivity.this, IncomingCallActivity.class);
                            intent.putExtra("caller_id", callerId);
                            intent.putExtra("caller_name", "Unknown User");
                            intent.putExtra("caller_avatar", "");
                            intent.putExtra("call_type", "video");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        });
            } else {
                Log.d(TAG, "Event is not StartCall, ignoring...");
            }
        });

        Log.d(TAG, "✅ Subscribed to latest events successfully");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}