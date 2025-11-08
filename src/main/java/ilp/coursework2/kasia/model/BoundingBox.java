package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents an axis-aligned bounding box (AABB) in 2D space.
 * Stores minimum and maximum coordinates for X (longitude) and Y (latitude).
 */
public class BoundingBox {

    // tolerance for floating-point comparisons
    // private static final double EPSILON = 1e-10;

    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    /**
     * Private constructor to create a bounding box with specific coordinates.
     *
     * @param minX minimum X coordinate (longitude)
     * @param maxX maximum X coordinate (longitude)
     * @param minY minimum Y coordinate (latitude)
     * @param maxY maximum Y coordinate (latitude)
     */
    private BoundingBox(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Creates a bounding box that encloses a polygon defined by a list of vertices.
     *
     * @param vertices list of positions representing the polygon vertices
     * @return bounding box that contains all vertices
     */
    public static BoundingBox fromPolygon(List<Position> vertices) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Position v : vertices) {
            minX = Math.min(minX, v.lng());
            maxX = Math.max(maxX, v.lng());
            minY = Math.min(minY, v.lat());
            maxY = Math.max(maxY, v.lat());
        }

        return new BoundingBox(minX, maxX, minY, maxY);
    }

    /**
     * Creates a bounding box that encloses a line segment defined by two positions.
     *
     * @param a first endpoint of the segment
     * @param b second endpoint of the segment
     * @return bounding box that contains the segment
     */
    public static BoundingBox fromSegment(Position a, Position b) {
        double minX = Math.min(a.lng(), b.lng());
        double maxX = Math.max(a.lng(), b.lng());
        double minY = Math.min(a.lat(), b.lat());
        double maxY = Math.max(a.lat(), b.lat());
        return new BoundingBox(minX, maxX, minY, maxY);
    }

    // ===========
    // Functions
    // ===========

    /**
     * Checks whether a given position is inside this bounding box.
     *
     * @param p the position to check
     * @return true if the position is inside the bounding box, false otherwise
     */
    public boolean contains(Position p) {
        double x = p.lng();
        double y = p.lat();
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    // ===========
    // Getters
    // ===========

    /** @return minimum X coordinate (longitude) of the bounding box */
    public double minX() {
        return minX;
    }

    /** @return maximum X coordinate (longitude) of the bounding box */
    public double maxX() {
        return maxX;
    }

    /** @return minimum Y coordinate (latitude) of the bounding box */
    public double minY() {
        return minY;
    }

    /** @return maximum Y coordinate (latitude) of the bounding box */
    public double maxY() {
        return maxY;
    }

    /** @return width of the bounding box (maxX - minX) */
    public double width() {
        return maxX - minX;
    }

    /** @return height of the bounding box (maxY - minY) */
    public double height() {
        return maxY - minY;
    }
}
