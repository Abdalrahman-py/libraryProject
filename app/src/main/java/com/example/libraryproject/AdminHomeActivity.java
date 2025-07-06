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
import com.example.libraryproject.databinding.DialogAddEditBookBinding; // Import the binding for the dialog

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private ActivityAdminHomeBinding binding; // Binding for AdminHomeActivity layout
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Author> authorListForDialog = new ArrayList<>();
    private Author selectedAuthorForDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(getApplicationContext());
        dbHelper = new DatabaseHelper(this);

        // Security check: Ensure user is logged in and is an Admin
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return; // Stop further execution
        }
        if (!"Admin".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, "Access Denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            // Optional: Log out user if they somehow reached here with wrong role
            // sessionManager.logoutUser();
            redirectToLogin(); // Or redirect to their appropriate dashboard if you have one
            return; // Stop further execution
        }

        loadUserDetails();
        setupRecyclerView();
        loadBooks();

        binding.buttonAdminLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(AdminHomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        binding.fabAddBook.setOnClickListener(v -> showAddEditBookDialog(null)); // Pass null for adding a new book
    }

    private void loadUserDetails() {
        binding.textViewAdminWelcome.setText("Welcome, " + sessionManager.getUsername() + "!");
        binding.textViewAdminRoleValue.setText(sessionManager.getUserRole());
        binding.textViewAdminEmailValue.setText(sessionManager.getUserEmail());
    }

    private void setupRecyclerView() {
        binding.recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        // Initialize bookList if it's null, though it's already initialized as new ArrayList<>()
        if (bookList == null) {
            bookList = new ArrayList<>();
        }
        bookAdapter = new BookAdapter(this, bookList, this); // 'this' implements OnBookClickListener
        binding.recyclerViewBooks.setAdapter(bookAdapter);
    }

    private void loadBooks() {
        List<Book> booksFromDb = dbHelper.getAllBooks();
        bookList.clear();
        bookList.addAll(booksFromDb);

        if (bookAdapter != null) { // Ensure adapter is initialized
            bookAdapter.notifyDataSetChanged(); // Or use bookAdapter.setBooks(booksFromDb); if you implemented that
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

        authorListForDialog = dbHelper.getAllAuthorsList(); // Load authors
        selectedAuthorForDialog = null; // Reset selected author

        ArrayAdapter<Author> authorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, authorListForDialog);
        dialogBinding.autoCompleteTextViewBookAuthor.setAdapter(authorAdapter);


        if (bookToEdit != null) {
            dialogBinding.editTextBookTitle.setText(bookToEdit.getTitle());
            // Find and set the author in AutoCompleteTextView
            if (bookToEdit.getAuthor() != null) {
                // First, try to find the author by name in our current list
                Author currentBookAuthor = null;
                for (Author author : authorListForDialog) {
                    if (author.getName().equals(bookToEdit.getAuthor())) {
                        currentBookAuthor = author;
                        break;
                    }
                }

                if (currentBookAuthor != null) {
                    dialogBinding.autoCompleteTextViewBookAuthor.setText(currentBookAuthor.getName(), false); // Set text, don't filter
                    selectedAuthorForDialog = currentBookAuthor;
                } else {
                    // If author not in list (e.g., data inconsistency or author was deleted),
                    // just show the name. User might need to re-select or add.
                    dialogBinding.autoCompleteTextViewBookAuthor.setText(bookToEdit.getAuthor(), false);
                    // selectedAuthorForDialog remains null, user MUST select a valid author
                    Toast.makeText(this, "Book's author '" + bookToEdit.getAuthor() + "' not found in current author list. Please select or add.", Toast.LENGTH_LONG).show();
                }
            }
            dialogBinding.editTextBookCategory.setText(bookToEdit.getCategory());
            dialogBinding.editTextBookDescription.setText(bookToEdit.getDescription());
        }

        // Handle author selection from dropdown
        dialogBinding.autoCompleteTextViewBookAuthor.setOnItemClickListener((parent, view, position, id) -> {
            selectedAuthorForDialog = (Author) parent.getItemAtPosition(position);
            Log.d("Dialog", "Selected author: " + (selectedAuthorForDialog != null ? selectedAuthorForDialog.getName() : "null"));
        });

        // Handle "Add New Author" button click
        dialogBinding.buttonAddNewAuthorDialog.setOnClickListener(v -> {
            showAddNewAuthorDialog(dialogBinding.autoCompleteTextViewBookAuthor, authorAdapter);
        });
        // --- END: Author Handling ---


        builder.setPositiveButton(bookToEdit == null ? "Add" : "Save", (dialog, which) -> {
            String title = dialogBinding.editTextBookTitle.getText().toString().trim();
            // String authorNameFromInput = dialogBinding.autoCompleteTextViewBookAuthor.getText().toString().trim(); // We use selectedAuthorForDialog
            String category = dialogBinding.editTextBookCategory.getText().toString().trim();
            String description = dialogBinding.editTextBookDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) { // Basic validation, add more as needed
                Toast.makeText(AdminHomeActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                // To keep the dialog open, you'd need a more complex setup or not dismiss.
                // For simplicity here, we'll let it dismiss and re-prompt if validation fails.
                // Consider implementing a way to prevent dialog dismissal on validation failure.
                return;
            }

            // --- START: Crucial Author Validation for Save ---
            if (selectedAuthorForDialog == null) {
                // This can happen if the user typed text that doesn't match an item,
                // or if an existing book's author was not found and they didn't re-select.
                String currentAuthorText = dialogBinding.autoCompleteTextViewBookAuthor.getText().toString().trim();
                if (!TextUtils.isEmpty(currentAuthorText)) {
                    // Attempt to find the author based on the current text
                    // This is a fallback, ideally selection is done via OnItemClickListener
                    boolean found = false;
                    for(Author author : authorListForDialog) {
                        if (author.getName().equalsIgnoreCase(currentAuthorText)) {
                            selectedAuthorForDialog = author;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Toast.makeText(AdminHomeActivity.this, "Author '" + currentAuthorText + "' is not recognized. Please select a valid author from the list or add a new one.", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(AdminHomeActivity.this, "Please select an author.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // --- END: Crucial Author Validation for Save ---
            boolean success;
            String successMessage;
            String failureMessage;

            if (bookToEdit == null) { // Adding new book
                Book newBook = new Book(title, category, selectedAuthorForDialog.getName(), description);
                success = dbHelper.addBook(newBook);
                successMessage = "Book added successfully";
                failureMessage = "Failed to add book";
            } else { // Editing existing book
                bookToEdit.setTitle(title);
                bookToEdit.setAuthor(selectedAuthorForDialog.getName());
                bookToEdit.setCategory(category);
                bookToEdit.setDescription(description);
                success = dbHelper.updateBook(bookToEdit);
                successMessage = "Book updated successfully";
                failureMessage = "Failed to update book";
            }

            if (success) {
                Toast.makeText(AdminHomeActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                loadBooks(); // Refresh the list
            } else {
                Toast.makeText(AdminHomeActivity.this, failureMessage, Toast.LENGTH_SHORT).show();
            }
            // Reset for next time
            selectedAuthorForDialog = null;
            authorListForDialog.clear();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            selectedAuthorForDialog = null; // Clean up
            authorListForDialog.clear();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddNewAuthorDialog(AutoCompleteTextView bookAuthorAutoCompleteTextView, ArrayAdapter<Author> authorArrayAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Author");

        // Simple layout for adding an author: an EditText
        // You can create a separate XML layout for this if you want more complex UI
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_author, null); // Create dialog_add_author.xml
        builder.setView(customLayout);

        final EditText editTextNewAuthorName = customLayout.findViewById(R.id.editTextNewAuthorNameDialog); // Ensure this ID exists in dialog_add_author.xml

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newAuthorName = editTextNewAuthorName.getText().toString().trim();
            if (TextUtils.isEmpty(newAuthorName)) {
                Toast.makeText(this, "Author name cannot be empty", Toast.LENGTH_SHORT).show();
                // TODO: Keep this dialog open for correction.
                // For now, it will dismiss. You might need to handle the button click yourself
                // to prevent dismissal on validation failure.
                return;
            }

            if (dbHelper.authorExists(newAuthorName)) {
                Toast.makeText(this, "Author '" + newAuthorName + "' already exists.", Toast.LENGTH_SHORT).show();
                // Optionally, try to find and set this existing author in the main dialog
                for(Author author : authorListForDialog){
                    if(author.getName().equalsIgnoreCase(newAuthorName)){
                        selectedAuthorForDialog = author;
                        bookAuthorAutoCompleteTextView.setText(author.getName(), false); // Update AutoCompleteTextView
                        break;
                    }
                }
                return;
            }

            long newAuthorId = dbHelper.addAuthor(newAuthorName); // Use the DB helper method
            if (newAuthorId != -1) {
                Toast.makeText(this, "Author '" + newAuthorName + "' added successfully", Toast.LENGTH_SHORT).show();
                // Refresh the author list in the main book dialog
                authorListForDialog.clear();
                authorListForDialog.addAll(dbHelper.getAllAuthorsList());
                authorArrayAdapter.notifyDataSetChanged(); // Notify the adapter in the main dialog

                // Find the newly added author and select it
                for (Author author : authorListForDialog) {
                    if (author.getId() == (int) newAuthorId) {
                        selectedAuthorForDialog = author;
                        bookAuthorAutoCompleteTextView.setText(author.getName(), false); // Update AutoCompleteTextView
                        // bookAuthorAutoCompleteTextView.setSelection(authorListForDialog.indexOf(author)); // May not work directly with AutoComplete
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "Failed to add new author", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onBookClick(Book book) {
        // Called when a book item is clicked (for editing)
        showAddEditBookDialog(book);
    }

    @Override
    public void onBookLongClick(Book book) {
        // Called when a book item is long-clicked (for deleting)
        new AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteBook(book.getId())) {
                        Toast.makeText(AdminHomeActivity.this, "\"" + book.getTitle() + "\" deleted successfully", Toast.LENGTH_SHORT).show();
                        loadBooks(); // Refresh the list
                    } else {
                        Toast.makeText(AdminHomeActivity.this, "Failed to delete book", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish AdminHomeActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close(); // Close the database when the activity is destroyed
        }
        binding = null; // Clean up view binding
    }
}