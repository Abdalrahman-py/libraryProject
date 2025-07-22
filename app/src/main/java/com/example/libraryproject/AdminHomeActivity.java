package com.example.libraryproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.widget.ViewPager2;

import com.example.libraryproject.databinding.ActivityAdminHomeBinding;
import com.example.libraryproject.databinding.FragmentBooksBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    private ActivityAdminHomeBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private List<Author> authorListForDialog = new ArrayList<>();
    private Author selectedAuthorForDialog;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        TabLayout tabLayout = binding.tabLayoutAdmin;
        ViewPager2 viewPager = binding.viewPagerAdmin;

        AdminPagerAdapter adapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Books");
                            break;
                        case 1:
                            tab.setText("Borrowed");
                            break;
                        case 2:
                            tab.setText("Authors");
                            break;
                    }
                }).attach();


        sessionManager = new SessionManager(getApplicationContext());
        dbHelper = new DatabaseHelper(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        if (!"Admin".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, "Access Denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        loadUserDetails();
        BooksFragment booksFragment = (BooksFragment) adapter.getFragmentAt(0);
        if (booksFragment != null) {
            booksFragment.loadBooks();
        } else {
            Log.w("AdminHomeActivity", "BooksFragment is not yet initialized.");
        }

        binding.buttonAdminLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void loadUserDetails() {
        binding.textViewAdminWelcome.setText("Welcome, " + sessionManager.getUsername() + "!");
        binding.textViewAdminRoleValue.setText(sessionManager.getUserRole());
        binding.textViewAdminEmailValue.setText(sessionManager.getUserEmail());
    }


    private void showAddNewAuthorDialog(AutoCompleteTextView bookAuthorAutoCompleteTextView, ArrayAdapter<Author> authorArrayAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Author");

        View layout = getLayoutInflater().inflate(R.layout.dialog_add_author, null);
        builder.setView(layout);

        EditText editTextNewAuthor = layout.findViewById(R.id.editTextNewAuthorNameDialog);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String authorName = editTextNewAuthor.getText().toString().trim();

            if (TextUtils.isEmpty(authorName)) {
                Toast.makeText(this, "Author name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.authorExists(authorName)) {
                Toast.makeText(this, "Author '" + authorName + "' already exists.", Toast.LENGTH_SHORT).show();
                for (Author author : authorListForDialog) {
                    if (author.getName().equalsIgnoreCase(authorName)) {
                        selectedAuthorForDialog = author;
                        bookAuthorAutoCompleteTextView.setText(author.getName(), false);
                        break;
                    }
                }
                return;
            }

            long id = dbHelper.addAuthor(authorName);
            if (id != -1) {
                Toast.makeText(this, "Author added successfully", Toast.LENGTH_SHORT).show();
                authorListForDialog.clear();
                authorListForDialog.addAll(dbHelper.getAllAuthorsList());
                authorArrayAdapter.notifyDataSetChanged();

                for (Author author : authorListForDialog) {
                    if (author.getId() == (int) id) {
                        selectedAuthorForDialog = author;
                        bookAuthorAutoCompleteTextView.setText(author.getName(), false);
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "Failed to add author", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showToast(boolean success, String successMsg, String errorMsg) {
        Toast.makeText(this, success ? successMsg : errorMsg, Toast.LENGTH_SHORT).show();
    }


    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
        super.onDestroy();
    }

}
