package com.example.libraryproject;

public class Author {
    private int id;
    private String name;

    // Default constructor (needed for some libraries/operations, good practice)
    public Author() {
    }

    // Constructor to create an Author object with id and name
    public Author(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor to create an Author object with just name (e.g., before it's saved to DB and gets an ID)
    public Author(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This is crucial for displaying the Author's name in an ArrayAdapter (e.g., for a Spinner).
     * When an ArrayAdapter is given a list of Author objects, it will call toString() on each
     * object to get the text to display.
     */
    @Override
    public String toString() {
        return name; // Display the author's name
    }

    // Optional: equals() and hashCode() if you plan to store Author objects in Sets or use them as keys in Maps.
    // For basic Spinner usage, toString() is the most important.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return id == author.id && (name != null ? name.equals(author.name) : author.name == null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
