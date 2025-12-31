package com.example.project_ez_talk.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.example.project_ez_talk.R;
import com.example.project_ez_talk.adapter.OnboardingAdapter;
import com.example.project_ez_talk.ui.BaseActivity;
import com.example.project_ez_talk.ui.auth.welcome.WelcomeActivity;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends BaseActivity {

    private ViewPager2 vpOnboarding;
    private LinearLayout indicatorLayout;
    private MaterialButton btnNext;
    private ImageView btnSkip;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupViewPager();
        setupListeners();
    }

    private void initViews() {
        vpOnboarding = findViewById(R.id.vp_onboarding);
        indicatorLayout = findViewById(R.id.indicator_layout);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter();
        vpOnboarding.setAdapter(adapter);

        setupIndicators();
        setCurrentIndicator(0);

        vpOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText(R.string.get_started);
                } else {
                    btnNext.setText(R.string.next);
                }
            }
        });
    }

    private void setupIndicators() {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (8 * getResources().getDisplayMetrics().density),
                    (int) (8 * getResources().getDisplayMetrics().density)
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_inactive);
            indicatorLayout.addView(indicator);
        }
    }

    private void setCurrentIndicator(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View indicator = indicatorLayout.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive);
            }
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (vpOnboarding.getCurrentItem() < adapter.getItemCount() - 1) {
                vpOnboarding.setCurrentItem(vpOnboarding.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("onboarding_shown", true);
        editor.apply();

        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}