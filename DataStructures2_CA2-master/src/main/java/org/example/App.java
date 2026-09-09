package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/example/primary.fxml"));
        Scene scene = new Scene(loader.load(), 1600, 900);

        stage.setTitle("National Gallery Route Finder");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

// ---
// references
/*
graph data str
https://www.geeksforgeeks.org/graph-data-structure-and-algorithms/
https://www.geeksforgeeks.org/adjacency-list-meaning-definition-in-dsa/
bfs
https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/
https://www.w3schools.com/dsa/dsa_algo_graphs_bfs.php
dfs
https://www.geeksforgeeks.org/depth-first-search-or-dfs-for-a-graph/
https://www.w3schools.com/dsa/dsa_algo_graphs_dfs.php
dijkstras shortest
https://www.geeksforgeeks.org/dijkstras-shortest-path-algorithm-greedy-algo-7/
https://www.w3schools.com/dsa/dsa_algo_graphs_dijkstra.php
priority queue
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html
hashmap
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html
https://www.w3schools.com/java/java_hashmap.asp
arraylists
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html
https://www.w3schools.com/java/java_arraylist.asp
hashsets
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html
https://www.w3schools.com/java/java_hashset.asp
 */