package com.ambulance.routing;

import java.awt.geom.Point2D; //x,y cordination hold KOORBE

public class Node {
    private String label;// node er name
    private String placeName;// node er jaigar name
    private Point2D.Double normalizedPosition;//node er position in graph

    public Node(String label, String placeName, Point2D.Double normalizedPosition) {
        this.label = label;
        this.placeName = placeName;
        this.normalizedPosition = normalizedPosition;
    }

    public String getLabel() {
        return label;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Point2D.Double getNormalizedPosition() {
        return normalizedPosition;
    }
}
