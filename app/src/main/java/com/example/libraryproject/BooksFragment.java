package com.example.libraryproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.libraryproject.databinding.FragmentBooksBinding;

import java.util.List;
import java.util.Objects;

public class BooksFragment extends Fragment {

    private FragmentBooksBinding binding;
    private BookAdapter bookAdapter;
    private DatabaseHelper databaseHelper;

    public BooksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBooksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(requireContext());

        // Setup RecyclerView
        bookAdapter = new BookAdapter(requireContext(), databaseHelper.getAllBooks(), new BookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                showEditDialog(book);
            }

            @Override
            public void onBookLongClick(Book book) {
                deleteBook(book);
            }
        });

        binding.recyclerViewBooks.setAdapter(bookAdapter);

        // FAB click: Add new book
        binding.fabAddBook.setOnClickListener(v -> showAddDialog());

        loadBooks();
    }

    public void loadBooks() {
        List<Book> books = databaseHelper.getAllBooks();

        if (books.isEmpty()) {
            binding.textViewNoBooks.setVisibility(View.VISIBLE);
            binding.recyclerViewBooks.setVisibility(View.GONE);
        } else {
            binding.textViewNoBooks.setVisibility(View.GONE);
            binding.recyclerViewBooks.setVisibility(View.VISIBLE);
            bookAdapter.setBooks(books);
        }
    }

    private void showAddDialog() {
        AddEditBookDialog dialog = AddEditBookDialog.newInstance(null, () -> {
            loadBooks();
            Toast.makeText(requireContext(), "Book added", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "AddBookDialog");
    }

    private void showEditDialog(Book book) {
        AddEditBookDialog dialog = AddEditBookDialog.newInstance(book, () -> {
            loadBooks();
            Toast.makeText(requireContext(), "Book updated", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getChildFragmentManager(), "EditBookDialog");
    }

    private void deleteBook(Book book) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Book")
                .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (databaseHelper.deleteBook(book.getId())) {
                        Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                        loadBooks();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete book", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
