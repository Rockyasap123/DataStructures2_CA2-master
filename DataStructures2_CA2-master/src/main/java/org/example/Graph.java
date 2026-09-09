package org.example;

import java.util.*;

/**
 * custom graph data structure using an adjacency list representation.
 * rooms are stored as nodes/vertices and doorways or corridors between
 * rooms are stored as bidirectional weighted edges.
 * forms the core data structure for the national gallery route finder.
 */
public class Graph {

    // adjacency list mapping each room to its list of outgoing edges
    private final Map<Room, List<Edge>> adjList = new HashMap<>();

    /**
     * adds a room to the graph as a node with no connections.
     * if the room already exists in the graph, no change is made.
     *
     * @param room the room to add
     */
    public void addNode(Room room) {
        adjList.putIfAbsent(room, new ArrayList<>());
    }

    /**
     * adds a bidirectional weighted edge between two rooms.
     * both rooms are added to the graph if they do not already exist.
     * creates two directed edges — one in each direction — to represent
     * a two-way connection such as a doorway or corridor.
     *
     * @param from the first room
     * @param to the second room
     * @param weight the traversal cost of the connection
     */
    public void addEdge(Room from, Room to, double weight) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());

        adjList.get(from).add(new Edge(to, weight));
        adjList.get(to).add(new Edge(from, weight));
    }

    /**
     * returns the list of edges (neighbours) for the given room.
     * returns an empty list if the room has no connections or is not in the graph.
     *
     * @param room the room to get neighbours for
     * @return list of edges from the given room
     */
    public List<Edge> getNeighbors(Room room) {
        return adjList.getOrDefault(room, new ArrayList<>());
    }

    /**
     * returns all rooms currently stored in the graph.
     *
     * @return set of all rooms in the graph
     */
    public Set<Room> getNodes() {
        return adjList.keySet();
    }
}