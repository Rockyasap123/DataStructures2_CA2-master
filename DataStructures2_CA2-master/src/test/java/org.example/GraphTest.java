package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    private Graph graph;
    private Room roomA, roomB, roomC, roomD, roomE;

    // builds a simple known graph before each test:
    //
    //  A --1-- B --1-- C
    //  |               |
    //  1               1
    //  |               |
    //  D --1-- E ------+
    //
    @BeforeEach
    public void setUp() {
        graph = new Graph();

        roomA = new Room("A", "Room A");
        roomB = new Room("B", "Room B");
        roomC = new Room("C", "Room C");
        roomD = new Room("D", "Room D");
        roomE = new Room("E", "Room E");

        graph.addNode(roomA);
        graph.addNode(roomB);
        graph.addNode(roomC);
        graph.addNode(roomD);
        graph.addNode(roomE);

        graph.addEdge(roomA, roomB, 1);
        graph.addEdge(roomB, roomC, 1);
        graph.addEdge(roomA, roomD, 1);
        graph.addEdge(roomD, roomE, 1);
        graph.addEdge(roomE, roomC, 1);
    }

    // -----------------------
    // GRAPH TESTS
    // -----------------------
    @Test
    public void testGraphHasCorrectNodeCount() {
        assertEquals(5, graph.getNodes().size());
    }

    @Test
    public void testGraphNeighboursCorrect() {
        List<Edge> neighbours = graph.getNeighbors(roomA);
        assertEquals(2, neighbours.size());
    }

    @Test
    public void testBidirectionalEdge() {
        // A->B should mean B->A also exists
        boolean bToA = graph.getNeighbors(roomB)
                .stream().anyMatch(e -> e.target.equals(roomA));
        assertTrue(bToA);
    }

    // -----------------------
    // BFS TESTS
    // -----------------------
    @Test
    public void testBFSFindsPath() {
        List<Room> path = BFS.findPath(graph, roomA, roomC, Collections.emptySet());
        assertFalse(path.isEmpty());
    }

    @Test
    public void testBFSPathStartsAndEndsCorrectly() {
        List<Room> path = BFS.findPath(graph, roomA, roomC, Collections.emptySet());
        assertEquals(roomA, path.get(0));
        assertEquals(roomC, path.get(path.size() - 1));
    }

    @Test
    public void testBFSShortestPath() {
        // shortest from A to C is A->B->C (2 steps)
        List<Room> path = BFS.findPath(graph, roomA, roomC, Collections.emptySet());
        assertEquals(3, path.size()); // 3 rooms = 2 steps
    }

    @Test
    public void testBFSRespectsAvoid() {
        // avoid B — forces path A->D->E->C
        Set<Room> avoid = new HashSet<>(Collections.singletonList(roomB));
        List<Room> path = BFS.findPath(graph, roomA, roomC, avoid);
        assertFalse(path.contains(roomB));
        assertEquals(roomC, path.get(path.size() - 1));
    }

    @Test
    public void testBFSNoPathReturnsEmpty() {
        // isolate roomC by avoiding both routes to it
        Set<Room> avoid = new HashSet<>(Arrays.asList(roomB, roomE));
        List<Room> path = BFS.findPath(graph, roomA, roomC, avoid);
        assertTrue(path.isEmpty());
    }

    // -----------------------
    // DIJKSTRA TESTS
    // -----------------------
    @Test
    public void testDijkstraFindsPath() {
        List<Room> path = Dijkstra.findPath(graph, roomA, roomC, Collections.emptySet());
        assertFalse(path.isEmpty());
    }

    @Test
    public void testDijkstraPathStartsAndEndsCorrectly() {
        List<Room> path = Dijkstra.findPath(graph, roomA, roomC, Collections.emptySet());
        assertEquals(roomA, path.get(0));
        assertEquals(roomC, path.get(path.size() - 1));
    }

    @Test
    public void testDijkstraShortestPath() {
        // all edges weight 1, shortest A to C is A->B->C
        List<Room> path = Dijkstra.findPath(graph, roomA, roomC, Collections.emptySet());
        assertEquals(3, path.size());
    }

    @Test
    public void testDijkstraRespectsAvoid() {
        Set<Room> avoid = new HashSet<>(Collections.singletonList(roomB));
        List<Room> path = Dijkstra.findPath(graph, roomA, roomC, avoid);
        assertFalse(path.contains(roomB));
    }

    @Test
    public void testDijkstraWeightedPath() {
        // add a heavy edge A->C directly (cost 10) — Dijkstra should still prefer A->B->C (cost 2)
        graph.addEdge(roomA, roomC, 10);
        List<Room> path = Dijkstra.findPath(graph, roomA, roomC, Collections.emptySet());
        assertEquals(3, path.size()); // still goes via B
    }

    // -----------------------
    // DFS TESTS
    // -----------------------
    @Test
    public void testDFSFindsAtLeastOnePath() {
        List<List<Room>> paths = DFSRoutes.findAllPaths(
                graph, roomA, roomC, 10, Collections.emptySet()
        );
        assertFalse(paths.isEmpty());
    }

    @Test
    public void testDFSAllPathsEndAtDestination() {
        List<List<Room>> paths = DFSRoutes.findAllPaths(
                graph, roomA, roomC, 10, Collections.emptySet()
        );
        for (List<Room> path : paths) {
            assertEquals(roomC, path.get(path.size() - 1));
        }
    }

    @Test
    public void testDFSRespectsLimit() {
        List<List<Room>> paths = DFSRoutes.findAllPaths(
                graph, roomA, roomC, 1, Collections.emptySet()
        );
        assertEquals(1, paths.size());
    }

    @Test
    public void testDFSRespectsAvoid() {
        Set<Room> avoid = new HashSet<>(Collections.singletonList(roomB));
        List<List<Room>> paths = DFSRoutes.findAllPaths(
                graph, roomA, roomC, 10, avoid
        );
        for (List<Room> path : paths) {
            assertFalse(path.contains(roomB));
        }
    }
}