// BookAdapter.java
package com.example.libraryproject; // Your package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;
    private OnBookClickListener listener;

    // Interface for click events
    public interface OnBookClickListener {
        void onBookClick(Book book); // For edit
        void onBookLongClick(Book book); // For delete or other options
    }

    public BookAdapter(Context context, List<Book> bookList, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.textViewTitle.setText(book.getTitle());
        holder.textViewAuthor.setText(book.getAuthor());
        holder.textViewCategory.setText("Category: " + book.getCategory());
        holder.textViewDescription.setText(book.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onBookLongClick(book);
                return true; // Consume the long click
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // Method to update the list of books in the adapter
    public void setBooks(List<Book> newBookList) {
        this.bookList.clear();
        this.bookList.addAll(newBookList);
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewAuthor, textViewCategory, textViewDescription;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewBookTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewBookAuthor);
            textViewCategory = itemView.findViewById(R.id.textViewBookCategory);
            textViewDescription = itemView.findViewById(R.id.textViewBookDescription);
        }
    }
}