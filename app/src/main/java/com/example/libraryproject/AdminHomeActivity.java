package com.example.libraryproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.libraryproject.databinding.ActivityAdminHomeBinding;
import com.example.libraryproject.databinding.DialogAddEditBookBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private ActivityAdminHomeBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private BookAdapter bookAdapter;

    private List<Book> bookList = new ArrayList<>();
    private List<Author> authorListForDialog = new ArrayList<>();
    private Author selectedAuthorForDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        setupRecyclerView();
        loadBooks();

        binding.buttonAdminLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        binding.fabAddBook.setOnClickListener(v -> showAddEditBookDialog(null));
    }

    private void loadUserDetails() {
        binding.textViewAdminWelcome.setText("Welcome, " + sessionManager.getUsername() + "!");
        binding.textViewAdminRoleValue.setText(sessionManager.getUserRole());
        binding.textViewAdminEmailValue.setText(sessionManager.getUserEmail());
    }

    private void setupRecyclerView() {
        binding.recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(this, bookList, this);
        binding.recyclerViewBooks.setAdapter(bookAdapter);
    }

    private void loadBooks() {
        bookList.clear();
        bookList.addAll(dbHelper.getAllBooks());

        if (bookAdapter != null) {
            bookAdapter.notifyDataSetChanged();
        }

        if (bookList.isEmpty()) {
            binding.recyclerViewBooks.setVisibility(View.GONE);
            binding.textViewNoBooks.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewBooks.setVisibility(View.VISIBLE);
            binding.textViewNoBooks.setVisibility(View.GONE);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void showAddEditBookDialog(final Book bookToEdit) {
        DialogAddEditBookBinding dialogBinding = DialogAddEditBookBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bookToEdit == null ? "Add New Book" : "Edit Book");
        builder.setView(dialogBinding.getRoot());

        authorListForDialog = dbHelper.getAllAuthorsList();
        selectedAuthorForDialog = null;

        ArrayAdapter<Author> authorAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, authorListForDialog);
        dialogBinding.autoCompleteTextViewBookAuthor.setAdapter(authorAdapter);

        if (bookToEdit != null) {
            dialogBinding.editTextBookTitle.setText(bookToEdit.getTitle());
            dialogBinding.editTextBookCategory.setText(bookToEdit.getCategory());
            dialogBinding.editTextBookDescription.setText(bookToEdit.getDescription());

            Author matchedAuthor = null;
            for (Author author : authorListForDialog) {
                if (author.getName().equals(bookToEdit.getAuthorName())) {
                    matchedAuthor = author;
                    break;
                }
            }

            if (matchedAuthor != null) {
                dialogBinding.autoCompleteTextViewBookAuthor.setText(matchedAuthor.getName(), false);
                selectedAuthorForDialog = matchedAuthor;
            } else {
                dialogBinding.autoCompleteTextViewBookAuthor.setText(bookToEdit.getAuthorName(), false);
                Toast.makeText(this,
                        "Book's author '" + bookToEdit.getAuthorName() + "' not found. Please select or add.",
                        Toast.LENGTH_LONG).show();
            }
        }

        dialogBinding.autoCompleteTextViewBookAuthor.setOnItemClickListener((parent, view, position, id) -> {
            selectedAuthorForDialog = (Author) parent.getItemAtPosition(position);
            Log.d("Dialog", "Selected author: " + selectedAuthorForDialog.getName());
        });

        dialogBinding.buttonAddNewAuthorDialog.setOnClickListener(v -> {
            showAddNewAuthorDialog(dialogBinding.autoCompleteTextViewBookAuthor, authorAdapter);
        });

        builder.setPositiveButton(bookToEdit == null ? "Add" : "Save", (dialog, which) -> {
            String title = dialogBinding.editTextBookTitle.getText().toString().trim();
            String category = dialogBinding.editTextBookCategory.getText().toString().trim();
            String description = dialogBinding.editTextBookDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAuthorForDialog == null) {
                String authorInput = dialogBinding.autoCompleteTextViewBookAuthor.getText().toString().trim();
                if (!TextUtils.isEmpty(authorInput)) {
                    for (Author author : authorListForDialog) {
                        if (author.getName().equalsIgnoreCase(authorInput)) {
                            selectedAuthorForDialog = author;
                            break;
                        }
                    }

                    if (selectedAuthorForDialog == null) {
                        Toast.makeText(this,
                                "Author '" + authorInput + "' is not recognized. Please select a valid author.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, "Please select an author.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean success;
            if (bookToEdit == null) {
                Book newBook = new Book();
                newBook.setTitle(title);
                newBook.setCategory(category);
                newBook.setAuthorId(selectedAuthorForDialog.getId()); // this is important
                newBook.setAuthorName(selectedAuthorForDialog.getName());
                newBook.setDescription(description);
                success = dbHelper.addBook(newBook);

                showToast(success, "Book added successfully", "Failed to add book");
            } else {
                bookToEdit.setTitle(title);
                bookToEdit.setAuthorId(selectedAuthorForDialog.getId());
                bookToEdit.setAuthorName(selectedAuthorForDialog.getName());
                bookToEdit.setCategory(category);
                bookToEdit.setDescription(description);
                success = dbHelper.updateBook(bookToEdit);
                showToast(success, "Book updated successfully", "Failed to update book");
            }

            if (success) loadBooks();

            selectedAuthorForDialog = null;
            authorListForDialog.clear();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            selectedAuthorForDialog = null;
            authorListForDialog.clear();
        });

        builder.create().show();
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

    @Override
    public void onBookClick(Book book) {
        showAddEditBookDialog(book);
    }

    @Override
    public void onBookLongClick(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteBook(book.getId())) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        loadBooks();
                    } else {
                        Toast.makeText(this, "Failed to delete book", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
