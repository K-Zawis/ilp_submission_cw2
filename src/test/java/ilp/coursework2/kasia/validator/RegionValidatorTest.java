package ilp.coursework2.kasia.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("Region Validator Tests")
class RegionValidatorTest {

    @Mock
    private IPositionValidator positionValidator;

    @InjectMocks
    private RegionValidator regionValidator;

    @BeforeEach
    void setUp() {
        lenient().when(positionValidator.isValidPosition(any(Position.class))).thenReturn(true);
    }

    // ==================
    // Basic Validation Tests
    // ==================

    @Test
    @DisplayName("Valid region request returns true")
    void validRegionRequest() {
        List<Position> vertices = Arrays.asList(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617),
                new Position(-3.184319, 55.942617),
                new Position(-3.184319, 55.946233),
                new Position(-3.192473, 55.946233));
        Region region = new Region("central", 0, null, vertices);
        RegionRequest request = new RegionRequest(new Position(-3.188, 55.944), region);

        boolean result = regionValidator.isValidRegionRequest(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Null region request returns false")
    void nullRegionRequest() {
        boolean result = regionValidator.isValidRegionRequest(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Region request with null position returns false")
    void nullPosition() {
        when(positionValidator.isValidPosition(null)).thenReturn(false);

        List<Position> vertices = Arrays.asList(
                new Position(-3.192473, 55.946233),
                new Position(-3.192473, 55.942617),
                new Position(-3.184319, 55.942617),
                new Position(-3.184319, 55.946233),
                new Position(-3.192473, 55.946233));
        Region region = new Region("central", 0, null, vertices);
        RegionRequest request = new RegionRequest(null, region);

        boolean result = regionValidator.isValidRegionRequest(request);

        assertFalse(result);
    }

    @Test
    @DisplayName("Region request with null region returns false")
    void nullRegion() {
        RegionRequest request = new RegionRequest(new Position(-3.188, 55.944), null);

        boolean result = regionValidator.isValidRegionRequest(request);

        assertFalse(result);
    }

    // ==================
    // Polygon Closure Tests
    // ==================

    @Test
    @DisplayName("Closed polygon returns true")
    void closedPolygon() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isPolygonClosed(vertices);

        assertTrue(result);
    }

    @Test
    @DisplayName("Open polygon returns false")
    void openPolygon() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0));

        boolean result = regionValidator.isPolygonClosed(vertices);

        assertFalse(result);
    }

    // ==================
    // Vertex Count Tests
    // ==================

    @Test
    @DisplayName("Polygon with less than 4 vertices returns false")
    void tooFewVertices() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(0.0, 1.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertFalse(result);
    }

    @Test
    @DisplayName("Polygon with exactly 4 vertices and closed returns true")
    void minimumValidVertices() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertTrue(result);
    }

    // ==================
    // Collinear Points Tests
    // ==================

    @Test
    @DisplayName("Polygon with collinear points returns false")
    void collinearPoints() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(2.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertFalse(result);
    }

    @Test
    @DisplayName("Polygon without collinear points returns true")
    void noCollinearPoints() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(2.0, 0.0),
                new Position(2.0, 2.0),
                new Position(0.0, 2.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertTrue(result);
    }

    // ==================
    // Self-Intersection Tests
    // ==================

    @Test
    @DisplayName("Self-intersecting polygon returns false")
    void selfIntersectingPolygon() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(2.0, 2.0),
                new Position(2.0, 0.0),
                new Position(0.0, 2.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertFalse(result);
    }

    @Test
    @DisplayName("Simple non-intersecting polygon returns true")
    void nonIntersectingPolygon() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));

        boolean result = regionValidator.isValidPolygon(vertices);

        assertTrue(result);
    }

    // ==================
    // Region Name Tests
    // ==================

    @Test
    @DisplayName("Region with null name returns false")
    void nullRegionName() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));
        Region region = new Region(null, 0, null, vertices);

        boolean result = regionValidator.isRegionValid(region);

        assertFalse(result);
    }

    @Test
    @DisplayName("Region with valid name returns true")
    void validRegionName() {
        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                new Position(1.0, 0.0),
                new Position(1.0, 1.0),
                new Position(0.0, 1.0),
                new Position(0.0, 0.0));
        Region region = new Region("test-region", 0, null, vertices);

        boolean result = regionValidator.isRegionValid(region);

        assertTrue(result);
    }

    // ==================
    // Vertices Validation Tests
    // ==================

    @Test
    @DisplayName("Empty vertices list returns false")
    void emptyVertices() {
        List<Position> vertices = Collections.emptyList();

        boolean result = regionValidator.areVerticesValid(vertices);

        assertFalse(result);
    }

    @Test
    @DisplayName("Vertices with invalid position returns false")
    void verticesWithInvalidPosition() {
        Position invalidPosition = new Position(null, 55.0);
        when(positionValidator.isValidPosition(invalidPosition)).thenReturn(false);

        List<Position> vertices = Arrays.asList(
                new Position(0.0, 0.0),
                invalidPosition,
                new Position(1.0, 1.0));

        boolean result = regionValidator.areVerticesValid(vertices);

        assertFalse(result);
    }
}