package ilp.coursework2.kasia.service;

import java.util.*;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;
import ilp.coursework2.kasia.model.enums.Direction;
import ilp.coursework2.kasia.validator.RegionValidator;

/**
 * A* pathfinding service for drone navigation.
 * Implements the A* algorithm with optimizations for obstacle avoidance.
 */
@Service
public class PathfindingService {
    private final DroneService droneService;
    private final RegionValidator regionValidator;

    // Cache for restricted area edges (avoid recomputing)
    private List<List<Position>> cachedEdges = null;
    private List<Region> cachedRegions = null;

    public PathfindingService(DroneService droneService, RegionValidator regionValidator) {
        this.droneService = droneService;
        this.regionValidator = regionValidator;
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
            // Use exact position comparison
            return Double.compare(position.lng(), node.position.lng()) == 0 &&
                    Double.compare(position.lat(), node.position.lat()) == 0;
        }

        @Override
        public int hashCode() {
            // Better hash function
            long lngBits = Double.doubleToLongBits(position.lng());
            long latBits = Double.doubleToLongBits(position.lat());
            return (int) (lngBits ^ (lngBits >>> 32) ^ latBits ^ (latBits >>> 32));
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

        // Increased safety limits
        long startTime = System.currentTimeMillis();
        long timeoutMs = 15000; // 15 second timeout (increased)
        int maxNodesExplored = 10000; // 10k nodes (increased)

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
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return List.of();
            }

            // Check node limit to prevent memory exhaustion
            if (closedSet.size() >= maxNodesExplored) {
                return List.of();
            }

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

                // Check if destination is in restricted area
                if (isInRestrictedArea(nextPos, restrictedAreas)) {
                    continue;
                }

                // Check if the path crosses through restricted area
                if (pathCrossesRestrictedArea(current.position, nextPos, restrictedAreas)) {
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

                // Use weighted heuristic for better pathfinding
                hScore = heuristic(nextPos, goal) * 1.1; // Slight weight towards goal
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
     * Check if the line segment from p1 to p2 crosses through any restricted area.
     * Uses cached edges for better performance.
     */
    private boolean pathCrossesRestrictedArea(Position p1, Position p2, List<Region> regions) {
        // Build edge cache if needed
        if (cachedEdges == null || cachedRegions != regions) {
            buildEdgeCache(regions);
        }

        // Check intersection with cached edges
        for (int regionIdx = 0; regionIdx < regions.size(); regionIdx++) {
            Region region = regions.get(regionIdx);
            List<Position> edges = cachedEdges.get(regionIdx);

            for (int i = 0; i < edges.size() - 1; i++) {
                Position v1 = edges.get(i);
                Position v2 = edges.get(i + 1);

                if (regionValidator.doSegmentsIntersect(p1, p2, v1, v2)) {
                    return true;
                }
            }

            // Check if either endpoint is inside the region
            RegionRequest req1 = new RegionRequest(p1, region);
            RegionRequest req2 = new RegionRequest(p2, region);
            if (droneService.isPointInRegion(req1) || droneService.isPointInRegion(req2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build cache of region edges for faster intersection testing.
     */
    private void buildEdgeCache(List<Region> regions) {
        cachedEdges = new ArrayList<>();
        cachedRegions = regions;

        for (Region region : regions) {
            cachedEdges.add(new ArrayList<>(region.vertices()));
        }
    }

    /**
     * Heuristic function for A*.
     * Uses Euclidean distance as an admissible heuristic.
     */
    private double heuristic(Position from, Position to) {
        PositionPair pair = new PositionPair(from, to);
        double distance = droneService.calculateDistance(pair);
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
        return Math.max(0, path.size() - 1);
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