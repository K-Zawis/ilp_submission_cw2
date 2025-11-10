package ilp.coursework2.kasia.service;

import java.util.*;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;
import ilp.coursework2.kasia.model.enums.Direction;

/**
 * A* pathfinding service for drone navigation.
 * Implements the A* algorithm as taught in ILP Lecture 6.
 */
@Service
public class PathfindingService {
    private final DroneService droneService;

    public PathfindingService(DroneService droneService) {
        this.droneService = droneService;
    }

    /**
     * Node class for A* algorithm.
     * Represents a position in the search space with costs.
     */
    private static class Node implements Comparable<Node> {
        Position position;
        Node parent;
        Direction directionFromParent;
        double gScore; // Exact cost from start
        double fScore; // gScore + heuristic

        Node(Position position, Node parent, Direction direction, double gScore, double hScore) {
            this.position = position;
            this.parent = parent;
            this.directionFromParent = direction;
            this.gScore = gScore;
            this.fScore = gScore + hScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Node node))
                return false;
            return positionsEqual(this.position, node.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    Math.round(position.lng() / DroneService.DISTANCE),
                    Math.round(position.lat() / DroneService.DISTANCE));
        }
    }

    // ==================
    // Actual Service Functions
    // ==================

    /**
     * Check if two positions are effectively equal (within tolerance).
     * Uses Euclidean distance to match spec's "circle with 0.00015° radius"
     * requirement.
     */
    private static boolean positionsEqual(Position p1, Position p2) {
        double deltaLng = p1.lng() - p2.lng();
        double deltaLat = p1.lat() - p2.lat();
        double distance = Math.sqrt(deltaLng * deltaLng + deltaLat * deltaLat);
        return distance < DroneService.DISTANCE; // strictly less than 0.00015
    }

    /**
     * Find shortest path from start to goal using A* algorithm.
     * 
     * @param start           starting position
     * @param goal            goal position
     * @param restrictedAreas no-fly zones to avoid
     * @param maxMoves        maximum number of moves allowed (null for unlimited)
     * @return list of positions representing the path (including start and goal),
     *         or empty list if no path found
     */
    public List<Position> findPath(
            Position start,
            Position goal,
            List<Region> restrictedAreas,
            Integer maxMoves) {

        // Priority queue (min heap) for nodes to explore
        PriorityQueue<Node> openSet = new PriorityQueue<>();

        // Set of explored positions
        Set<Node> closedSet = new HashSet<>();

        // Track best gScore for each position
        Map<Position, Double> gScores = new HashMap<>();

        // Initialize start node
        double hScore = heuristic(start, goal);
        Node startNode = new Node(start, null, null, 0, hScore);
        openSet.add(startNode);
        gScores.put(start, 0.0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // Goal reached!
            if (positionsEqual(current.position, goal)) {
                return reconstructPath(current);
            }

            // Skip if already explored
            if (closedSet.contains(current)) {
                continue;
            }

            // Mark as explored
            closedSet.add(current);

            // Check move limit
            if (maxMoves != null && getPathLength(current) >= maxMoves) {
                continue; // Can't explore further from this node
            }

            // Explore all 16 directions
            for (Direction direction : Direction.values()) {
                NextPositionRequest moveRequest = new NextPositionRequest(
                        current.position,
                        direction.getAngle());
                Position nextPos = droneService.calculateNextPosition(moveRequest);

                // Check if in restricted area
                if (isInRestrictedArea(nextPos, restrictedAreas)) {
                    continue;
                }

                // Calculate new gScore (cost is 1 per move)
                double tentativeGScore = current.gScore + 1.0;

                // Check if this path to nextPos is better
                Double existingGScore = gScores.get(nextPos);
                if (existingGScore != null && tentativeGScore >= existingGScore) {
                    continue; // Not a better path
                }

                // This is the best path to nextPos so far
                gScores.put(nextPos, tentativeGScore);

                hScore = heuristic(nextPos, goal);
                Node neighbor = new Node(nextPos, current, direction, tentativeGScore, hScore);
                openSet.add(neighbor);
            }
        }

        // No path found
        return List.of();
    }

    /**
     * Check if a position is inside any restricted region.
     */
    private boolean isInRestrictedArea(Position position, List<Region> regions) {
        for (Region region : regions) {
            RegionRequest request = new RegionRequest(position, region);
            if (droneService.isPointInRegion(request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Heuristic function for A*.
     * Uses Euclidean distance as an admissible heuristic.
     */
    private double heuristic(Position from, Position to) {
        // Use your DroneService to calculate distance
        PositionPair pair = new PositionPair(from, to);
        double distance = droneService.calculateDistance(pair);

        // Convert to number of moves (each move is MOVE_DISTANCE degrees)
        return distance / DroneService.DISTANCE;
    }

    /**
     * Reconstruct the path from start to goal by following parent pointers.
     */
    private List<Position> reconstructPath(Node goalNode) {
        List<Position> path = new ArrayList<>();
        Node current = goalNode;

        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Get the length of the path from start to this node.
     */
    private int getPathLength(Node node) {
        int length = 0;
        Node current = node;
        while (current.parent != null) {
            length++;
            current = current.parent;
        }
        return length;
    }

    /**
     * Calculate the total cost (number of moves) for a path.
     */
    public int calculatePathCost(List<Position> path) {
        return Math.max(0, path.size() - 1); // Subtract 1 because path includes start
    }

    /**
     * Check if a position is close to the target.
     * Used for hover detection (delivery point reached).
     */
    public boolean isAtPosition(Position current, Position target) {
        PositionPair pair = new PositionPair(current, target);
        return droneService.isCloseTo(pair);
    }
}
