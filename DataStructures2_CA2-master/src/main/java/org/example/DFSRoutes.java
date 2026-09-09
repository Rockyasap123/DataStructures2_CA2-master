package org.example;

import java.util.*;

/**
 * depth-first search implementation for finding multiple route permutations
 * between two rooms in the national gallery graph.
 * returns up to a user-specified limit of distinct paths,
 * respecting avoid sets and optional waypoints.
 */
public class DFSRoutes {

    /**
     * finds all paths between start and end up to the specified limit.
     * uses recursive dfs with backtracking to explore all possible routes.
     *
     * @param graph the graph to search
     * @param start the starting room
     * @param end the destination room
     * @param limit the maximum number of paths to return
     * @param avoid set of rooms to exclude from all routes
     * @return list of paths, where each path is a list of rooms
     */
    public static List<List<Room>> findAllPaths(Graph graph, Room start, Room end,
                                                int limit, Set<Room> avoid) {

        List<List<Room>> results = new ArrayList<>();
        Set<Room> visited = new HashSet<>();

        dfs(graph, start, end, visited, new ArrayList<>(), results, limit, avoid);

        return results;
    }

    /**
     * recursive dfs helper that explores all paths from the current room to end.
     * backtracks by removing the current room from the path and visited set
     * after all neighbours have been explored.
     *
     * @param graph the graph to search
     * @param current the room currently being visited
     * @param end the destination room
     * @param visited set of rooms already on the current path
     * @param path the current path being built
     * @param results the list of completed paths found so far
     * @param limit the maximum number of paths to find
     * @param avoid set of rooms to exclude from all routes
     */
    private static void dfs(Graph graph, Room current, Room end, Set<Room> visited, List<Room> path, List<List<Room>> results, int limit, Set<Room> avoid) {

        path.add(current);
        visited.add(current);

        if (current.equals(end)) {
            // reached the destination — save a copy of the current path
            results.add(new ArrayList<>(path));
        } else {
            for (Edge e : graph.getNeighbors(current)) {
                // only explore unvisited neighbours not in the avoid set
                if (!visited.contains(e.target)
                        && !avoid.contains(e.target)
                        && results.size() < limit) {
                    dfs(graph, e.target, end, visited, path, results, limit, avoid);
                }
            }
        }

        // backtrack — remove current room from path and visited
        path.remove(path.size() - 1);
        visited.remove(current);
    }
}