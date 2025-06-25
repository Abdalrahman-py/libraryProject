package com.example.libraryproject; // Your package name

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
// No need to import individual view types like Button, EditText, etc.
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryproject.databinding.ActivityLoginBinding; // Import generated binding class

public class LoginActivity extends AppCompatActivity {

    // Declare the binding variable
    private ActivityLoginBinding binding;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root of the binding as the content view

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.isLoggedIn()) {
            navigateToAppropriateDashboard(sessionManager.getUserRole());
            finish();
            return;
        }

        // Access views using the binding object
        binding.buttonLogin.setOnClickListener(v -> loginUser());

        binding.textViewGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String usernameOrEmail = binding.editTextLoginUsername.getText().toString().trim();
        String password = binding.editTextLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            binding.editTextLoginUsername.setError("Please enter username or email");
            binding.editTextLoginUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextLoginPassword.setError("Please enter password");
            binding.editTextLoginPassword.requestFocus();
            return;
        }

        // Assuming getUser can handle username or email (modify DB query if needed)
        Cursor cursor = dbHelper.getUser(usernameOrEmail, password);

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            String fetchedUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
            cursor.close();

            sessionManager.createLoginSession(userId, fetchedUsername, role, email);
            Toast.makeText(getApplicationContext(), "Login Successful. Role: " + role, Toast.LENGTH_LONG).show();

            navigateToAppropriateDashboard(role);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Invalid username/email or password", Toast.LENGTH_LONG).show();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // In LoginActivity.java

    private void navigateToAppropriateDashboard(String role) {
        Intent intent;
        if (role == null) {
            Toast.makeText(this, "Error: User role not found.", Toast.LENGTH_SHORT).show();
            // Fallback to LoginActivity or handle error appropriately.
            // Since we are already in LoginActivity, this case might indicate a deeper issue
            // or just that the method was called before a role could be determined.
            // For simplicity, we'll assume this means stay on login or re-attempt.
            return; // Or redirect to login again if necessary, though it seems redundant.
        }

        if (role.equals("Admin")) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else if (role.equals("Student")) {
            intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
        } else {
            Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
            // If role is unknown, maybe stay on login screen or redirect to it
            return; // Stay on LoginActivity
        }

        // Add flags to clear the task stack and start the new activity as a new task
        // This is important after login to prevent going back to LoginActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish LoginActivity after navigating to the dashboard
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Important to prevent memory leaks in Activities
    }
}