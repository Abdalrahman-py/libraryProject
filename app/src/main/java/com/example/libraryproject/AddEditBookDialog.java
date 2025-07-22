package com.example.libraryproject;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.libraryproject.databinding.DialogAddEditBookBinding;

import java.util.List;

public class AddEditBookDialog extends DialogFragment {
    private DialogAddEditBookBinding binding;
    private DatabaseHelper dbHelper;
    private Book bookToEdit;
    private OnBookSavedListener listener;
    private List<Author> authorList;
    private Author selectedAuthor;

    public interface OnBookSavedListener {
        void onBookSaved();
    }

    // Recommended static method to instantiate the dialog
    public static AddEditBookDialog newInstance(Book book, OnBookSavedListener listener) {
        AddEditBookDialog dialog = new AddEditBookDialog();
        dialog.bookToEdit = book;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogAddEditBookBinding.inflate(LayoutInflater.from(requireContext()));
        dbHelper = new DatabaseHelper(requireContext());

        setupAuthorAutoComplete();

        if (bookToEdit != null) {
            populateFieldsForEditing();
        }

        return new AlertDialog.Builder(requireContext())
                .setTitle(bookToEdit == null ? "Add Book" : "Edit Book")
                .setView(binding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> saveBook())
                .setNegativeButton("Cancel", null)
                .create();
    }

    private void setupAuthorAutoComplete() {
        authorList = dbHelper.getAllAuthorsList();

        ArrayAdapter<Author> authorAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                authorList
        );

        binding.autoCompleteTextViewBookAuthor.setAdapter(authorAdapter);
        binding.autoCompleteTextViewBookAuthor.setOnItemClickListener((parent, view, position, id) -> {
            selectedAuthor = authorList.get(position);
        });
    }

    private void populateFieldsForEditing() {
        binding.editTextBookTitle.setText(bookToEdit.getTitle());
        binding.editTextBookCategory.setText(bookToEdit.getCategory());
        binding.editTextBookDescription.setText(bookToEdit.getDescription());
        binding.autoCompleteTextViewBookAuthor.setText(bookToEdit.getAuthorName());

        // Optionally, pre-select the author from the list
        for (Author author : authorList) {
            if (author.getName().equals(bookToEdit.getAuthorName())) {
                selectedAuthor = author;
                break;
            }
        }
    }

    private void saveBook() {
        String title = binding.editTextBookTitle.getText().toString().trim();
        String category = binding.editTextBookCategory.getText().toString().trim();
        String description = binding.editTextBookDescription.getText().toString().trim();

        if (title.isEmpty() || selectedAuthor == null) {
            Toast.makeText(getContext(), "Title and author are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookToEdit == null) {
            // New book
            Book newBook = new Book(title, category, selectedAuthor.getId(), selectedAuthor.getName(), description);
            dbHelper.addBook(newBook);
        } else {
            // Update existing
            bookToEdit.setTitle(title);
            bookToEdit.setCategory(category);
            bookToEdit.setDescription(description);
            bookToEdit.setAuthorId(selectedAuthor.getId());
            bookToEdit.setAuthorName(selectedAuthor.getName());
            dbHelper.updateBook(bookToEdit);
        }

        if (listener != null) listener.onBookSaved();
        dismiss(); // Close the dialog
    }
}
