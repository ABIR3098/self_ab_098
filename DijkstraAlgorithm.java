package com.ambulance.routing;

import java.util.*;

public class DijkstraAlgorithm {

    public static List<Integer> getShortestPath(int[][] adjacencyMatrix, int start, int end) {

        int n = adjacencyMatrix.length;
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        for (int i = 0; i < n; i++) {
            int u = -1, min = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && dist[j] < min) {
                    min = dist[j];
                    u = j;
                }
            }
            if (u == -1) break;
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (adjacencyMatrix[u][v] > 0 &&
                        dist[u] + adjacencyMatrix[u][v] < dist[v]) {
                    dist[v] = dist[u] + adjacencyMatrix[u][v];
                    prev[v] = u;
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at]) path.add(at);
        Collections.reverse(path);

        return path.get(0) == start ? path : null;
    }

    public static int calculateTotalDistance(int[][] adjacencyMatrix, List<Integer> path) {
        int sum = 0;
        for (int i = 0; i < path.size() - 1; i++)
            sum += adjacencyMatrix[path.get(i)][path.get(i + 1)];
        return sum;
    }
}
