package com.example.libraryproject; // Your package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.RadioButton; // Still needed for type casting from findViewById
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryproject.databinding.ActivityRegisterBinding; // Import generated binding class

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding; // Declare the binding variable
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater()); // Inflate
        setContentView(binding.getRoot()); // Set content view

        dbHelper = new DatabaseHelper(this);

        binding.buttonRegister.setOnClickListener(v -> registerUser());

        binding.textViewGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String username = binding.editTextRegisterUsername.getText().toString().trim();
        String email = binding.editTextRegisterEmail.getText().toString().trim();
        String password = binding.editTextRegisterPassword.getText().toString().trim();
        String phone = binding.editTextRegisterPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.editTextRegisterUsername.setError("Username is required");
            binding.editTextRegisterUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.editTextRegisterEmail.setError("Email is required");
            binding.editTextRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextRegisterEmail.setError("Enter a valid email");
            binding.editTextRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextRegisterPassword.setError("Password is required");
            binding.editTextRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.editTextRegisterPassword.setError("Password must be at least 6 characters");
            binding.editTextRegisterPassword.requestFocus();
            return;
        }

        int selectedRadioButtonId = binding.radioGroupRole.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        // RadioButton is not directly part of binding in a way that gives text,
        // so findViewById on the ID from the radioGroup is still common here.
        // Or you can check IDs:
        String role;
        if (selectedRadioButtonId == binding.radioButtonStudent.getId()) {
            role = binding.radioButtonStudent.getText().toString();
        } else if (selectedRadioButtonId == binding.radioButtonAdmin.getId()) {
            role = binding.radioButtonAdmin.getText().toString();
        } else {
            Toast.makeText(this, "Role selection error", Toast.LENGTH_SHORT).show();
            return;
        }
        // Alternative for role (if you prefer direct findViewById for the selected RadioButton):
        // RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        // String role = selectedRadioButton.getText().toString();


        if (dbHelper.checkUsernameExists(username)) {
            binding.editTextRegisterUsername.setError("Username already taken");
            binding.editTextRegisterUsername.requestFocus();
            return;
        }

        boolean isAdded = dbHelper.addUser(username, password, email, phone, role);

        if (isAdded) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Important for Activities
    }
}