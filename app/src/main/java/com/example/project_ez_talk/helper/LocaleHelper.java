package com.example.project_ez_talk.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * LocaleHelper - Utility class for managing app language/locale
 * Handles language switching between English (en) and Khmer (km)
 */
@SuppressWarnings("deprecation")
public class LocaleHelper {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_LANGUAGE_CODE = "language_code";
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * Set and persist app locale
     * Call this when user changes language in settings
     *
     * Usage: LocaleHelper.setLocale(context, "km");
     */
    public static void setLocale(Context context, String languageCode) {
        // Validate language code
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = DEFAULT_LANGUAGE;
        }

        // Save language preference FIRST
        saveLanguagePreference(context, languageCode);

        // Then apply locale
        applyLocale(context, languageCode);
    }

    /**
     * Apply saved locale from SharedPreferences
     * Call this in attachBaseContext() of every Activity (via BaseActivity)
     */
    public static Context setLocale(Context context) {
        String languageCode = getLanguagePreference(context);
        updateResources(context, languageCode);
        return context.createConfigurationContext(getConfiguration(context, languageCode));
    }

    /**
     * Get currently saved language code
     */
    public static String getLanguage(Context context) {
        return getLanguagePreference(context);
    }

    /**
     * Apply locale to context (for immediate UI update)
     */
    @SuppressLint("ObsoleteSdkInt")
    private static void applyLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    /**
     * Get configuration with specified locale
     */
    @SuppressLint("ObsoleteSdkInt")
    private static Configuration getConfiguration(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Configuration config = new Configuration(context.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        return config;
    }

    /**
     * Update resources with new locale
     */
    @SuppressLint("ObsoleteSdkInt")
    private static void updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /**
     * Save language preference to SharedPreferences
     */
    private static void saveLanguagePreference(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply();
    }

    /**
     * Get saved language preference from SharedPreferences
     */
    private static String getLanguagePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE);
    }
}