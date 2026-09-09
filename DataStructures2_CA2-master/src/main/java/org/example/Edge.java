package org.example;

/**
 * represents a weighted directed edge between two rooms in the national gallery graph.
 * each edge connects a source room to a target room with a traversal cost (weight).
 * edges are created in pairs by the graph to represent bidirectional connections
 * such as doorways and corridors between rooms.
 */
public class Edge {

    /** the room this edge leads to */
    public Room target;

    /** the traversal cost of this edge — currently 1 for all direct connections */
    public double weight;

    /**
     * constructs an edge to the given target room with the specified weight.
     *
     * @param target the room this edge leads to
     * @param weight the traversal cost of this edge
     */
    public Edge(Room target, double weight) {
        this.target = target;
        this.weight = weight;
    }
}