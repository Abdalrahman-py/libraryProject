package com.example.libraryproject;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LibraryDB.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role"; // "Admin" or "Student"

    // Books Table
    public static final String TABLE_BOOKS = "books";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TITLE = "title";
    public static final String COL_BOOK_CATEGORY = "category";
    public static final String COL_BOOK_AUTHOR_ID = "author_id";
    public static final String COL_BOOK_DESCRIPTION = "description";
    public static final String COL_BOOK_IS_BORROWED = "is_borrowed"; // 0 = available, 1 = borrowed

    // Authors Table
    public static final String TABLE_AUTHORS = "authors";
    public static final String COL_AUTHOR_ID = "id";
    public static final String COL_AUTHOR_NAME = "name";

    // Borrow Table
    public static final String TABLE_BORROW = "borrow_table";
    public static final String COL_BORROW_ID = "id";
    public static final String COL_BORROW_BOOK_ID = "book_id";
    public static final String COL_BORROW_STUDENT_ID = "student_id";
    public static final String COL_BORROW_DATE = "borrow_date";
    public static final String COL_RETURN_DATE = "return_date";

    // SQL to create tables
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_USERNAME + " TEXT UNIQUE NOT NULL,"
            + COL_PASSWORD + " TEXT NOT NULL,"
            + COL_PHONE + " TEXT,"
            + COL_ROLE + " TEXT NOT NULL DEFAULT 'Student'" // Default role is Student
            + ")";

    private static final String CREATE_TABLE_AUTHORS = "CREATE TABLE " + TABLE_AUTHORS + "("
            + COL_AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_AUTHOR_NAME + " TEXT UNIQUE NOT NULL"
            + ")";

    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + "("
            + COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_BOOK_TITLE + " TEXT NOT NULL,"
            + COL_BOOK_CATEGORY + " TEXT NOT NULL,"
            + COL_BOOK_AUTHOR_ID + " INTEGER NOT NULL,"
            + COL_BOOK_DESCRIPTION + " TEXT,"
            + COL_BOOK_IS_BORROWED + " INTEGER NOT NULL DEFAULT 0,"
            + "FOREIGN KEY(" + COL_BOOK_AUTHOR_ID + ") REFERENCES " + TABLE_AUTHORS + "(" + COL_AUTHOR_ID + ")"
            + ")";

    private static final String CREATE_TABLE_BORROW = "CREATE TABLE " + TABLE_BORROW + "("
            + COL_BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_BORROW_BOOK_ID + " INTEGER NOT NULL,"
            + COL_BORROW_STUDENT_ID + " INTEGER NOT NULL,"
            + COL_BORROW_DATE + " TEXT NOT NULL,"
            + COL_RETURN_DATE + " TEXT,"
            + "FOREIGN KEY(" + COL_BORROW_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + "),"
            + "FOREIGN KEY(" + COL_BORROW_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")"
            + ")";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_AUTHORS);
        db.execSQL(CREATE_TABLE_BOOKS);
        db.execSQL(CREATE_TABLE_BORROW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROW);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUTHORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // --- User Operations ---

    /**
     * Adds a new user to the database.
     * @param username The user's chosen username.
     * @param password The user's password.
     * @param phone The user's phone number (optional).
     * @param role The user's role ("Admin" or "Student").
     * @return true if user is added successfully, false otherwise.
     */
    public boolean addUser(String username, String password, String phone, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_PHONE, phone);
        cv.put(COL_ROLE, role);
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;
    }

    /**
     * Validates user credentials.
     * @param username The username to check.
     * @param password The password to check.
     * @return A Cursor containing user data if valid, null otherwise.
     */
    public Cursor getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        return cursor; // Caller must close the cursor
    }

    /**
     * Checks if a username already exists.
     * @param username The username to check.
     * @return true if username exists, false otherwise.
     */
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // --- Book Operations ---

    /**
     * Adds a new book to the database.
     * @param title Book title.
     * @param category Book category.
     * @param authorId ID of the author.
     * @param description Book description.
     * @return true if book is added successfully, false otherwise.
     */
    public boolean addBook(String title, String category, int authorId, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_TITLE, title);
        cv.put(COL_BOOK_CATEGORY, category);
        cv.put(COL_BOOK_AUTHOR_ID, authorId);
        cv.put(COL_BOOK_DESCRIPTION, description);
        cv.put(COL_BOOK_IS_BORROWED, 0); // Initially not borrowed
        long result = db.insert(TABLE_BOOKS, null, cv);
        db.close();
        return result != -1;
    }

    /**
     * Updates an existing book.
     * @param bookId ID of the book to update.
     * @param title New title.
     * @param category New category.
     * @param authorId New author ID.
     * @param description New description.
     * @return true if updated successfully, false otherwise.
     */
    public boolean updateBook(int bookId, String title, String category, int authorId, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_TITLE, title);
        cv.put(COL_BOOK_CATEGORY, category);
        cv.put(COL_BOOK_AUTHOR_ID, authorId);
        cv.put(COL_BOOK_DESCRIPTION, description);
        int result = db.update(TABLE_BOOKS, cv, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }

    /**
     * Deletes a book from the database.
     * @param bookId ID of the book to delete.
     * @return true if deleted successfully, false otherwise.
     */
    public boolean deleteBook(int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BOOKS, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }

    /**
     * Gets all books from the database.
     * @return Cursor containing all book data.
     */
    public Cursor getAllBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID;
        return db.rawQuery(query, null); // Caller must close the cursor
    }

    /**
     * Searches books by title.
     * @param query The search query.
     * @return Cursor containing matching book data.
     */
    public Cursor searchBooksByTitle(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs); // Caller must close the cursor
    }

    /**
     * Gets books by category.
     * @param category The category to filter by.
     * @return Cursor containing matching book data.
     */
    public Cursor getBooksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_CATEGORY + " = ?";
        String[] selectionArgs = new String[]{category};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs); // Caller must close the cursor
    }

    /**
     * Gets books by author.
     * @param authorId The ID of the author.
     * @return Cursor containing matching book data.
     */
    public Cursor getBooksByAuthor(int authorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_AUTHOR_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(authorId)};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs); // Caller must close the cursor
    }

    /**
     * Marks a book as borrowed or available.
     * @param bookId ID of the book.
     * @param isBorrowed 1 for borrowed, 0 for available.
     * @return true if updated, false otherwise.
     */
    public boolean updateBookBorrowedStatus(int bookId, int isBorrowed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_IS_BORROWED, isBorrowed);
        int result = db.update(TABLE_BOOKS, cv, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }


    // --- Author Operations ---

    /**
     * Adds a new author to the database.
     * @param name Author's name.
     * @return The ID of the new author, or -1 if failed.
     */
    public long addAuthor(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AUTHOR_NAME, name);
        long result = db.insert(TABLE_AUTHORS, null, cv);
        db.close();
        return result; // Returns row ID or -1 on error
    }

    /**
     * Gets all authors.
     * @return Cursor containing all author data.
     */
    public Cursor getAllAuthors() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_AUTHORS;
        return db.rawQuery(query, null); // Caller must close the cursor
    }

    /**
     * Gets an author by name.
     * @param name The name of the author.
     * @return Cursor containing author data, or null if not found.
     */
    public Cursor getAuthorByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_AUTHORS + " WHERE " + COL_AUTHOR_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        return cursor; // Caller must close the cursor
    }


    // --- Borrow Operations ---

    /**
     * Records a book borrowing.
     * @param bookId ID of the borrowed book.
     * @param studentId ID of the student borrowing.
     * @param borrowDate Date of borrowing.
     * @return true if borrowing is recorded, false otherwise.
     */
    public boolean recordBorrow(int bookId, int studentId, String borrowDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BORROW_BOOK_ID, bookId);
        cv.put(COL_BORROW_STUDENT_ID, studentId);
        cv.put(COL_BORROW_DATE, borrowDate);
        long result = db.insert(TABLE_BORROW, null, cv);
        db.close();
        return result != -1;
    }

    /**
     * Gets all borrowed books with student and book details.
     * @return Cursor containing borrowed book details.
     */
    public Cursor getAllBorrowedBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b." + COL_BOOK_TITLE + ", u." + COL_USERNAME + ", br." + COL_BORROW_DATE +
                " FROM " + TABLE_BORROW + " br " +
                "INNER JOIN " + TABLE_BOOKS + " b ON br." + COL_BORROW_BOOK_ID + " = b." + COL_BOOK_ID +
                "INNER JOIN " + TABLE_USERS + " u ON br." + COL_BORROW_STUDENT_ID + " = u." + COL_USER_ID;
        return db.rawQuery(query, null); // Caller must close the cursor
    }

    /**
     * Gets borrowing history for a specific student.
     * @param studentId ID of the student.
     * @return Cursor containing borrowing history.
     */
    public Cursor getStudentBorrowingHistory(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b." + COL_BOOK_TITLE + ", br." + COL_BORROW_DATE + ", br." + COL_RETURN_DATE +
                " FROM " + TABLE_BORROW + " br " +
                "INNER JOIN " + TABLE_BOOKS + " b ON br." + COL_BORROW_BOOK_ID + " = b." + COL_BOOK_ID +
                " WHERE br." + COL_BORROW_STUDENT_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(studentId)}); // Caller must close the cursor
    }

    /**
     * Marks a borrowed book as returned.
     * @param borrowId ID of the borrow record.
     * @param returnDate Date of return.
     * @return true if updated, false otherwise.
     */
    public boolean updateReturnDate(int borrowId, String returnDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RETURN_DATE, returnDate);
        int result = db.update(TABLE_BORROW, cv, COL_BORROW_ID + " = ?", new String[]{String.valueOf(borrowId)});
        db.close();
        return result > 0;
    }
}
