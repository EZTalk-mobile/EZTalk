package com.example.project_ez_talk.ui.profile;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_ez_talk.R;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout llLanguage;
    private TextView tvCurrentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        llLanguage = findViewById(R.id.llLanguage);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);

        llLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "French", "Khmer"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setItems(languages, (dialog, which) ->
                tvCurrentLanguage.setText(languages[which])
        );
        builder.show();
    }
}
