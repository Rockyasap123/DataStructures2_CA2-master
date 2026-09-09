package org.example;

import java.io.*;
import java.util.*;

/**
 * loads the national gallery graph data from csv files on the classpath.
 * reads rooms and connections to build the graph, and reads exhibits
 * to build the room-to-exhibit lookup map used by the interesting route algorithm.
 * csv files are read from the resources directory and should be easy to update
 * manually to add new rooms, connections, or exhibits.
 */
public class GraphLoader {

    /**
     * loads rooms and connections from csv files and builds the graph.
     * the rooms file must have columns: room_id, name.
     * the connections file must have columns: from, to, distance.
     * skips any connections that reference unknown room ids with a warning.
     *
     * @param roomsFile path to the rooms csv file on the classpath
     * @param connectionsFile path to the connections csv file on the classpath
     * @return the fully constructed graph with all rooms and edges
     * @throws Exception if either file cannot be read
     */
    public static Graph load(String roomsFile, String connectionsFile) throws Exception {

        Graph graph = new Graph();
        Map<String, Room> roomMap = new HashMap<>();

        // -------------------
        // load rooms
        // -------------------
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(roomsFile)
                )
        );

        br.readLine(); // skip header row

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // split into at most 2 parts — name may contain commas
            String[] parts = line.split(",", 2);
            String id   = parts[0].trim();
            String name = parts[1].trim();

            Room room = new Room(id, name);
            roomMap.put(id, room);
            graph.addNode(room);
        }
        br.close();

        // -------------------
        // load connections
        // -------------------
        br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(connectionsFile)
                )
        );

        br.readLine(); // skip header row

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            String fromId  = parts[0].trim();
            String toId    = parts[1].trim();
            double weight  = Double.parseDouble(parts[2].trim());

            Room from = roomMap.get(fromId);
            Room to   = roomMap.get(toId);

            // warn and skip if either room id is not found in the rooms file
            if (from == null || to == null) {
                System.err.println("warning: unknown room in connection: " + fromId + " -> " + toId);
                continue;
            }

            graph.addEdge(from, to, weight);
        }
        br.close();

        return graph;
    }

    /**
     * loads exhibits from a csv file and returns a map of room id to exhibit list.
     * the exhibits file must have columns: exhibit_id, title, artist, room_id.
     * splits on at most 4 parts to allow commas in title and artist name fields.
     *
     * @param exhibitsFile path to the exhibits csv file on the classpath
     * @return map of room id to list of exhibits in that room
     * @throws Exception if the file cannot be read
     */
    public static Map<String, List<Exhibit>> loadExhibits(String exhibitsFile) throws Exception {

        Map<String, List<Exhibit>> byRoom = new HashMap<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        GraphLoader.class.getResourceAsStream(exhibitsFile)
                )
        );

        br.readLine(); // skip header row

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // limit 4 — title and artist name may contain commas
            String[] parts = line.split(",", 4);
            String title  = parts[0].trim();
            String artist = parts[1].trim();
            String roomId = parts[2].trim();

            Exhibit exhibit = new Exhibit(title, artist);
            byRoom.computeIfAbsent(roomId, k -> new ArrayList<>()).add(exhibit);
        }
        br.close();

        return byRoom;
    }
}