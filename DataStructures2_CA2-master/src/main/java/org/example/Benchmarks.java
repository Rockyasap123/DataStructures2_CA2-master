package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class Benchmarks {

    private Graph graph;
    private Room roomStart;
    private Room roomEnd;
    private Map<String, List<Exhibit>> exhibitsByRoom;
    private Set<String> preferredArtists;

    // -----------------------
    // SETUP — runs once before benchmarks
    // builds a graph matching the real gallery layout
    // -----------------------
    @Setup
    public void setup() throws Exception {
        graph = GraphLoader.load("/data/rooms.csv", "/data/connections.csv");
        exhibitsByRoom = GraphLoader.loadExhibits("/data/exhibits.csv");

        // find Room 2 and Room 33 as start/end — long cross-gallery route
        for (Room r : graph.getNodes()) {
            if (r.getId().equals("2"))  roomStart = r;
            if (r.getId().equals("33")) roomEnd   = r;
        }

        preferredArtists = new HashSet<>(Collections.singletonList("rembrandt"));
    }

    // -----------------------
    // BFS BENCHMARK
    // -----------------------
    @Benchmark
    public List<Room> benchmarkBFS() {
        return BFS.findPath(graph, roomStart, roomEnd, Collections.emptySet());
    }

    // -----------------------
    // DIJKSTRA BENCHMARK
    // -----------------------
    @Benchmark
    public List<Room> benchmarkDijkstra() {
        return Dijkstra.findPath(graph, roomStart, roomEnd, Collections.emptySet());
    }

    // -----------------------
    // DFS BENCHMARK
    // -----------------------
    @Benchmark
    public List<List<Room>> benchmarkDFS() {
        return DFSRoutes.findAllPaths(graph, roomStart, roomEnd, 5, Collections.emptySet());
    }

    // -----------------------
    // MOST INTERESTING BENCHMARK
    // -----------------------
    @Benchmark
    public List<Room> benchmarkInteresting() {
        return Dijkstra.findInterestingPath(
                graph, roomStart, roomEnd,
                Collections.emptySet(),
                exhibitsByRoom,
                preferredArtists
        );
    }

    // -----------------------
    // MAIN — run benchmarks directly
    // -----------------------
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}