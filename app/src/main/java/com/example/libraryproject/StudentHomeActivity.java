package com.example.libraryproject; // Ensure this is your package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Make sure the binding class name matches your layout file name (activity_student_home.xml)
import com.example.libraryproject.databinding.ActivityStudentHomeBinding;

public class StudentHomeActivity extends AppCompatActivity {

    // Declare the binding variable, ensuring the type matches your layout file name
    private ActivityStudentHomeBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(getApplicationContext());

        // Check if user is logged in and is a Student
        if (!sessionManager.isLoggedIn()) {
            // Not logged in, redirect to LoginActivity
            redirectToLogin();
            return; // Stop further execution of onCreate
        }

        String userRole = sessionManager.getUserRole();
        if (userRole == null || !userRole.equals("Student")) {
            // Logged in but not a Student (shouldn't happen if navigation is correct)
            Toast.makeText(this, "Access Denied. Not a Student.", Toast.LENGTH_LONG).show();
            sessionManager.logoutUser(); // Log out the user
            redirectToLogin();
            return; // Stop further execution
        }

        // User is logged in and is a Student, load user details
        loadUserDetails();

        // Set OnClickListener for the logout button
        binding.buttonStudentLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(StudentHomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // TODO: Add listeners and logic for other student functions later
        // e.g., binding.buttonViewBooks.setOnClickListener(...)
    }

    private void loadUserDetails() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole(); // Should be "Student" here

        // Use the IDs from your activity_student_home.xml
        binding.textViewStudentWelcome.setText("Welcome, " + (username != null ? username : "Student") + "!");
        binding.textViewStudentRoleValue.setText(role != null ? role : "N/A"); // Value TextView
        binding.textViewStudentEmailValue.setText(email != null ? email : "N/A"); // Value TextView
    }

    private void redirectToLogin() {
        Intent intent = new Intent(StudentHomeActivity.this, LoginActivity.class);
        // Clear activity stack so the user cannot go back to StudentHomeActivity after logging out
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish StudentHomeActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Important for View Binding in Activities
    }
}