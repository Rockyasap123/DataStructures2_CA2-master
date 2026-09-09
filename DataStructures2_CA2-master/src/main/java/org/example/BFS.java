package org.example;

import java.util.*;

/**
 * breadth-first search implementation for finding the shortest route
 * between two rooms in terms of number of hops.
 * supports avoid sets to exclude specific rooms from the route.
 */
public class BFS {

    /**
     * finds the shortest path from start to end using bfs.
     * avoids any rooms in the avoid set.
     * returns an empty list if no path exists.
     *
     * @param graph the graph to search
     * @param start the starting room
     * @param end the destination room
     * @param avoid set of rooms to exclude from the route
     * @return list of rooms representing the shortest path, or empty list if no path found
     */
    public static List<Room> findPath(Graph graph, Room start, Room end, Set<Room> avoid) {

        Queue<Room> queue = new LinkedList<>();
        Map<Room, Room> prev = new HashMap<>();
        Set<Room> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {

            Room current = queue.poll();

            if (current.equals(end)) break;

            for (Edge e : graph.getNeighbors(current)) {

                Room next = e.target;

                // only visit unvisited rooms that are not in the avoid set
                if (!visited.contains(next) && !avoid.contains(next)) {
                    visited.add(next);
                    prev.put(next, current);
                    queue.add(next);
                }
            }
        }

        return reconstruct(prev, start, end);
    }

    /**
     * reconstructs the path from start to end by walking back through the prev map.
     * returns an empty list if no path was found.
     *
     * @param prev map of each room to the room it was reached from
     * @param start the starting room
     * @param end the destination room
     * @return list of rooms from start to end, or empty list if no path found
     */
    private static List<Room> reconstruct(Map<Room, Room> prev, Room start, Room end) {

        List<Room> path = new ArrayList<>();
        Room current = end;

        // walk back from end to start using the prev map
        while (current != null) {
            path.add(current);
            current = prev.get(current);
        }

        Collections.reverse(path);

        // verify the path actually starts at the start room
        if (!path.isEmpty() && path.get(0).equals(start)) {
            return path;
        }

        return new ArrayList<>();
    }
}