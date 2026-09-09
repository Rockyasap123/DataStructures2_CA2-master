package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;

/**
 * javafx controller for the national gallery route finder application.
 * handles all user interactions including route finding via bfs, dijkstra,
 * dfs, pixel bfs, and the most interesting route algorithm.
 * manages the floor plan map display and output area.
 */
public class Controller {

    private Graph graph;
    private Map<String, List<Exhibit>> exhibitsByRoom;
    private Map<String, Room> roomById;
    private Image bwImage;
    private boolean pixelBFSMode = false;
    private int[] pixelStart = null;

    @FXML private ComboBox<Room> startBox;
    @FXML private ComboBox<Room> endBox;
    @FXML private TextArea outputArea;
    @FXML private TextArea artistInput;
    @FXML private TextField avoidInput;
    @FXML private TextField waypointInput;
    @FXML private Spinner<Integer> dfsLimitSpinner;
    @FXML private Pane mapPane;
    @FXML private ImageView mapImage;

    /**
     * initialises the application by loading the graph, images, and setting up
     * the map click handler. called automatically by javafx on startup.
     */
    @FXML
    public void initialize() {
        try {
            loadGraph();
            loadImages();
            setupMapClickHandler();
        } catch (Exception e) {
            outputArea.setText("Error loading graph: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * loads rooms, connections, and exhibits from csv files.
     * builds the room lookup map and populates the start/end combo boxes.
     *
     * @throws Exception if the csv files cannot be read
     */
    private void loadGraph() throws Exception {
        graph = GraphLoader.load("/data/rooms.csv", "/data/connections.csv");
        exhibitsByRoom = GraphLoader.loadExhibits("/data/exhibits.csv");

        roomById = new HashMap<>();
        for (Room r : graph.getNodes()) {
            roomById.put(r.getId(), r);
        }

        List<Room> sorted = new ArrayList<>(graph.getNodes());
        sorted.sort(Comparator.comparing(id -> extractSortKey(id.getId())));
        startBox.getItems().addAll(sorted);
        endBox.getItems().addAll(sorted);
    }

    /**
     * loads the colour floor plan image and the black and white image
     * used for pixel bfs into memory.
     */
    private void loadImages() {
        java.io.InputStream imgStream = getClass().getResourceAsStream("/images/gallery_floorplan.jpg");
        if (imgStream != null) {
            mapImage.setImage(new Image(imgStream));
        } else {
            outputArea.setText("Warning: floor plan image not found.");
        }

        java.io.InputStream bwStream = getClass().getResourceAsStream("/images/gallery_bwver.jpg");
        if (bwStream != null) {
            bwImage = new Image(bwStream);
        } else {
            System.err.println("Warning: B&W floor plan not found.");
        }
    }

    /**
     * sets up the mouse click handler on the map pane for pixel bfs.
     * first click selects the start point, second click selects the end point
     * and triggers the pixel bfs search automatically.
     */
    private void setupMapClickHandler() {
        mapPane.setOnMouseClicked(event -> {
            if (!pixelBFSMode) return;

            // convert pane coordinates to image coordinates
            double scaleX = bwImage.getWidth()  / mapPane.getWidth();
            double scaleY = bwImage.getHeight() / mapPane.getHeight();

            int imgX = (int)(event.getX() * scaleX);
            int imgY = (int)(event.getY() * scaleY);

            if (pixelStart == null) {
                // first click — store start point and draw green marker
                pixelStart = new int[]{imgX, imgY};
                outputArea.setText("Start point selected at (" + imgX + ", " + imgY + ").\nNow click your destination.");

                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(event.getX(), event.getY(), 6);
                dot.setFill(javafx.scene.paint.Color.GREEN);
                dot.setId("pixelBFSMarker");
                mapPane.getChildren().add(dot);

            } else {
                // second click — run pixel bfs between the two selected points
                int[] end = new int[]{imgX, imgY};
                outputArea.setText("Running pixel BFS...");

                List<int[]> path = PixelBFS.findPath(bwImage, pixelStart[0], pixelStart[1], end[0], end[1]);

                mapPane.getChildren().removeIf(n -> "pixelBFSMarker".equals(n.getId()));

                if (path.isEmpty()) {
                    outputArea.setText("No pixel path found. Try clicking inside a room.");
                } else {
                    drawPixelPath(path);
                    outputArea.setText("Pixel BFS complete.\nDistance: " + path.size() + " pixels.");
                }

                // reset pixel bfs state
                pixelBFSMode = false;
                pixelStart = null;
            }
        });
    }

    /**
     * handles the bfs shortest route button.
     * finds and displays the shortest route between the selected rooms using bfs.
     */
    @FXML
    public void handleBFS() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<Room> avoid = parseAvoid();
        List<Room> path = BFS.findPath(graph, start, end, avoid);

        outputArea.setText("BFS Shortest Route:\n" + formatPath(path));
    }

    /**
     * handles the pixel bfs button.
     * activates pixel bfs mode and prompts the user to click two points on the map.
     */
    @FXML
    public void handlePixelBFS() {
        pixelBFSMode = true;
        pixelStart = null;

        // clear any existing routes from the map
        mapPane.getChildren().removeIf(n ->
                n instanceof javafx.scene.shape.Line ||
                        n instanceof javafx.scene.shape.Circle ||
                        n instanceof javafx.scene.shape.Polyline
        );

        outputArea.setText("Pixel BFS mode active.\nClick your starting point on the map.");
    }

    /**
     * draws the pixel bfs path on the map as a red polyline.
     * samples every 3rd pixel for rendering efficiency.
     * places a green dot at the start and a red dot at the end.
     *
     * @param path list of pixel coordinates representing the path
     */
    private void drawPixelPath(List<int[]> path) {

        double scaleX = mapPane.getWidth()  / bwImage.getWidth();
        double scaleY = mapPane.getHeight() / bwImage.getHeight();

        // use a polyline for efficiency — one shape instead of thousands of lines
        javafx.scene.shape.Polyline polyline = new javafx.scene.shape.Polyline();
        polyline.setStroke(javafx.scene.paint.Color.RED);
        polyline.setStrokeWidth(2);
        polyline.setOpacity(0.8);

        // sample every 3rd pixel to keep rendering fast
        for (int i = 0; i < path.size(); i += 3) {
            int[] p = path.get(i);
            polyline.getPoints().addAll(
                    p[0] * scaleX,
                    p[1] * scaleY
            );
        }

        mapPane.getChildren().add(polyline);

        // green dot at start, red dot at end
        int[] start = path.get(0);
        int[] end   = path.get(path.size() - 1);

        javafx.scene.shape.Circle startDot = new javafx.scene.shape.Circle(
                start[0] * scaleX, start[1] * scaleY, 6,
                javafx.scene.paint.Color.GREEN
        );
        javafx.scene.shape.Circle endDot = new javafx.scene.shape.Circle(
                end[0] * scaleX, end[1] * scaleY, 6,
                javafx.scene.paint.Color.RED
        );

        mapPane.getChildren().addAll(startDot, endDot);
    }

    /**
     * handles the dijkstra shortest route button.
     * finds and displays the shortest weighted route using dijkstra's algorithm.
     * supports waypoints and avoid sets.
     */
    @FXML
    public void handleDijkstra() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();
        List<Room> path = runWithWaypoints(start, end, waypoints, avoid, false, null);

        outputArea.setText("Dijkstra Shortest Route:\n" + formatPath(path));
    }

    /**
     * handles the dfs multiple routes button.
     * finds multiple route permutations using depth-first search up to the user-specified limit.
     * supports waypoints and avoid sets.
     */
    @FXML
    public void handleDFS() {
        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        int limit = dfsLimitSpinner.getValue();
        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();

        // build list of stops including any waypoints
        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);

        StringBuilder sb = new StringBuilder();
        int count = 1;

        if (stops.size() == 2) {
            // no waypoints — normal dfs between start and end
            List<List<Room>> paths = DFSRoutes.findAllPaths(graph, start, end, limit, avoid);
            sb.append("DFS Routes (").append(paths.size()).append(" found):\n\n");
            for (List<Room> path : paths) {
                sb.append("Route ").append(count++).append(": ");
                sb.append(formatPath(path)).append("\n");
            }
        } else {
            // waypoints present — run dfs between each pair of stops
            sb.append("DFS Routes with waypoints:\n\n");
            for (int i = 0; i < stops.size() - 1; i++) {
                List<List<Room>> paths = DFSRoutes.findAllPaths(
                        graph, stops.get(i), stops.get(i + 1), limit, avoid
                );
                sb.append("Segment ").append(i + 1)
                        .append(" (").append(stops.get(i).getName())
                        .append(" → ").append(stops.get(i + 1).getName()).append("):\n");
                for (List<Room> path : paths) {
                    sb.append("  Route ").append(count++).append(": ");
                    sb.append(formatPath(path)).append("\n");
                }
            }
        }

        outputArea.setText(sb.toString());
    }

