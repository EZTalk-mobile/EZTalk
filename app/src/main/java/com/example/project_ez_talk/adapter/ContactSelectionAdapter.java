package com.example.project_ez_talk.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.Contact;

import java.util.List;

public class ContactSelectionAdapter extends RecyclerView.Adapter<ContactSelectionAdapter.ViewHolder> {

    private final List<Contact> contacts;
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onClick(Contact contact);
    }

    public ContactSelectionAdapter(List<Contact> contacts, OnContactClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        // Set name
        if (holder.tvName != null) {
            holder.tvName.setText(contact.getName());
        }

        // Set phone
        if (holder.tvPhone != null) {
            holder.tvPhone.setText(contact.getPhone());
        }

        // Set avatar
        if (holder.ivAvatar != null) {
            if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(contact.getAvatarUrl())
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_profile);
            }
        }

        // Set checkbox state
        if (holder.checkbox != null) {
            holder.checkbox.setChecked(contact.isSelected());
        }

        // Handle click - toggle selection
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(contact);  // ✅ Call onClick for selection toggle
            }
        });

        // Also handle checkbox directly
        if (holder.checkbox != null) {
            holder.checkbox.setOnCheckedChangeListener(null); // Remove listener to prevent double trigger
            holder.checkbox.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(contact);  // ✅ Call onClick when checkbox clicked
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageView ivAvatar;
        CheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}