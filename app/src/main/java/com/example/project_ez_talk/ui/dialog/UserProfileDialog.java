package com.example.project_ez_talk.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.ui.chat.detail.ChatDetailActivity;

public class UserProfileDialog extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_AVATAR = "avatar_url";
    private static final String ARG_BIO = "bio";
    private static final String ARG_IS_ONLINE = "is_online";

    public static UserProfileDialog newInstance(
            String userId,
            String userName,
            String username,
            String avatarUrl,
            String bio,
            boolean isOnline
    ) {
        UserProfileDialog dialog = new UserProfileDialog();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_USER_NAME, userName);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_AVATAR, avatarUrl);
        args.putString(ARG_BIO, bio);
        args.putBoolean(ARG_IS_ONLINE, isOnline);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user_profile, container, false);

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return view;
        }

        String userId = args.getString(ARG_USER_ID);
        String userName = args.getString(ARG_USER_NAME);
        String username = args.getString(ARG_USERNAME);
        String avatarUrl = args.getString(ARG_AVATAR);
        String bio = args.getString(ARG_BIO, "Hey there! I'm using EZ Talk");
        boolean isOnline = args.getBoolean(ARG_IS_ONLINE, false);

        ImageView ivClose = view.findViewById(R.id.ivClose);
        ImageView ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        View viewOnlineStatus = view.findViewById(R.id.viewOnlineStatus);
        TextView tvOnlineStatus = view.findViewById(R.id.tvOnlineStatus);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvBio = view.findViewById(R.id.tvBio);
        LinearLayout layoutMessage = view.findViewById(R.id.layoutMessage);
        LinearLayout layoutVoiceCall = view.findViewById(R.id.layoutVoiceCall);
        LinearLayout layoutVideoCall = view.findViewById(R.id.layoutVideoCall);

        // Set data
        tvUserName.setText(userName);
        tvUsername.setText(username != null && !username.isEmpty() ? "@" + username : "");
        tvBio.setText(bio);

        // Online status
        if (isOnline) {
            viewOnlineStatus.setVisibility(View.VISIBLE);
            tvOnlineStatus.setText("Online");
        } else {
            viewOnlineStatus.setVisibility(View.GONE);
            tvOnlineStatus.setText("Offline");
        }
        tvOnlineStatus.setVisibility(View.VISIBLE);

        // Avatar
        Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(ivProfilePicture);

        // Actions
        ivClose.setOnClickListener(v -> dismiss());

        layoutMessage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatDetailActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("user_name", userName);
            intent.putExtra("user_avatar", avatarUrl);
            startActivity(intent);
            dismiss();
        });

        layoutVoiceCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Voice call coming soon!", Toast.LENGTH_SHORT).show();
        });

        layoutVideoCall.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Video call coming soon!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}