    /**
     * handles the most interesting route button.
     * finds a route that visits rooms containing the user's preferred artists.
     * supports waypoints and avoid sets.
     */
    @FXML
    public void handleInteresting() {

        Room start = startBox.getValue();
        Room end = endBox.getValue();

        if (!validate(start, end)) return;

        Set<String> artists = parseArtists();

        if (artists.isEmpty()) {
            outputArea.setText("Enter at least one preferred artist.");
            return;
        }

        Set<Room> avoid = parseAvoid();
        List<Room> waypoints = parseWaypoints();
        List<Room> path = runWithWaypoints(start, end, waypoints, avoid, true, artists);

        outputArea.setText("Most Interesting Route (artists: " + artists + "):\n" + formatPath(path));
    }

    /**
     * chains dijkstra calls through a list of waypoints.
     * builds the full path as: start -> wp1 -> wp2 -> ... -> end.
     * uses the interesting variant of dijkstra if the interesting flag is set.
     *
     * @param start the starting room
     * @param end the destination room
     * @param waypoints list of rooms that must be visited along the route
     * @param avoid set of rooms to exclude from the route
     * @param interesting whether to use the most interesting route variant
     * @param artists set of preferred artist names for the interesting route
     * @return the full path as a list of rooms
     */
    private List<Room> runWithWaypoints(Room start, Room end,
                                        List<Room> waypoints, Set<Room> avoid,
                                        boolean interesting, Set<String> artists) {
        List<Room> fullPath = new ArrayList<>();
        List<Room> stops = new ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);

