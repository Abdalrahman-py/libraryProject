// Book.java
package com.example.libraryproject; // Your package name

public class Book {
    private int id;
    private String title;
    private String category;
    private String author;
    private String description;

    // Constructors
    public Book() {
    }

    public Book(int id, String title, String category, String author, String description) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.author = author;
        this.description = description;
    }

    public Book(String title, String category, String author, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.description = description;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Optional: Override toString() for debugging
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}