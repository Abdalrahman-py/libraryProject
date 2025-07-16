package com.example.libraryproject;

public class Book {
    private int id;
    private String title;
    private String category;
    private int authorId;


    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    private String authorName;
    private String description;
    private boolean isBorrowed;

    // Constructors
    public Book() {
    }

    public Book(int id, String title, String category, int authorId, String description, boolean isBorrowed) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.authorId = authorId;
        this.description = description;
        this.isBorrowed = isBorrowed;
    }

    public Book(String title, String category, int authorId, String authorName, String description) {
        this.title = title;
        this.category = category;
        this.authorName = authorName;
        this.authorId = authorId; // Stores authorId
        this.description = description;
        this.isBorrowed = false; // Default for new books
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        this.isBorrowed = borrowed;
    }

    // Optional: Override toString() for debugging
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", authorName=" +authorName + '\'' +
                ", authorId=" + authorId + '\'' +
                ", description='" + description + '\'' +
                ", isBorrowed=" + isBorrowed +
                '}';
    }
}