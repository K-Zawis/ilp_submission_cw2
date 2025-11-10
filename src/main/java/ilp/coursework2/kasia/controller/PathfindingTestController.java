package ilp.coursework2.kasia.controller;

import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.service.IlpService;
import ilp.coursework2.kasia.service.PathfindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Test endpoints for pathfinding debugging.
 * These endpoints help verify the A* algorithm is working correctly.
 */
@RestController
@RequestMapping("/api/test")
public class PathfindingTestController {

    private final PathfindingService pathfindingService;
    private final IlpService ilpService;

    public PathfindingTestController(PathfindingService pathfindingService, IlpService ilpService) {
        this.pathfindingService = pathfindingService;
        this.ilpService = ilpService;
    }

    /**
     * Test pathfinding between two points.
     * 
     * Example:
     * GET
     * http://localhost:8080/api/test/path?fromLng=-3.186&fromLat=55.944&toLng=-3.177&toLat=55.981
     */
    @GetMapping("/path")
    public ResponseEntity<Map<String, Object>> testPath(
            @RequestParam double fromLng,
            @RequestParam double fromLat,
            @RequestParam double toLng,
            @RequestParam double toLat,
            @RequestParam(required = false) Integer maxMoves) {

        Position start = new Position(fromLng, fromLat);
        Position goal = new Position(toLng, toLat);

        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        List<Position> path = pathfindingService.findPath(
                start,
                goal,
                restrictedAreas,
                maxMoves != null ? maxMoves : 2000);

        boolean pathFound = !path.isEmpty();

        return ResponseEntity.ok(Map.of(
                "start", start,
                "goal", goal,
                "pathFound", pathFound,
                "pathLength", path.size(),
                "moveCost", pathfindingService.calculatePathCost(path),
                "restrictedAreasCount", restrictedAreas.size(),
                "message", pathFound
                        ? "Path found with " + path.size() + " positions"
                        : "No path found - might be blocked or unreachable"));
    }

    /**
     * Test with detailed path output (careful - can be very long!)
     * 
     * Example:
     * GET
     * http://localhost:8080/api/test/path-detailed?fromLng=-3.186&fromLat=55.944&toLng=-3.1869&toLat=55.9441&maxMoves=50
     */
    @GetMapping("/path-detailed")
    public ResponseEntity<Map<String, Object>> testPathDetailed(
            @RequestParam double fromLng,
            @RequestParam double fromLat,
            @RequestParam double toLng,
            @RequestParam double toLat,
            @RequestParam(defaultValue = "100") Integer maxMoves) {

        Position start = new Position(fromLng, fromLat);
        Position goal = new Position(toLng, toLat);

        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        List<Position> path = pathfindingService.findPath(
                start,
                goal,
                restrictedAreas,
                maxMoves);

        return ResponseEntity.ok(Map.of(
                "start", start,
                "goal", goal,
                "pathFound", !path.isEmpty(),
                "pathLength", path.size(),
                "moveCost", pathfindingService.calculatePathCost(path),
                "fullPath", path));
    }

    /**
     * Test pathfinding between service points.
     * 
     * Example:
     * GET http://localhost:8080/api/test/service-points-path
     */
    @GetMapping("/service-points-path")
    public ResponseEntity<Map<String, Object>> testServicePointsPath() {
        var servicePoints = ilpService.getServicePoints();

        if (servicePoints.size() < 2) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Need at least 2 service points to test"));
        }

        Position start = servicePoints.get(0).location();
        Position goal = servicePoints.get(1).location();

        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        List<Position> path = pathfindingService.findPath(
                start,
                goal,
                restrictedAreas,
                2000);

        return ResponseEntity.ok(Map.of(
                "servicePoint1", servicePoints.get(0).name(),
                "servicePoint2", servicePoints.get(1).name(),
                "start", start,
                "goal", goal,
                "pathFound", !path.isEmpty(),
                "pathLength", path.size(),
                "moveCost", pathfindingService.calculatePathCost(path)));
    }
}