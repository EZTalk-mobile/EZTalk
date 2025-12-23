package com.example.project_ez_talk.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_ez_talk.helper.LocaleHelper;

/**
 * BaseActivity - Base class for all activities
 * Automatically applies saved language preference
 *
 * All your activities should extend this instead of AppCompatActivity
 *
 * Example:
 * public class HomeActivity extends BaseActivity {
 *     // Your code here
 * }
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Any common initialization can go here
    }
}