package com.example.libraryproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LibraryDB.db";
    // Increment the database version because of schema change
    private static final int DATABASE_VERSION = 2; // MODIFIED

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role";


    // ... (other table and column constants remain the same)
    public static final String TABLE_BOOKS = "books";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TITLE = "title";
    public static final String COL_BOOK_CATEGORY = "category";
    public static final String COL_BOOK_AUTHOR_ID = "author_id";
    public static final String COL_BOOK_AUTHOR_NAME = "author_name";
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
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_USERNAME + " TEXT UNIQUE NOT NULL," + COL_PASSWORD + " TEXT NOT NULL," + COL_EMAIL + " TEXT," // ADDED
            + COL_PHONE + " TEXT," + COL_ROLE + " TEXT NOT NULL DEFAULT 'Student'" + ")";

    // ... (CREATE_TABLE_AUTHORS, CREATE_TABLE_BOOKS, CREATE_TABLE_BORROW remain the same)
    private static final String CREATE_TABLE_AUTHORS = "CREATE TABLE " + TABLE_AUTHORS + "(" + COL_AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_AUTHOR_NAME + " TEXT UNIQUE NOT NULL" + ")";

    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + "(" + COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_BOOK_TITLE + " TEXT NOT NULL," + COL_BOOK_CATEGORY + " TEXT NOT NULL," + COL_BOOK_AUTHOR_ID + " INTEGER NOT NULL," + COL_BOOK_DESCRIPTION + " TEXT," + COL_BOOK_IS_BORROWED + " INTEGER NOT NULL DEFAULT 0," + "FOREIGN KEY(" + COL_BOOK_AUTHOR_ID + ") REFERENCES " + TABLE_AUTHORS + "(" + COL_AUTHOR_ID + ")" + ")";

    private static final String CREATE_TABLE_BORROW = "CREATE TABLE " + TABLE_BORROW + "(" + COL_BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_BORROW_BOOK_ID + " INTEGER NOT NULL," + COL_BORROW_STUDENT_ID + " INTEGER NOT NULL," + COL_BORROW_DATE + " TEXT NOT NULL," + COL_RETURN_DATE + " TEXT," + "FOREIGN KEY(" + COL_BORROW_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + ")," + "FOREIGN KEY(" + COL_BORROW_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" + ")";


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
     *
     * @param username The user's chosen username.
     * @param password The user's password.
     * @param email    The user's email address (can be null). // MODIFIED Javadoc
     * @param phone    The user's phone number (can be null). // MODIFIED Javadoc
     * @param role     The user's role ("Admin" or "Student").
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
     *
     * @param usernameOrEmail The username to check.
     * @param password        The password to check.
     * @return A Cursor containing user data if valid, null otherwise.
     */
    public Cursor getUser(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Query now needs to be able to fetch the email as well if needed upon login
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE (" + COL_USERNAME + " = ? OR " + COL_EMAIL + " = ?) AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{usernameOrEmail, usernameOrEmail, password});
        return cursor; // Caller must close the cursor
    }
