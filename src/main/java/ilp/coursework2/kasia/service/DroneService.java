package ilp.coursework2.kasia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.interfaces.IDroneService;
import ilp.coursework2.kasia.model.BoundingBox;
import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;

/**
 * Service for drone navigation and geometric calculations.
 * Provides functionality for distance calculations, proximity checks,
 * position updates, and point-in-polygon determinations.
 */
@Service
public class DroneService implements IDroneService {
    /** Standard movement distance for the drone (in degrees) */
    public static final double DISTANCE = 0.00015;

    /**
     * Calculates the Euclidean distance between two positions.
     *
     * @param positions a pair of positions to measure
     * @return the Euclidean distance between the two positions
     */
    @Override
    public double calculateDistance(PositionPair positions) {
        Position position1 = positions.position1();
        Position position2 = positions.position2();

        double deltaLng = position2.lng() - position1.lng();
        double deltaLat = position2.lat() - position1.lat();

        return Math.sqrt(deltaLng * deltaLng + deltaLat * deltaLat);
    }

    /**
     * Determines if two positions are close to each other.
     * Positions are considered close if the distance between them is less than
     * 0.00015 degrees.
     *
     * @param positions a pair of positions to compare
     * @return true if the positions are close (distance < 0.00015), false otherwise
     */
    @Override
    public boolean isCloseTo(PositionPair positions) {
        double distance = calculateDistance(positions);
        return distance < DISTANCE;
    }

    /**
     * Calculates the next position of the drone given a starting position and
     * movement angle.
     * The drone moves 0.00015 degrees in the specified direction.
     *
     * @param request contains the starting position and angle
     * @return the new position after movement
     */
    @Override
    public Position calculateNextPosition(NextPositionRequest request) {
        double lng = request.start().lng();
        double lat = request.start().lat();
        double angleRadians = Math.toRadians(request.angle());

        double newLng = lng + (DISTANCE * Math.cos(angleRadians));
        double newLat = lat + (DISTANCE * Math.sin(angleRadians));

        return new Position(newLng, newLat);
    }

    /**
     * Determines if a point is inside a given region (polygon).
     * Uses ray casting algorithm to check if the point is within the polygon
     * boundaries.
     * Points on the polygon boundary are considered inside (as per specification).
     *
     * @param request contains the point to test and the region definition
     * @return true if the point is inside or on the boundary of the region, false
     *         otherwise
     */
    @Override
    public boolean isPointInRegion(RegionRequest request) {
        Region region = request.region();
        Position point = request.position();
        List<Position> polygon = region.vertices();

        // Quick rejection: if point is outside bounding box, it's not in the region
        BoundingBox box = BoundingBox.fromPolygon(polygon);
        if (!box.contains(point))
            return false;

        // Ray casting algorithm: cast a ray from point to infinity (right)
        // Count how many times the ray crosses polygon edges
        // Odd crossings = inside, even crossings = outside

        double pointX = point.lng();
        double pointY = point.lat();
        boolean isInside = false;

        // Check each edge of the polygon
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            Position vertex1 = polygon.get(i);
            Position vertex2 = polygon.get(j);

            if (isPointOnSegment(point, vertex1, vertex2))
                return true;

            if (doesRayIntersectEdge(pointX, pointY, vertex1, vertex2))
                isInside = !isInside;
        }

        return isInside;
    }

    // ==================
    // Helper functions
    // ==================

    /**
     * Checks if a horizontal ray cast from a point to the right intersects a
     * polygon edge.
     *
     * @param pointX  x-coordinate of the point
     * @param pointY  y-coordinate of the point
     * @param vertex1 first vertex of the edge
     * @param vertex2 second vertex of the edge
     * @return true if the ray intersects the edge, false otherwise
     */
    private boolean doesRayIntersectEdge(
            double pointX,
            double pointY,
            Position vertex1,
            Position vertex2) {
        double y1 = vertex1.lat();
        double y2 = vertex2.lat();
        double x1 = vertex1.lng();
        double x2 = vertex2.lng();

        // Edge must straddle the horizontal line through the point
        boolean straddlesHorizontal = (y1 > pointY) != (y2 > pointY);
        if (!straddlesHorizontal) {
            return false;
        }

        // Calculate x-coordinate where edge crosses the horizontal line at pointY
        double intersectionX = (x2 - x1) * (pointY - y1) / (y2 - y1) + x1;

        // Ray extends to the right, so intersection must be to the right of point
        return pointX < intersectionX;
    }

    /**
     * Determines if a point lies exactly on a line segment.
     * Uses cross product to check collinearity and bounding box to check if point
     * is within segment bounds.
     *
     * @param p the point to test
     * @param a first endpoint of the segment
     * @param b second endpoint of the segment
     * @return true if the point lies on the segment, false otherwise
     */
    private boolean isPointOnSegment(Position p, Position a, Position b) {
        // Cross product == 0 then colinear
        double cross = (p.lat() - a.lat()) * (b.lng() - a.lng()) -
                (p.lng() - a.lng()) * (b.lat() - a.lat());

        if (Math.abs(cross) > 0)
            return false;

        // Check bounding box
        BoundingBox segmentBox = BoundingBox.fromSegment(a, b);
        return segmentBox.contains(p);
    }
}
