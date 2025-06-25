package com.example.libraryproject; // Or your app's package name

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LibraryAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_EMAIL = "userEmail"; // Added for student profile

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    // Mode for SharedPreferences
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(int userId, String username, String role, String email) {
        // Storing login value as TRUE
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Storing user details in pref
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_EMAIL, email); // Store email

        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get stored session data
     * */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1); // Return -1 if not found
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, null);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null); // Get email
    }


    /**
     * Clear session details
     * */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        // Intent i = new Intent(_context, LoginActivity.class);
        // Add flags to clear activity stack
        // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // _context.startActivity(i);
        // Note: The redirection logic is commented out here as it's better
        // to handle it in the Activity/Fragment where logout is called.
    }
}