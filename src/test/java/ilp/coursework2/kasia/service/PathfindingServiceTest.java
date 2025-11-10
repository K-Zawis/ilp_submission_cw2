package ilp.coursework2.kasia.service;

import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PathfindingServiceTest {

    @Autowired
    private PathfindingService pathfindingService;

    @Autowired
    private IlpService ilpService;

    private List<Region> restrictedAreas;

    @BeforeEach
    void setUp() {
        restrictedAreas = ilpService.getRestrictedAreas();
    }

    @Test
    void testFindPath_ShortDistance_NoObstacles() {
        Position start = new Position(-3.186, 55.944);
        Position goal = new Position(-3.1869, 55.9441);

        List<Position> path = pathfindingService.findPath(start, goal, restrictedAreas, 100);

        assertFalse(path.isEmpty(), "Path should be found");
        assertEquals(start, path.get(0), "Path should start at start position");
        assertTrue(pathfindingService.isAtPosition(path.get(path.size() - 1), goal),
                "Path should end near goal position");
    }

    @Test
    void testFindPath_UnreachableWithLowMoveLimit() {
        Position start = new Position(-3.186, 55.944);
        Position goal = new Position(-3.177, 55.981); // Far away

        List<Position> path = pathfindingService.findPath(start, goal, restrictedAreas, 10);

        assertTrue(path.isEmpty(), "Path should not be found with low move limit");
    }

    @Test
    void testCalculatePathCost() {
        List<Position> path = List.of(
                new Position(-3.186, 55.944),
                new Position(-3.1861, 55.944),
                new Position(-3.1862, 55.944));

        assertEquals(2, pathfindingService.calculatePathCost(path));
    }
}