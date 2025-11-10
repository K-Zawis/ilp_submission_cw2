package ilp.coursework2.kasia.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;

@DisplayName("Drone Service Tests")
class DroneServiceTest {

    private DroneService droneService;

    @BeforeEach
    void setUp() {
        droneService = new DroneService();
    }

    // ==================
    // Distance Calculation Tests
    // ==================

    @Test
    @DisplayName("Calculate distance returns correct Euclidean distance")
    void calculateDistance() {
        PositionPair positions = new PositionPair(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617));

        double distance = droneService.calculateDistance(positions);

        assertEquals(0.003616, distance, 0.000001);
    }

    @Test
    @DisplayName("Calculate distance for same position returns zero")
    void calculateDistanceSamePosition() {
        PositionPair positions = new PositionPair(
                new Position(0.0, 0.0),
                new Position(0.0, 0.0));

        double distance = droneService.calculateDistance(positions);

        assertEquals(0.0, distance, 0.000001);
    }

    @Test
    @DisplayName("Calculate distance returns positive value")
    void calculateDistancePositive() {
        PositionPair positions = new PositionPair(
                new Position(1.0, 1.0),
                new Position(2.0, 2.0));

        double distance = droneService.calculateDistance(positions);

        assertTrue(distance > 0);
    }

    // ==================
    // IsCloseTo Tests
    // ==================

    @Test
    @DisplayName("IsCloseTo returns true when distance is less than threshold")
    void isCloseToReturnsTrue() {
        PositionPair positions = new PositionPair(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.946234));

        boolean isClose = droneService.isCloseTo(positions);

        assertTrue(isClose);
    }

    @Test
    @DisplayName("IsCloseTo returns false when distance exceeds threshold")
    void isCloseToReturnsFalse() {
        PositionPair positions = new PositionPair(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617));

        boolean isClose = droneService.isCloseTo(positions);

        assertFalse(isClose);
    }

    @Test
    @DisplayName("IsCloseTo returns true when distance equals threshold minus epsilon")
    void isCloseToEdgeCaseBelowThreshold() {
        // Distance just under 0.00015
        PositionPair positions = new PositionPair(
                new Position(0.0, 0.0),
                new Position(0.00014, 0.0));

        boolean isClose = droneService.isCloseTo(positions);

        assertTrue(isClose);
    }

    @Test
    @DisplayName("IsCloseTo returns false when distance equals threshold")
    void isCloseToEdgeCaseAtThreshold() {
        // Distance exactly at 0.00015
        PositionPair positions = new PositionPair(
                new Position(0.0, 0.0),
                new Position(0.00015, 0.0));

        boolean isClose = droneService.isCloseTo(positions);

        assertFalse(isClose);
    }

    // ==================
    // Next Position Tests
    // ==================

    @Test
    @DisplayName("Calculate next position moves drone east when angle is 0°")
    void nextPositionMovesEast() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                0.0);

        Position next = droneService.calculateNextPosition(request);

        assertEquals(0.00015, next.lng(), 0.0000001);
        assertEquals(0.0, next.lat(), 0.0000001);
    }

    @Test
    @DisplayName("Calculate next position moves drone north when angle is 90°")
    void nextPositionMovesNorth() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                90.0);

        Position next = droneService.calculateNextPosition(request);

        assertEquals(0.0, next.lng(), 0.0000001);
        assertEquals(0.00015, next.lat(), 0.0000001);
    }

    @Test
    @DisplayName("Calculate next position moves drone west when angle is 180°")
    void nextPositionMovesWest() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                180.0);

        Position next = droneService.calculateNextPosition(request);

        assertEquals(-0.00015, next.lng(), 0.0000001);
        assertEquals(0.0, next.lat(), 0.0000001);
    }

    @Test
    @DisplayName("Calculate next position moves drone south when angle is 270°")
    void nextPositionMovesSouth() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                270.0);

        Position next = droneService.calculateNextPosition(request);

        assertEquals(0.0, next.lng(), 0.0000001);
        assertEquals(-0.00015, next.lat(), 0.0000001);
    }

    @Test
    @DisplayName("Calculate next position moves drone northeast when angle is 45°")
    void nextPositionMovesNortheast() {
        NextPositionRequest request = new NextPositionRequest(
                new Position(0.0, 0.0),
                45.0);

        Position next = droneService.calculateNextPosition(request);

        double expected = 0.00015 / Math.sqrt(2);
        assertEquals(expected, next.lng(), 0.0000001);
        assertEquals(expected, next.lat(), 0.0000001);
    }

    // ==================
    // Point In Region Tests
    // ==================

    @Test
    @DisplayName("Point inside region returns true")
    void pointInsideRegion() {
        List<Position> vertices = Arrays.asList(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617),
                new Position(-3.184319, 55.942617),
                new Position(-3.184319, 55.946233),
                new Position(-3.192473, 55.946233));
        Region region = new Region("central", 0, null, vertices);
        RegionRequest request = new RegionRequest(new Position(-3.188, 55.944), region);

        boolean result = droneService.isPointInRegion(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Point outside region returns false")
    void pointOutsideRegion() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));
        Region region = new Region("square", 0, null, vertices);
        RegionRequest request = new RegionRequest(new Position(2.0, 2.0), region);

        boolean result = droneService.isPointInRegion(request);

        assertFalse(result);
    }

    @Test
    @DisplayName("Point on boundary of region returns true")
    void pointOnBoundary() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));
        Region region = new Region("square", 0, null, vertices);
        RegionRequest request = new RegionRequest(new Position(0.5, 0.0), region);

        boolean result = droneService.isPointInRegion(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Point at vertex of region returns true")
    void pointAtVertex() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));
        Region region = new Region("square", 0, null, vertices);
        RegionRequest request = new RegionRequest(new Position(0.0, 0.0), region);

        boolean result = droneService.isPointInRegion(request);

        assertTrue(result);
    }
}