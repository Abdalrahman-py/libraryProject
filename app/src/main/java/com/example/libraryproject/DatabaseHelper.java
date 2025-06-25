package com.example.libraryproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LibraryDB.db";
    // Increment the database version because of schema change
    private static final int DATABASE_VERSION = 2; // MODIFIED

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email"; // ADDED
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role"; // "Admin" or "Student"

    // ... (other table and column constants remain the same)
    public static final String TABLE_BOOKS = "books";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TITLE = "title";
    public static final String COL_BOOK_CATEGORY = "category";
    public static final String COL_BOOK_AUTHOR_ID = "author_id";
    public static final String COL_BOOK_DESCRIPTION = "description";
    public static final String COL_BOOK_IS_BORROWED = "is_borrowed";

    public static final String TABLE_AUTHORS = "authors";
    public static final String COL_AUTHOR_ID = "id";
    public static final String COL_AUTHOR_NAME = "name";

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
            + COL_EMAIL + " TEXT," // ADDED
            + COL_PHONE + " TEXT,"
            + COL_ROLE + " TEXT NOT NULL DEFAULT 'Student'"
            + ")";

    // ... (CREATE_TABLE_AUTHORS, CREATE_TABLE_BOOKS, CREATE_TABLE_BORROW remain the same)
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
        // This basic implementation drops and recreates tables.
        // For a production app, you'd implement data migration (e.g., using ALTER TABLE).
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
     * @param email The user's email address (can be null). // MODIFIED Javadoc
     * @param phone The user's phone number (can be null). // MODIFIED Javadoc
     * @param role The user's role ("Admin" or "Student").
     * @return true if user is added successfully, false otherwise.
     */
    public boolean addUser(String username, String password, String email, String phone, String role) { // MODIFIED signature
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_EMAIL, email); // ADDED
        cv.put(COL_PHONE, phone);
        cv.put(COL_ROLE, role);
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result != -1;
    }

    // ... (getUser, checkUsernameExists, and all other methods remain the same for now)
    // --- Book Operations, Author Operations etc. ---
    // (Make sure the rest of your methods from the original file are here)

    /**
     * Validates user credentials.
     * @param username The username to check.
     * @param password The password to check.
     * @return A Cursor containing user data if valid, null otherwise.
     */
    public Cursor getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Query now needs to be able to fetch the email as well if needed upon login
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
        // db.close(); // You might want to remove db.close() here if checkUsernameExists is often called before another db operation.
        // Or ensure it's managed carefully. For now, keeping it as is from your original.
        return exists;
    }

    // --- Book Operations --- (These remain the same as your provided file)
    public boolean addBook(String title, String category, int authorId, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_TITLE, title);
        cv.put(COL_BOOK_CATEGORY, category);
        cv.put(COL_BOOK_AUTHOR_ID, authorId);
        cv.put(COL_BOOK_DESCRIPTION, description);
        cv.put(COL_BOOK_IS_BORROWED, 0);
        long result = db.insert(TABLE_BOOKS, null, cv);
        db.close();
        return result != -1;
    }

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

    public boolean deleteBook(int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BOOKS, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }

    public Cursor getAllBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID;
        return db.rawQuery(query, null);
    }

    public Cursor searchBooksByTitle(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getBooksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_CATEGORY + " = ?";
        String[] selectionArgs = new String[]{category};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getBooksByAuthor(int authorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_AUTHOR_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(authorId)};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID +
                " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs);
    }

    public boolean updateBookBorrowedStatus(int bookId, int isBorrowed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_IS_BORROWED, isBorrowed);
        int result = db.update(TABLE_BOOKS, cv, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }

    // --- Author Operations --- (These remain the same as your provided file)
    public long addAuthor(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AUTHOR_NAME, name);
        long result = db.insert(TABLE_AUTHORS, null, cv);
        db.close();
        return result;
    }

    public Cursor getAllAuthors() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_AUTHORS;
        return db.rawQuery(query, null);
    }
}