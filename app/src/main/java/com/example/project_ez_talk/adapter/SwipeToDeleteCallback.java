package com.example.project_ez_talk.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_ez_talk.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private static final String TAG = "SwipeToDeleteCallback";
    private static final float SWIPE_THRESHOLD = 0.3f;

    private Context context;
    private Paint paint;
    private Paint clearPaint;
    private ColorDrawable background;
    private int backgroundColor;
    private Drawable deleteIcon;
    private int intrinsicWidth;
    private int intrinsicHeight;

    private final MessageAdapter adapter;

    public SwipeToDeleteCallback(Context context, MessageAdapter adapter) {
        this.context = context;
        this.adapter = adapter;

        // Initialize paint
        this.paint = new Paint();
        this.paint.setAntiAlias(true);

        // Clear paint for text
        this.clearPaint = new Paint();
        this.clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Background color
        this.backgroundColor = Color.parseColor("#FF5252"); // Red
        this.background = new ColorDrawable();

        // Try to load delete icon (optional - create ic_delete.xml if you want icon)
        try {
            this.deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
            if (deleteIcon != null) {
                deleteIcon.setTint(Color.WHITE);
                intrinsicWidth = deleteIcon.getIntrinsicWidth();
                intrinsicHeight = deleteIcon.getIntrinsicHeight();
            }
        } catch (Exception e) {
            Log.w(TAG, "Delete icon not found, using text only");
        }
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Allow swipe left only (right swipe can be enabled by adding ItemTouchHelper.RIGHT)
        int swipeFlags = ItemTouchHelper.LEFT;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // Not used for swipe
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        try {
            int position = viewHolder.getAdapterPosition();
            Log.d(TAG, "✅ Swipe detected at position: " + position);

            if (position != RecyclerView.NO_POSITION && position >= 0 && position < adapter.getMessages().size()) {
                adapter.deleteMessageAtPosition(position);
            } else {
                Log.e(TAG, "❌ Invalid position: " + position);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in onSwiped: " + e.getMessage(), e);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {

            // Get item view bounds
            android.view.View itemView = viewHolder.itemView;
            int itemHeight = itemView.getBottom() - itemView.getTop();

            // Draw red background
            background.setColor(backgroundColor);
            background.setBounds(
                    (int) (itemView.getRight() + dX),
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
            background.draw(c);

            // Calculate delete icon/text position
            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
            int deleteIconRight = itemView.getRight() - deleteIconMargin;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;

            // Draw delete icon if available
            if (deleteIcon != null && Math.abs(dX) > intrinsicWidth + deleteIconMargin) {
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);
            }

            // Draw "DELETE" text
            paint.setColor(Color.WHITE);
            paint.setTextSize(40f);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setAntiAlias(true);

            // Position text to the left of icon or on right edge
            float textX = itemView.getRight() - deleteIconMargin - intrinsicWidth - 20;
            if (deleteIcon == null) {
                textX = itemView.getRight() - 40;
            }

            float textY = itemView.getTop() + (itemHeight / 2f) + (paint.getTextSize() / 3f);

            // Only draw text if there's enough space
            if (Math.abs(dX) > 100) {
                c.drawText("DELETE", textX, textY, paint);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 10; // Makes swipe more responsive
    }
}