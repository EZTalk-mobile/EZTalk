package com.example.project_ez_talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_ez_talk.R;
import com.example.project_ez_talk.model.CallLog;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    private List<CallLog> callLogs;
    private Context context;
    private OnCallLogClickListener listener;
    private String currentUserId;

    public interface OnCallLogClickListener {
        void onCallClick(CallLog callLog);
        void onDeleteClick(CallLog callLog);
    }

    public CallLogAdapter(List<CallLog> callLogs, Context context, OnCallLogClickListener listener) {
        this.callLogs = callLogs;
        this.context = context;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLog log = callLogs.get(position);

        // ✅ Null safety checks
        if (log == null || log.getCallerId() == null || log.getReceiverId() == null) {
            return;
        }

        // Determine if incoming or outgoing
        boolean isIncoming = !log.getCallerId().equals(currentUserId);

        // Set caller/receiver name
        String displayName = getDisplayName(log, isIncoming);
        holder.tvName.setText(displayName);

        // Set call type icon
        setCallTypeIcon(holder, log);

        // Set call status and info
        String callInfo = getCallStatusText(log, isIncoming);
        holder.tvCallInfo.setText(callInfo);

        // Set icon color based on status
        setIconColor(holder, log);

        // Load avatar
        loadAvatar(holder, log, isIncoming);

        // Click to redial
        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(log);
            }
        });

        // Long press to delete
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(log);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    // ✅ Helper methods with null safety

    private String getDisplayName(CallLog log, boolean isIncoming) {
        String name;
        if (isIncoming) {
            name = log.getCallerName();
        } else {
            name = log.getReceiverName();
        }
        return (name != null && !name.isEmpty()) ? name : "Unknown";
    }

    private void setCallTypeIcon(ViewHolder holder, CallLog log) {
        if ("video".equals(log.getCallType())) {
            holder.ivCallType.setImageResource(R.drawable.ic_video);
        } else {
            holder.ivCallType.setImageResource(R.drawable.ic_call);
        }
    }

    private void setIconColor(ViewHolder holder, CallLog log) {
        int iconColor;
        String status = log.getStatus() != null ? log.getStatus() : "unknown";

        switch (status) {
            case "answered":
            case "ended":
            case "completed":
                iconColor = context.getColor(R.color.status_online);
                break;
            case "missed":
                iconColor = context.getColor(R.color.accent_red);
                break;
            case "rejected":
            case "declined":
                iconColor = context.getColor(R.color.accent_orange);
                break;
            default:
                iconColor = context.getColor(R.color.text_secondary);
        }
        holder.ivCallType.setColorFilter(iconColor);
    }

    private void loadAvatar(ViewHolder holder, CallLog log, boolean isIncoming) {
        String avatarUrl;
        if (isIncoming) {
            avatarUrl = log.getCallerAvatar();
        } else {
            avatarUrl = log.getReceiverAvatar();
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private String getCallStatusText(CallLog log, boolean isIncoming) {
        StringBuilder info = new StringBuilder();

        String status = log.getStatus() != null ? log.getStatus() : "unknown";

        // Add direction/status
        if (isIncoming) {
            switch (status) {
                case "missed":
                    info.append("Missed • ");
                    break;
                case "rejected":
                case "declined":
                    info.append("Declined • ");
                    break;
                default:
                    info.append("Incoming • ");
            }
        } else {
            info.append("Outgoing • ");
        }

        // Add time
        info.append(formatTime(log.getStartTime()));

        // Add duration if call was completed
        if (("ended".equals(status) || "completed".equals(status)) && log.getDuration() > 0) {
            info.append(" • ").append(formatDuration(log.getDuration()));
        }

        return info.toString();
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "Unknown time";
        }

        Date date = new Date(timestamp);
        Date now = new Date();

        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (todayFormat.format(date).equals(todayFormat.format(now))) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(date);
        }

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        if (yearFormat.format(date).equals(yearFormat.format(now))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return dateFormat.format(date);
        }

        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return fullFormat.format(date);
    }

    private String formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "0s";
        }

        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvCallInfo;
        ImageView ivCallType;
        ImageView btnCall;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCallInfo = itemView.findViewById(R.id.tv_call_info);
            ivCallType = itemView.findViewById(R.id.iv_call_type);
            btnCall = itemView.findViewById(R.id.btn_call);
        }
    }
}