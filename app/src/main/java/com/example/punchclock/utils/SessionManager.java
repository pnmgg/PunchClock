package com.example.punchclock.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.punchclock.data.User;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_REMEMBERED = "is_remembered";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";

    // Expiration times (in milliseconds)
    private static final long EXPIRATION_REMEMBER_ME = 30L * 24 * 60 * 60 * 1000; // 30 days
    private static final long EXPIRATION_DEFAULT = 24L * 60 * 60 * 1000; // 24 hours

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(long userId, String username, String role, boolean isRemembered) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_REMEMBERED, isRemembered);
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            return false;
        }

        // Check expiration
        long loginTime = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0);
        boolean isRemembered = prefs.getBoolean(KEY_IS_REMEMBERED, false);
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - loginTime;

        long expirationTime = isRemembered ? EXPIRATION_REMEMBER_ME : EXPIRATION_DEFAULT;

        if (duration > expirationTime) {
            // Session expired
            logoutUser();
            return false;
        }

        return true;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "USER");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }
}
