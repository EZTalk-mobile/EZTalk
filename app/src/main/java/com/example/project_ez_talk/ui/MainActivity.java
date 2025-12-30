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
import com.example.project_ez_talk.ui.call.incoming.IncomingCallActivity;
import com.example.project_ez_talk.ui.profile.AddFriendDialog;
import com.example.project_ez_talk.utils.Preferences;
import com.example.project_ez_talk.webTRC.MainRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import android.content.Context;

public class MainActivity extends BaseActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabCenter;
    private MaterialToolbar toolbar;
    private ImageView ivSearch, ivNotification;
    private static final String DATABASE_URL =
            "https://project-ez-talk-dccea-default-rtdb.europe-west1.firebasedatabase.app";
   private DatabaseReference rootRef;
   private MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("MainActivity", "======================================");
        Log.d("MainActivity", "=== MainActivity onCreate() START ===");
        Log.d("MainActivity", "======================================");

        // Initialize Firebase Database with correct region URL
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
            Log.d("MainActivity", "✅ Firebase Database initialized: " + DATABASE_URL);
        } catch (Exception e) {
            Log.e("MainActivity", "❌ Error initializing Firebase Database: " + e.getMessage());
        }
        

        // Check if user is logged in
        Log.d("MainActivity", "Checking user authentication...");
        if (FirebaseAuth.getInstance().getCurrentUser() == null || !Preferences.isLoggedIn(this)) {
            Log.d("MainActivity", "❌ User not logged in, redirecting to WelcomeActivity");
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        
        Log.d("MainActivity", "✅ User is authenticated");

        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Initializing views...");
        initViews();
        Log.d("MainActivity", "Setting up navigation...");
        setupNavigation();
        Log.d("MainActivity", "Setting up toolbar...");
        setupToolbar();
        Log.d("MainActivity", "Setting up FAB...");
        setupFab();
        Log.d("MainActivity", "Calling setupIncomingCallListener...");
        setupIncomingCallListener();
        Log.d("MainActivity", "✅ onCreate() COMPLETE");
    }

    private void setupIncomingCallListener() {
        Log.d("MainActivity", "=== setupIncomingCallListener() ===");
        
        // Get MainRepository instance
        mainRepository = MainRepository.getInstance();
        
        // Login to Firebase with current user ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null 
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
            : null;
        
        Log.d("MainActivity", "Current User ID: " + currentUserId);
            
        if (currentUserId != null) {
            Log.d("MainActivity", "Attempting to login to WebRTC signaling...");
            mainRepository.login(currentUserId, this, () -> {
                Log.d("MainActivity", "✅ Logged in to WebRTC signaling successfully");
                Log.d("MainActivity", "User should now be visible in database: " + currentUserId);
                
                // Subscribe to incoming call events
                Log.d("MainActivity", "Subscribing to incoming call events...");
                mainRepository.subscribeForLatestEvent(model -> {
                    Log.d("MainActivity", "📩 Event received - Type: " + model.getType() + ", Sender: " + model.getSender());
                    if (model.getType() == com.example.project_ez_talk.webTRC.DataModelType.StartCall) {
                        // Show incoming call screen
                        Log.d("MainActivity", "🔔 Incoming call detected from: " + model.getSender());
                        
                        // Fetch caller info from Firestore
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(model.getSender())
                                .get()
                                .addOnSuccessListener(doc -> {
                                    String callerName = doc.exists() ? doc.getString("name") : model.getSender();
                                    String callerAvatar = doc.exists() ? doc.getString("avatarUrl") : "";
                                    
                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, model.getSender());
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerName);
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_AVATAR, callerAvatar);
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, "video");
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        Log.d("MainActivity", "✅ IncomingCallActivity launched");
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Failed to fetch caller info: " + e.getMessage());
                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, model.getSender());
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, model.getSender());
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, "video");
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    });
                                });
                    }
                });
                Log.d("MainActivity", "✅ Subscribed to incoming call events");
            });
        } else {
            Log.e("MainActivity", "❌ ERROR: Current user ID is null - cannot login to WebRTC");
        }
    }





    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNavigation);
        fabCenter = findViewById(R.id.fabCenter);
        ivSearch = findViewById(R.id.ivSearch);
        ivNotification = findViewById(R.id.ivNotification);
        FirebaseDatabase.getInstance().getReference("testValue")
                .setValue("hello world")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Write OK", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Write FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        Log.d("CHANNEL_DEBUG", "Send button clicked");
    }

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
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        // Search button click - Opens SearchActivity
        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Notification button click
        ivNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            // TODO: Show notifications or navigate to NotificationActivity
        });
    }

    private void setupFab() {
        fabCenter.setOnClickListener(v -> openAddFriendDialog());
    }

    private void openAddFriendDialog() {
        AddFriendDialog dialog = new AddFriendDialog();
        dialog.show(getSupportFragmentManager(), "add_friend_dialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}