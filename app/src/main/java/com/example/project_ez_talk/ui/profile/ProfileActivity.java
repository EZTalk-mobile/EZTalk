package com.example.project_ez_talk.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_ez_talk.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {

    FloatingActionButton fabAddFriendTop, fabAddFriendBottom;
    LinearLayout llSettings, llAccount, llPrivacy;
    MaterialButton btnEditProfile;

    float dX, dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // ====== FIND VIEWS ======
        fabAddFriendTop = findViewById(R.id.fabEditPhoto); // top FAB
        fabAddFriendBottom = findViewById(R.id.fabAddFriendBottom); // bottom draggable FAB
        llSettings = findViewById(R.id.llSettings);
        llAccount = findViewById(R.id.llAccount);
        llPrivacy = findViewById(R.id.llPrivacy);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // ====== CLICK LISTENERS ======
        llSettings.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class))
        );

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        llAccount.setOnClickListener(v ->
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show()
        );

        llPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Privacy clicked", Toast.LENGTH_SHORT).show()
        );

        fabAddFriendTop.setOnClickListener(v ->
                Toast.makeText(this, "Add Friend (Top) clicked", Toast.LENGTH_SHORT).show()
        );

        // ====== DRAGGABLE FAB ======
        fabAddFriendBottom.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX);
                    v.setY(event.getRawY() + dY);
                    return true;
                default:
                    return false;
            }
        });
    }
}
