package com.example.libraryproject; // Ensure this is your package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryproject.databinding.ActivityAdminHomeBinding; // Import generated binding class

public class AdminHomeActivity extends AppCompatActivity {

    private ActivityAdminHomeBinding binding; // Declare the binding variable
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(getApplicationContext());

        // Check if user is logged in and is an Admin
        if (!sessionManager.isLoggedIn()) {
            // Not logged in, redirect to LoginActivity
            redirectToLogin();
            return; // Stop further execution of onCreate
        }

        String userRole = sessionManager.getUserRole();
        if (userRole == null || !userRole.equals("Admin")) {
            // Logged in but not an Admin (shouldn't happen if navigation is correct)
            Toast.makeText(this, "Access Denied. Not an Admin.", Toast.LENGTH_LONG).show();
            sessionManager.logoutUser(); // Log out the user
            redirectToLogin();
            return; // Stop further execution
        }

        // User is logged in and is an Admin, load user details
        loadUserDetails();

        binding.buttonAdminLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(AdminHomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // TODO: Add listeners and logic for other admin functions later
        // e.g., binding.buttonManageUsers.setOnClickListener(...)
        //TODO: manage books in separate activity/screen
            // TODO: feature to delete books
            // TODO: feature to update books
            // TODO: feature to add books form includes book title, category, author and description


    }

    private void loadUserDetails() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole(); // Should be "Admin" here

        binding.textViewAdminWelcome.setText("Welcome, " + (username != null ? username : "Admin") + "!");
        binding.textViewAdminRole.setText("Role: " + (role != null ? role : "N/A"));
        binding.textViewAdminEmail.setText("Email: " + (email != null ? email : "N/A"));
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
        // Clear activity stack so the user cannot go back to AdminHomeActivity after logging out
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish AdminHomeActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Important for View Binding in Activities
    }
}