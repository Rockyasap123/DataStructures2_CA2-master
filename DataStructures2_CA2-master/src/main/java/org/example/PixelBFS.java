package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * pixel-level breadth-first search on the black and white floor plan image.
 * treats light pixels (brightness above 0.5) as walkable and dark pixels as walls.
 * finds the shortest pixel path between two clicked points on the map and
 * returns it as a list of pixel coordinates for drawing on the floor plan.
 */
public class PixelBFS {

    /**
     * finds the shortest pixel path between two points on the black and white image.
     * uses 4-directional bfs (up, down, left, right) expanding pixel by pixel.
     * returns an empty list if either point is on a non-walkable pixel or no path exists.
     *
     * @param bwImage the black and white floor plan image
     * @param startX the x coordinate of the start pixel
     * @param startY the y coordinate of the start pixel
     * @param endX the x coordinate of the end pixel
     * @param endY the y coordinate of the end pixel
     * @return list of int[]{x, y} pixel coordinates representing the path,
     *         or an empty list if no path was found
     */
    public static List<int[]> findPath(Image bwImage, int startX, int startY, int endX, int endY) {

        int width  = (int) bwImage.getWidth();
        int height = (int) bwImage.getHeight();

        PixelReader pr = bwImage.getPixelReader();

        // return empty if either endpoint is on a non-walkable pixel
        if (!isWalkable(pr, startX, startY) || !isWalkable(pr, endX, endY)) {
            return new ArrayList<>();
        }

        // prev[y][x] stores the pixel we came from — used to reconstruct the path
        int[][][] prev = new int[height][width][];
        boolean[][] visited = new boolean[height][width];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        // 4-directional movement vectors: up, down, left, right
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};

        while (!queue.isEmpty()) {

            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            // stop early once the destination pixel is reached
            if (cx == endX && cy == endY) break;

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                // skip pixels outside image bounds
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (visited[ny][nx]) continue;
                if (!isWalkable(pr, nx, ny)) continue;

                visited[ny][nx] = true;
                prev[ny][nx] = new int[]{cx, cy};
                queue.add(new int[]{nx, ny});
            }
        }

        return reconstruct(prev, startX, startY, endX, endY);
    }

    /**
     * checks whether a pixel is walkable by testing its brightness.
     * pixels with brightness above 0.5 are treated as walkable (white/light areas).
     * pixels below this threshold are treated as walls (dark areas).
     *
     * @param pr the pixel reader for the image
     * @param x the x coordinate of the pixel
     * @param y the y coordinate of the pixel
     * @return true if the pixel is walkable, false if it is a wall
     */
    private static boolean isWalkable(PixelReader pr, int x, int y) {
        Color c = pr.getColor(x, y);
        return c.getBrightness() > 0.5;
    }

    /**
     * reconstructs the pixel path from start to end by walking back through the prev array.
     * returns an empty list if no path was found to the end point.
     *
     * @param prev 2d array storing the previous pixel for each visited pixel
     * @param startX the x coordinate of the start pixel
     * @param startY the y coordinate of the start pixel
     * @param endX the x coordinate of the end pixel
     * @param endY the y coordinate of the end pixel
     * @return list of pixel coordinates from start to end, or empty list if no path found
     */
    private static List<int[]> reconstruct(int[][][] prev,
                                           int startX, int startY,
                                           int endX,   int endY) {
        List<int[]> path = new ArrayList<>();
        int[] current = {endX, endY};

        // walk back from end to start using the prev array
        while (current != null) {
            path.add(current);
            int[] p = prev[current[1]][current[0]];
            if (p == null && (current[0] != startX || current[1] != startY)) {
                // reached a dead end before finding the start — no path exists
                return new ArrayList<>();
            }
            current = p;
        }

        Collections.reverse(path);
        return path;
    }
}