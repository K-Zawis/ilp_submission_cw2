package ilp.coursework2.kasia.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.interfaces.IRegionValidator;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;

/**
 * Validator for region-based requests and polygon geometry.
 * Performs comprehensive validation including polygon closure,
 * self-intersections,
 * and collinear point detection.
 */
@Component
public class RegionValidator implements IRegionValidator {

    private static final double EPSILON = 1e-10;

    private final IPositionValidator positionValidator;

    public RegionValidator(IPositionValidator positionValidator) {
        this.positionValidator = positionValidator;
    }

    /**
     * Validates a complete region request including position and region geometry.
     *
     * @param request the region request to validate
     * @return true if the request is valid, false otherwise
     */
    @Override
    public boolean isValidRegionRequest(RegionRequest request) {
        return request != null
                && positionValidator.isValidPosition(request.position())
                && isRegionValid(request.region());
    }

    /**
     * Validates a region including its name, vertices, and polygon geometry.
     *
     * @param region the region to validate
     * @return true if the region is valid, false otherwise
     */
    @Override
    public boolean isRegionValid(Region region) {
        return region != null
                && region.name() != null
                && region.vertices() != null
                && areVerticesValid(region.vertices())
                && isValidPolygon(region.vertices());
    }

    /**
     * Checks if all vertices in the list are valid positions.
     *
     * @param vertices the list of vertices to validate
     * @return true if all vertices are valid, false otherwise
     */
    @Override
    public boolean areVerticesValid(List<Position> vertices) {
        if (vertices.isEmpty())
            return false;

        for (Position vertex : vertices) {
            if (!positionValidator.isValidPosition(vertex)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates polygon geometry including closure, self-intersections, and
     * collinear points.
     * A valid polygon must:
     * - Have at least 4 vertices (3 unique points + closing point)
     * - Be closed (first and last vertices match)
     * - Have no self-intersecting edges
     * - Have no three consecutive collinear points (degenerate edges)
     *
     * @param points the vertices defining the polygon
     * @return true if the polygon is valid, false otherwise
     */
    @Override
    public boolean isValidPolygon(List<Position> points) {
        if (points.size() < 4)
            return false;

        if (!isPolygonClosed(points))
            return false;

        if (hasSelfIntersections(points))
            return false;

        if (hasColinearPoints(points))
            return false;

        return true;
    }

    /**
     * Checks if a polygon is closed (first vertex equals last vertex).
     *
     * @param points the vertices of the polygon
     * @return true if the polygon is closed, false otherwise
     */
    @Override
    public boolean isPolygonClosed(List<Position> points) {
        Position first = points.get(0);
        Position last = points.get(points.size() - 1);
        return first.lng().equals(last.lng()) && first.lat().equals(last.lat());
    }

    // ==================
    // Helper functions
    // ==================

    /**
     * Detects if the polygon has any self-intersecting edges.
     * Checks all pairs of non-adjacent edges for intersection.
     *
     * @param points the vertices of the polygon
     * @return true if self-intersections are found, false otherwise
     */
    private boolean hasSelfIntersections(List<Position> points) {
        int n = points.size();

        // Check each pair of non-adjacent edges for intersection
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 2; j < n - 1; j++) {
                // Skip checking adjacent edges and closing edge with first edge
                if (i == 0 && j == n - 2) {
                    continue;
                }

                if (doSegmentsIntersect(
                        points.get(i), points.get(i + 1),
                        points.get(j), points.get(j + 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if two line segments intersect using cross product method.
     *
     * @param p1 first endpoint of segment 1
     * @param p2 second endpoint of segment 1
     * @param p3 first endpoint of segment 2
     * @param p4 second endpoint of segment 2
     * @return true if the segments intersect, false otherwise
     */
    private boolean doSegmentsIntersect(Position p1, Position p2, Position p3, Position p4) {
        // Calculate directions using cross product
        double d1 = crossProduct(p3, p4, p1);
        double d2 = crossProduct(p3, p4, p2);
        double d3 = crossProduct(p1, p2, p3);
        double d4 = crossProduct(p1, p2, p4);

        // Segments intersect if they straddle each other
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        return false;
    }

    /**
     * Calculates the cross product to determine which side of a line a point is on.
     *
     * @param p1 first point defining the line
     * @param p2 second point defining the line
     * @param p3 point to test
     * @return positive if p3 is left of the line, negative if right, zero if on the
     *         line
     */
    private double crossProduct(Position p1, Position p2, Position p3) {
        // Cross product to determine which side of line p3 is on
        return (p3.lng() - p1.lng()) * (p2.lat() - p1.lat()) -
                (p2.lng() - p1.lng()) * (p3.lat() - p1.lat());
    }

    /**
     * Checks if the polygon contains any three consecutive collinear points.
     * Collinear points create degenerate edges and are not valid for a polygon.
     *
     * @param points the vertices of the polygon
     * @return true if collinear points are found, false otherwise
     */
    private boolean hasColinearPoints(List<Position> points) {
        // Check every triplet of consecutive points (excluding the duplicate closing
        // vertex)
        for (int i = 0; i < points.size() - 2; i++) {
            if (areThreePointsColinear(points.get(i), points.get(i + 1), points.get(i + 2))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if three points are collinear using the cross product method.
     * Points are collinear if the area of the triangle they form is effectively
     * zero.
     *
     * @param a first point
     * @param b second point
     * @param c third point
     * @return true if the points are collinear, false otherwise
     */
    private boolean areThreePointsColinear(Position a, Position b, Position c) {

        double dx1 = b.lng() - a.lng(); // Vector AB (x component)
        double dy1 = c.lat() - a.lat(); // Vector AC (y component)

        double dx2 = c.lng() - a.lng(); // Vector AC (x component)
        double dy2 = b.lat() - a.lat(); // Vector AB (y component)

        // Cross product magnitude = 2 * triangle area
        double crossProduct = (dx1 * dy1) - (dx2 * dy2);
        double area = Math.abs(crossProduct);

        // If area is effectively zero, points are collinear
        return area < EPSILON;
    }

}
