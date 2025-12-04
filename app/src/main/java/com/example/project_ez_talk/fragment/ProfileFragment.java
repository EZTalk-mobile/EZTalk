package com.example.project_ez_talk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_ez_talk.activity.EditProfileActivity;
import com.example.project_ez_talk.activity.SettingsActivity;
import com.example.project_ez_talk.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private MaterialButton btnEditProfile;
    private LinearLayout llSettings;
    private FloatingActionButton fabAddFriend;

    private float dX, dY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        llSettings = view.findViewById(R.id.llSettings);

        // Add Friend Floating Button
        fabAddFriend = new FloatingActionButton(requireContext());
        fabAddFriend.setImageResource(R.drawable.ic_add);
        fabAddFriend.setBackgroundTintList(getResources().getColorStateList(R.color.primary_purple));
        fabAddFriend.setContentDescription("Add Friend");

        // Add FAB to layout
        ((ViewGroup) view).addView(fabAddFriend);

        // Initial position
        fabAddFriend.setX(800);
        fabAddFriend.setY(1200);

        // Drag functionality
        fabAddFriend.setOnTouchListener((v, event) -> {
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

        // Click Add Friend
        fabAddFriend.setOnClickListener(v ->
                Toast.makeText(getContext(), "Add Friend clicked", Toast.LENGTH_SHORT).show()
        );

        // Click Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        // Click Settings
        llSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        return view;
    }
}
