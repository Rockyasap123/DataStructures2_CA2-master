package org.example;

/**
 * represents an art exhibit displayed in a room of the national gallery.
 * each exhibit has a title and artist name used for route interest scoring.
 */
public class Exhibit {

    private String title;
    private String artist;

    /**
     * constructs an exhibit with the given details.
     * the id and roomId parameters are accepted for csv compatibility
     * but are not stored in this class.
     *
     * @param title the title of the artwork
     * @param artist the name of the artist
     */
    public Exhibit(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    /**
     * returns the name of the artist who created this exhibit.
     *
     * @return the artist name
     */
    public String getArtist() { return artist; }

    /**
     * returns a string representation of the exhibit
     * in the format "title — artist".
     *
     * @return formatted string of title and artist
     */
    @Override
    public String toString() {
        return title + " — " + artist;
    }
}