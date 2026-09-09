package org.example;

/**
 * represents a room (node) in the national gallery graph.
 * each room has a unique id used for graph lookups and equality checks,
 * and a display name shown in the user interface and route output.
 * equality and hashing are based on the room id only, ensuring rooms
 * are correctly identified across collections such as hashmap and hashset.
 */
public class Room {

    private String id;
    private String name;

    /**
     * constructs a room with the given id and display name.
     *
     * @param id the unique identifier for this room (e.g. "22", "central_hall")
     * @param name the full display name of the room (e.g. "Room 22 | Rembrandt")
     */
    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * returns the unique identifier of this room.
     *
     * @return the room id
     */
    public String getId() { return id; }

    /**
     * returns the display name of this room.
     *
     * @return the room name
     */
    public String getName() { return name; }

    /**
     * returns the display name of this room.
     * used by javafx combo boxes to show room names in the dropdown.
     *
     * @return the room name
     */
    @Override
    public String toString() { return name; }

    /**
     * checks equality based on room id only.
     * two rooms are equal if they have the same id, regardless of name.
     *
     * @param o the object to compare to
     * @return true if the other object is a room with the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        return id.equals(((Room) o).id);
    }

    /**
     * returns a hash code based on the room id.
     * consistent with equals so rooms work correctly in hashmap and hashset.
     *
     * @return hash code of the room id
     */
    @Override
    public int hashCode() { return id.hashCode(); }
}