package org.example;

import java.util.*;

/**
 * dijkstra's algorithm implementation for finding the shortest weighted route
 * between two rooms in the national gallery graph.
 * also provides a most interesting route variant that greedily visits all rooms
 * containing preferred artists along the way.
 */
public class Dijkstra {

    /**
     * finds the shortest weighted path from start to end using dijkstra's algorithm.
     * uses a priority queue with lazy deletion to handle updated distances efficiently.
     * avoids any rooms in the avoid set.
     *
     * @param graph the graph to search
     * @param start the starting room
     * @param end the destination room
     * @param avoid set of rooms to exclude from the route
     * @return list of rooms representing the shortest path, or empty list if no path found
     */
    public static List<Room> findPath(Graph graph, Room start, Room end,
                                      Set<Room> avoid) {

        Map<Room, Double> dist = new HashMap<>();
        Map<Room, Room> prev = new HashMap<>();

        // priority queue ordered by current best known distance
        PriorityQueue<Room> pq = new PriorityQueue<>(
                Comparator.comparingDouble(r -> dist.getOrDefault(r, Double.POSITIVE_INFINITY))
        );

        // initialise all distances to infinity
        for (Room r : graph.getNodes()) {
            dist.put(r, Double.POSITIVE_INFINITY);
        }

        dist.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {

            Room current = pq.poll();
            if (current.equals(end)) break;

            for (Edge e : graph.getNeighbors(current)) {

                if (avoid.contains(e.target)) continue;

                double newDist = dist.get(current) + e.weight;

                // only update if a shorter path to this neighbour has been found
                if (newDist < dist.getOrDefault(e.target, Double.POSITIVE_INFINITY)) {
                    dist.put(e.target, newDist);
                    prev.put(e.target, current);
                    pq.add(e.target);
                }
            }
        }

        return reconstruct(prev, start, end);
    }

    /**
     * finds the most interesting path from start to end by visiting all rooms
     * that contain exhibits by the preferred artists.
     * uses a greedy nearest-neighbour approach to order the interesting rooms,
     * then chains dijkstra calls between each stop.
     * falls back to the standard shortest path if no interesting rooms are found.
     *
     * @param graph the graph to search
     * @param start the starting room
     * @param end the destination room
     * @param avoid set of rooms to exclude from the route
     * @param exhibitsByRoom map of room ids to their list of exhibits
     * @param preferredArtists set of lowercase artist names the visitor is interested in
     * @return list of rooms representing the most interesting path
     */
    public static List<Room> findInterestingPath(Graph graph, Room start, Room end, Set<Room> avoid, Map<String, List<Exhibit>> exhibitsByRoom, Set<String> preferredArtists) {

        // collect all rooms that contain at least one preferred artist
        List<Room> interestingRooms = new ArrayList<>();
        for (Room r : graph.getNodes()) {
            if (avoid.contains(r) || r.equals(start) || r.equals(end)) continue;
            List<Exhibit> exhibits = exhibitsByRoom.getOrDefault(
                    r.getId(), Collections.emptyList()
            );
            boolean hasInterest = exhibits.stream()
                    .anyMatch(ex -> preferredArtists.contains(ex.getArtist().toLowerCase()));
            if (hasInterest) {
                interestingRooms.add(r);
            }
        }

        // if no interesting rooms found, fall back to standard shortest path
        if (interestingRooms.isEmpty()) {
            return findPath(graph, start, end, avoid);
        }

        // greedily order interesting rooms by proximity — always visit the nearest next
        List<Room> orderedWaypoints = new ArrayList<>();
        List<Room> remaining = new ArrayList<>(interestingRooms);
        Room current = start;

        while (!remaining.isEmpty()) {
            Room nearest = null;
            double nearestCost = Double.MAX_VALUE;

            for (Room candidate : remaining) {
                List<Room> path = findPath(graph, current, candidate, avoid);
                double cost = pathCost(path);
                if (cost < nearestCost) {
                    nearestCost = cost;
                    nearest = candidate;
                }
            }

            if (nearest == null) break;
            orderedWaypoints.add(nearest);
            remaining.remove(nearest);
            current = nearest;
        }

        // build full path by chaining dijkstra between each ordered stop
        List<Room> fullPath = new ArrayList<>();
        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(orderedWaypoints);
        stops.add(end);

        for (int i = 0; i < stops.size() - 1; i++) {
            List<Room> segment = findPath(graph, stops.get(i), stops.get(i + 1), avoid);
            if (segment.isEmpty()) continue;
            // remove the first room of each segment to avoid duplicating join nodes
            if (!fullPath.isEmpty()) segment.remove(0);
            fullPath.addAll(segment);
        }

        return fullPath;
    }

    /**
     * returns the cost of a path as the number of edges traversed.
     * returns max value if the path is empty to indicate no path was found.
     *
     * @param path the path to calculate the cost of
     * @return the number of steps in the path, or double max value if empty
     */
    private static double pathCost(List<Room> path) {
        return path.isEmpty() ? Double.MAX_VALUE : path.size() - 1;
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