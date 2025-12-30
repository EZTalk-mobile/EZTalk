package com.example.project_ez_talk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter() {
        this.onboardingItems = getOnboardingItems();
    }

    private List<OnboardingItem> getOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(
                R.drawable.onboarding_1,
                "Connect with Friends",
                "Stay in touch with your friends and family through instant messaging"
        ));

        items.add(new OnboardingItem(
                R.drawable.onboarding_2,
                "Video & Voice Calls",
                "Make high-quality video and voice calls anytime, anywhere"
        ));

        items.add(new OnboardingItem(
                R.drawable.onboarding_3,
                "Share Moments",
                "Share photos, videos, and special moments with people you care about"
        ));

        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnboardingItem item = onboardingItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivOnboarding;
        private final TextView tvTitle;
        private final TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOnboarding = itemView.findViewById(R.id.iv_onboarding);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        public void bind(OnboardingItem item) {
            ivOnboarding.setImageResource(item.getImageRes());
            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
        }
    }
}