//SELECT * FROM users_table WHERE (username = ? OR email = ?) AND password = ?

    /**
     * Checks if a username already exists.
     *
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

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
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

    public boolean addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_TITLE, book.getTitle());
        cv.put(COL_BOOK_CATEGORY, book.getCategory());
        cv.put(COL_BOOK_AUTHOR_ID, book.getAuthorId());
        cv.put(COL_BOOK_DESCRIPTION, book.getDescription());
        cv.put(COL_BOOK_IS_BORROWED, 0);
        long result = db.insert(TABLE_BOOKS, null, cv);
        // db.close(); // Keep db open if you are doing multiple operations, close it when done with the helper instance
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

    public boolean updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_TITLE, book.getTitle());
        values.put(COL_BOOK_CATEGORY, book.getCategory());
        values.put(COL_BOOK_AUTHOR_ID, book.getAuthorId());
        values.put(COL_BOOK_DESCRIPTION, book.getDescription());

        int rowsAffected = db.update(TABLE_BOOKS, values, COL_BOOK_ID + " = ?", new String[]{String.valueOf(book.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteBook(int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BOOKS, COL_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        db.close();
        return result > 0;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        String selectQuery = "SELECT b." + COL_BOOK_ID + ", b." + COL_BOOK_TITLE +
                ", b." + COL_BOOK_CATEGORY + ", b." + COL_BOOK_AUTHOR_ID +
                ", a." + COL_AUTHOR_NAME + ", b." + COL_BOOK_DESCRIPTION +
                " FROM " + TABLE_BOOKS + " b INNER JOIN " + TABLE_AUTHORS + " a " +
                "ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                // Create a new Book object for each row
                Book book = new Book();

                // Get column indices (do this once outside the loop for efficiency if preferred)
                book.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_ID)));
                book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_TITLE)));
                book.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_CATEGORY)));
                book.setAuthorId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_AUTHOR_ID))); // ✅ ID
                book.setAuthorName(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR_NAME))); // ✅ NAME
                book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_DESCRIPTION)));

                // Add book to list
                bookList.add(book);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database
        cursor.close();
//        db.close(); // Or manage db lifecycle elsewhere if appropriate
        Log.e("getAllBooks", "Book List Size: " + bookList.size());
        // Return the list of books
        return bookList;
    }


    public Cursor searchBooksByTitle(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " + "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID + " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getBooksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_CATEGORY + " = ?";
        String[] selectionArgs = new String[]{category};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " + "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID + " WHERE " + selection;
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getBooksByAuthor(int authorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BOOK_AUTHOR_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(authorId)};
        String sql = "SELECT b.*, a." + COL_AUTHOR_NAME + " FROM " + TABLE_BOOKS + " b " + "INNER JOIN " + TABLE_AUTHORS + " a ON b." + COL_BOOK_AUTHOR_ID + " = a." + COL_AUTHOR_ID + " WHERE " + selection;
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

    /**
     * Adds a new author to the database.
     * Checks if author already exists by name (case-insensitive for robustness, though UNIQUE constraint is case-sensitive by default in SQLite unless specified).
     *
     * @param name The name of the author.
     * @return The ID of the newly added author, or the ID of the existing author if name matches, or -1 if an error occurred.
     */
    public long addAuthor(String name) {
        if (name == null || name.trim().isEmpty()) {
            return -1; // Or throw an IllegalArgumentException
        }

        // First, check if author already exists to avoid duplicate UNIQUE constraint violation
        // and to return existing ID if found.
        long existingAuthorId = getAuthorIdByName(name);
        if (existingAuthorId != -1) {
            Log.d("DatabaseHelper", "Author '" + name + "' already exists with ID: " + existingAuthorId);
            return existingAuthorId; // Return existing ID
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AUTHOR_NAME, name.trim()); // Store trimmed name

        long result = -1;
        try {
            result = db.insert(TABLE_AUTHORS, null, cv);
            if (result != -1) {
                Log.d("DatabaseHelper", "Added new author '" + name + "' with ID: " + result);
            } else {
                Log.e("DatabaseHelper", "Failed to add author '" + name + "'. Insert returned -1.");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding author '" + name + "': " + e.getMessage());
        } finally {
            // db.close(); // Manage database connection lifecycle carefully.
            // Consider closing db in the activity/fragment when all operations are done.
        }
        return result;
    }

    /**
     * Checks if an author with the given name already exists in the database.
     * This check is case-sensitive by default due to how SQLite's TEXT comparison works without specific collations.
     * If you need case-insensitive, the query would need "COLLATE NOCASE".
     *
     * @param name The name of the author to check.
     * @return true if an author with that name exists, false otherwise.
     */
    public boolean authorExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            // Using parameterized query for safety
            cursor = db.query(TABLE_AUTHORS, new String[]{COL_AUTHOR_ID}, COL_AUTHOR_NAME + " = ?", new String[]{name.trim()}, null, null, null);
            if (cursor != null) {
                exists = cursor.getCount() > 0;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking if author exists: " + name + ", " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }
        return exists;
    }

    /**
     * Retrieves the ID of an author by their name.
     *
     * @param name The name of the author.
     * @return The ID of the author, or -1 if not found or an error occurs.
     */
    public long getAuthorIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        long authorId = -1;
        try {
            cursor = db.query(TABLE_AUTHORS, new String[]{COL_AUTHOR_ID}, COL_AUTHOR_NAME + " = ?", new String[]{name.trim()}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int colIdx = cursor.getColumnIndex(COL_AUTHOR_ID);
                if (colIdx != -1) {
                    authorId = cursor.getLong(colIdx);
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting author ID by name: " + name + ", " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }
        return authorId;
    }

    /**
     * Retrieves all authors from the database as a List of Author objects.
     *
     * @return A List of Author objects, ordered by name.
     */
    public List<Author> getAllAuthorsList() {
        List<Author> authorList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_AUTHORS, new String[]{COL_AUTHOR_ID, COL_AUTHOR_NAME}, null, null, null, null, COL_AUTHOR_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int idColIdx = cursor.getColumnIndex(COL_AUTHOR_ID);
                int nameColIdx = cursor.getColumnIndex(COL_AUTHOR_NAME);

                do {
                    int id = -1;
                    String name = null;

                    if (idColIdx != -1) id = cursor.getInt(idColIdx);
                    if (nameColIdx != -1) name = cursor.getString(nameColIdx);
                    if (id != -1 && name != null) {
                        authorList.add(new Author(id, name));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all authors: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close();
        }
        Log.d("DatabaseHelper", "Fetched " + authorList.size() + " authors.");
        return authorList;
    }

    public Cursor getAllAuthors() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_AUTHORS;
        return db.rawQuery(query, null);
    }
    /**
     * Retrieves the name of an author by their ID.
     *
     * @param authorId The ID of the author.
     * @return The name of the author, or null if not found or an error occurs.
     */
    public String getAuthorNameById(int authorId) {
        if (authorId <= 0) { // Basic validation
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String authorName = null;
        try {
            // Query the authors table for the author with the given ID
            cursor = db.query(
                    TABLE_AUTHORS,                     // Table to query
                    new String[]{COL_AUTHOR_NAME},     // Columns to return
                    COL_AUTHOR_ID + " = ?",            // WHERE clause
                    new String[]{String.valueOf(authorId)}, // Values for the WHERE clause
                    null,                              // Group by
                    null,                              // Having
                    null                               // Order by
            );
            if (cursor != null && cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex(COL_AUTHOR_NAME);
                if (nameColumnIndex != -1) {
                    authorName = cursor.getString(nameColumnIndex);
                } else {
                    Log.e("DatabaseHelper", "Column COL_AUTHOR_NAME not found inTABLE_AUTHORS.");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting author name by ID: " + authorId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close(); // Only close if this DatabaseHelper instance is not long-lived or shared.
            // If it's a singleton or managed by the Activity/Fragment lifecycle, don't close here.
        }
        return authorName;
    }
}