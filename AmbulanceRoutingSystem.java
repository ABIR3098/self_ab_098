package com.ambulance.routing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class AmbulanceRoutingSystem extends JPanel {

    private int[][] adjacencyMatrix = {
            { 0, 4, 0, 0, 0, 0,0,0,2,4 },//A
            { 4, 0, 5, 0, 0, 0,0,0,0,0 },//B
            { 0, 5, 0, 0, 0, 0,0,5 ,0,0},//C
            { 0, 0, 0, 0, 0, 2 ,2,0,0,3},//D
            { 0, 0, 0, 0, 0, 3,0,0,1,0 },//E
            { 0, 0, 0, 2, 3, 0,0,0,0,0 },//F
            { 0, 0, 0, 2, 0, 0,0,4 ,0,0},//G
            { 0, 0, 5, 0, 0, 0,4,0 ,0,2},//H
            { 2, 0, 0, 0, 1, 0,0,0,0,0 },//I
            { 4, 0, 0, 3, 0, 0,0,2,0,0 }//P
    };

    private final Node[] nodes = {
            new Node("A", "Hospital", new java.awt.geom.Point2D.Double(0.55, 0.26)),
            new Node("B", "MIRPUR-13", new java.awt.geom.Point2D.Double(0.14, 0.26)),
            new Node("C", "MIRPUR-14", new java.awt.geom.Point2D.Double(0.14, 0.49)),
            new Node("D", "BUP", new java.awt.geom.Point2D.Double(0.55, 0.81)),
            new Node("E", "MIRPUR DOHS", new java.awt.geom.Point2D.Double(0.80, 0.33)),
            new Node("F", "ECB", new java.awt.geom.Point2D.Double(0.80, 0.81)),
            new Node("G", "MIST", new java.awt.geom.Point2D.Double(0.40, 0.81)),
            new Node("H", "MIRPUR-10", new java.awt.geom.Point2D.Double(0.40, 0.51)),
            new Node("I", "MIRPUR-11", new java.awt.geom.Point2D.Double(0.73, 0.26)),
            new Node("P", "MIRPUR-12", new java.awt.geom.Point2D.Double(0.55, 0.54))
    };

    private final List<Point> screenPositions = new ArrayList<>();
    private Image backgroundImage;
    private Image ambulanceImage;

    private int startNode, destNode;
    private int obsNode1, obsNode2; // Storing obstacle nodes
    private List<Integer> shortestPath;
    private int totalDistance;
    private double progress = 0.0;
    private int currentSegment = 0;
    private javax.swing.Timer animationTimer;

    public AmbulanceRoutingSystem(int start, int dest, int obs1, int obs2) {
        this.startNode = start;
        this.destNode = dest;
        this.obsNode1 = obs1;
        this.obsNode2 = obs2;

        // Load images
        backgroundImage = new ImageIcon("city_map.png.jpg").getImage();
        ambulanceImage = new ImageIcon("ambulance.png").getImage();

        // WINDOW SIZE
        setPreferredSize(new Dimension(1445, 768));

        // Prepare node screen positions
        for (int i = 0; i < nodes.length; i++) screenPositions.add(new Point(0, 0));

        // APPLY OBSTACLE
        // We only modify the matrix if the indices are valid
        if (obs1 >= 0 && obs1 < nodes.length && obs2 >= 0 && obs2 < nodes.length) {
            adjacencyMatrix[obs1][obs2] = 0;
            adjacencyMatrix[obs2][obs1] = 0;
        }

        // GET PATH
        shortestPath = DijkstraAlgorithm.getShortestPath(adjacencyMatrix, startNode, destNode);
        if (shortestPath != null) {
            totalDistance = DijkstraAlgorithm.calculateTotalDistance(adjacencyMatrix, shortestPath);
        } else {
            totalDistance = 0;
        }

        // START ANIMATION
        startAnimation();
    }

    private void startAnimation() {
        animationTimer = new javax.swing.Timer(30, e -> {
            if (shortestPath == null || shortestPath.size() < 2) {
                animationTimer.stop();
                return;
            }
            progress += 0.02;
            if (progress >= 1.0) {
                progress = 0.0;
                currentSegment++;
                if (currentSegment >= shortestPath.size() - 1)
                    animationTimer.stop();
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        updateScreenPositions();

        // Draw the obstacle line
        drawObstacle(g);

        // Draw edges and weights
        drawEdges(g);

        drawShortestPath(g);

        // Draw nodes
        drawNodes(g);

        drawAmbulance(g);

        // Draw info
        drawInfo(g);

        g.dispose();
    }

    // Draws the blocked path as a dashed red line (from previous fix)
    private void drawObstacle(Graphics2D g) {
        if (obsNode1 < 0 || obsNode1 >= nodes.length || obsNode2 < 0 || obsNode2 >= nodes.length || obsNode1 == obsNode2) {
            return;
        }

        Point p1 = screenPositions.get(obsNode1);
        Point p2 = screenPositions.get(obsNode2);

        float dash[] = {10.0f, 5.0f};
        g.setStroke(new BasicStroke(
                5,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
                10.0f,
                dash,
                0.0f
        ));

        g.setColor(Color.RED);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);

        g.setStroke(new BasicStroke(2));
    }


    private void updateScreenPositions() {
        screenPositions.clear();
        int w = getWidth(), h = getHeight();
        for (Node node : nodes) {
            int sx = (int) (node.getNormalizedPosition().x * w);
            int sy = (int) (node.getNormalizedPosition().y * h);
            screenPositions.add(new Point(sx, sy));
        }
    }

    // Draws edges and their weights.

    private void drawEdges(Graphics2D g) {
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = i + 1; j < adjacencyMatrix.length; j++) {
                int weight = adjacencyMatrix[i][j];

                // Only draw if there is a path AND it's not the obstacle (which is 0 now)
                if (weight > 0) {
                    Point p1 = screenPositions.get(i);
                    Point p2 = screenPositions.get(j);

                    // Draw the standard gray line
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Draw the weight (distance)
                    int mx = (p1.x + p2.x) / 2;
                    int my = (p1.y + p2.y) / 2;

                    // Draw a white rectangle background for clarity
                    g.setColor(new Color(255, 255, 255, 180)); // Semi-transparent white
                    g.fillRect(mx - 10, my - 12, 20, 16);

                    // Draw the black weight text
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString(String.valueOf(weight), mx - 5, my + 2);
                    g.setFont(new Font("Arial", Font.BOLD, 14)); // Reset font
                }
            }
        }
    }

    private void drawShortestPath(Graphics2D g) {
        if (shortestPath == null || shortestPath.size() < 2) return;
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(4));
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point p1 = screenPositions.get(shortestPath.get(i));
            Point p2 = screenPositions.get(shortestPath.get(i + 1));
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }


    private void drawNodes(Graphics2D g) {
        for (int i = 0; i < screenPositions.size(); i++) {
            Point p = screenPositions.get(i);

            // Set Node Color
            Color nodeColor;
            if (i == startNode) nodeColor = Color.RED; // Dark Green for Start
            else if (i == destNode) nodeColor = Color.GREEN; // BLUE for Destination
            else nodeColor = Color.YELLOW ;

            // Draw Outline (Black border)
            g.setColor(Color.BLACK);
            g.fillOval(p.x - 11, p.y - 11, 22, 22);

            // Draw Node Fill
            g.setColor(nodeColor);
            g.fillOval(p.x - 10, p.y - 10, 20, 20);

            // Draw Label and Place Name
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(nodes[i].getLabel(), p.x - 5, p.y - 15);

            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString(nodes[i].getPlaceName(), p.x - 20, p.y + 25);
        }
    }

    private void drawAmbulance(Graphics2D g) {
        if (shortestPath == null || shortestPath.size() < 2) return;

        int from = shortestPath.get(currentSegment);
        int to = shortestPath.get(Math.min(currentSegment + 1, shortestPath.size() - 1));

        Point p1 = screenPositions.get(from);
        Point p2 = screenPositions.get(to);

        double x = p1.x + (p2.x - p1.x) * progress;
        double y = p1.y + (p2.y - p1.y) * progress;

        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);

        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);

        g.drawImage(ambulanceImage, -30, -20, 40, 30, this);

        g.setTransform(old);
    }


    private void drawInfo(Graphics2D g) {
        g.setColor(new Color(0, 150, 0, 180)); // Semi-transparent dark green
        g.fillRect(10, 10, 380, 100);

        g.setColor(Color.WHITE); // White text for contrast
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Start: " + nodes[startNode].getLabel() + " (" + nodes[startNode].getPlaceName() + ")", 20, 30);
        g.drawString("Destination: " + nodes[destNode].getLabel() + " (" + nodes[destNode].getPlaceName() + ")", 20, 45);
        g.drawString("Obstacle: " + nodes[obsNode1].getLabel() + " to " + nodes[obsNode2].getLabel(), 20, 60);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        if (shortestPath == null || shortestPath.size() < 2) {
            g.setColor(Color.RED);
            g.drawString("STATUS: NO PATH FOUND!", 20, 78);
            g.drawString("Distance: N/A", 20, 93);
        } else {
            // Show Current Movement Status *** NEW ***
            if (currentSegment < shortestPath.size() - 1) {
                String fromLabel = nodes[shortestPath.get(currentSegment)].getLabel();
                String toLabel = nodes[shortestPath.get(currentSegment + 1)].getLabel();
                g.setColor(Color.YELLOW);
                g.drawString("STATUS: Moving from " + fromLabel + " to " + toLabel + "...", 20, 78);
            } else {
                g.setColor(Color.CYAN);
                g.drawString("STATUS: Destination Reached!", 20, 78);
            }

            g.setColor(Color.WHITE);
            g.drawString("Path: " + shortestPath.toString(), 220, 93);
            g.drawString("Distance: " + totalDistance, 20, 93);
        }
    }
}
