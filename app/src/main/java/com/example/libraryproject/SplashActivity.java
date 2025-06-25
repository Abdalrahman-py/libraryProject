package com.example.libraryproject; // Your package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // For splash screen delay
import android.os.Looper;  // For splash screen delay
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// If you are using View Binding for the splash screen layout (if it has one)
// import com.example.libraryproject.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    // Optional: If your splash screen has a layout and you use View Binding
    // private ActivitySplashBinding binding;

    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If your splash screen has a layout:
        // binding = ActivitySplashBinding.inflate(getLayoutInflater());
        // setContentView(binding.getRoot());
        // OR if not using View Binding for splash (or no complex UI):
        setContentView(R.layout.activity_splash); // Assuming you have activity_splash.xml

        sessionManager = new SessionManager(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // This code will be executed after the SPLASH_TIMEOUT
            checkUserSession();
        }, SPLASH_TIMEOUT);
    }

    private void checkUserSession() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in, get their role and navigate
            String role = sessionManager.getUserRole();
            navigateToAppropriateDashboard(role);
        } else {
            // User is not logged in, navigate to LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        // Finish SplashActivity so the user cannot navigate back to it
        finish();
    }

// In SplashActivity.java

    private void navigateToAppropriateDashboard(String role) {
        Intent intent;
        if (role == null) {
            Toast.makeText(this, "Error: User role not found. Logging out.", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser();
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else if (role.equals("Admin")) {
            // Navigate to AdminHomeActivity
            intent = new Intent(SplashActivity.this, AdminHomeActivity.class);
        } else if (role.equals("Student")) {
            // Navigate to StudentHomeActivity
            intent = new Intent(SplashActivity.this, StudentHomeActivity.class);
        } else {
            Toast.makeText(this, "Unknown role: " + role + ". Logging out.", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser();
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        // finish() for SplashActivity is called in checkUserSession() after this method returns
        // or after the Handler delay, so no need to call finish() here.
    }
}