        for (int i = 0; i < stops.size() - 1; i++) {
            Room from = stops.get(i);
            Room to   = stops.get(i + 1);

            List<Room> segment = interesting
                    ? Dijkstra.findInterestingPath(graph, from, to, avoid, exhibitsByRoom, artists)
                    : Dijkstra.findPath(graph, from, to, avoid);

            if (segment.isEmpty()) {
                outputArea.setText("No path found between " + from.getName() + " and " + to.getName());
                return new ArrayList<>();
            }

            // remove the first room of each segment to avoid duplicating the join node
            if (!fullPath.isEmpty()) segment.remove(0);
            fullPath.addAll(segment);
        }

        return fullPath;
    }

    /**
     * parses the avoid input field into a set of rooms.
     * ignores any room ids that are not found in the graph.
     *
     * @return set of rooms to avoid
     */
    private Set<Room> parseAvoid() {
        String text = avoidInput.getText().trim();
        if (text.isEmpty()) return Collections.emptySet();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(roomById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * parses the waypoint input field into an ordered list of rooms.
     * ignores any room ids that are not found in the graph.
     *
     * @return ordered list of waypoint rooms
     */
    private List<Room> parseWaypoints() {
        String text = waypointInput.getText().trim();
        if (text.isEmpty()) return Collections.emptyList();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(roomById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * parses the artist input field into a set of lowercase artist names.
     * normalises input to lowercase for case-insensitive matching.
     *
     * @return set of preferred artist names in lowercase
     */
    private Set<String> parseArtists() {
        String text = artistInput.getText().trim();
        if (text.isEmpty()) return Collections.emptySet();

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * validates that both a start and end room have been selected
     * and that they are not the same room.
     *
     * @param start the selected start room
     * @param end the selected end room
     * @return true if the selection is valid, false otherwise
     */
    private boolean validate(Room start, Room end) {
        if (start == null || end == null) {
            outputArea.setText("Please select both a start and end room.");
            return false;
        }
        if (start.equals(end)) {
            outputArea.setText("Start and end rooms are the same.");
            return false;
        }
        return true;
    }

    /**
     * formats a list of rooms as a readable path string with arrows between rooms.
     * also appends the total number of steps taken.
     *
     * @param path list of rooms representing the route
     * @return formatted string representation of the path
     */
    private String formatPath(List<Room> path) {
        if (path == null || path.isEmpty()) return "No path found.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size() - 1) sb.append("\n → ");
        }
        sb.append("\n(").append(path.size() - 1).append(" steps)");
        return sb.toString();
    }

    /**
     * generates a sort key for a room id so that rooms are ordered numerically
     * rather than alphabetically in the combo boxes.
     * numeric ids are zero-padded, non-numeric ids are sorted to the end.
     *
     * @param id the room id to generate a sort key for
     * @return a string sort key that produces correct numeric ordering
     */
    private static String extractSortKey(String id) {
        StringBuilder digits = new StringBuilder();
        StringBuilder rest = new StringBuilder();
        boolean digitsDone = false;

        for (char c : id.toCharArray()) {
            if (Character.isDigit(c) && !digitsDone) {
                digits.append(c);
            } else {
                digitsDone = true;
                rest.append(c);
            }
        }

        if (digits.length() == 0) {
            // no leading number — sort after all numbered rooms
            return "zz_" + id;
        }

        // zero-pad the numeric part so string sort matches numeric sort
        return String.format("%010d", Integer.parseInt(digits.toString())) + rest.toString();
    }
}