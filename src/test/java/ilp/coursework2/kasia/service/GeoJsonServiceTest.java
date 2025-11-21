package ilp.coursework2.kasia.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ilp.coursework2.kasia.model.*;

@DisplayName("GeoJson Service Tests")
class GeoJsonServiceTest {

    private GeoJsonService service;

    @BeforeEach
    void setUp() {
        service = new GeoJsonService();
    }

    // ==================
    // convertDronePathToGeoJson Tests
    // ==================

    @Test
    @DisplayName("Convert simple drone path to GeoJSON")
    void convertDronePathToGeoJson_SimplePath_ReturnsValidGeoJson() throws JSONException {
        // Create a simple flight path
        List<Position> flightPath = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946),
                new Position(-3.188, 55.947));

        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        String result = service.convertDronePathToGeoJson(dronePath);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        JSONObject geoJson = new JSONObject(result);
        assertEquals("LineString", geoJson.getString("type"));

        JSONArray coordinates = geoJson.getJSONArray("coordinates");
        assertEquals(3, coordinates.length());

        // Check first coordinate
        JSONArray firstCoord = coordinates.getJSONArray(0);
        assertEquals(-3.186, firstCoord.getDouble(0), 0.001);
        assertEquals(55.945, firstCoord.getDouble(1), 0.001);
    }

    @Test
    @DisplayName("Convert drone path with multiple deliveries merges paths")
    void convertDronePathToGeoJson_MultipleDeliveries_MergesPaths() throws JSONException {
        // First delivery: A -> B -> C
        List<Position> flightPath1 = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946),
                new Position(-3.188, 55.947));

        // Second delivery: C -> D (C is duplicate, should be removed)
        List<Position> flightPath2 = List.of(
                new Position(-3.188, 55.947),
                new Position(-3.189, 55.948));

        Delivery delivery1 = new Delivery(1, flightPath1);
        Delivery delivery2 = new Delivery(2, flightPath2);
        DronePath dronePath = new DronePath("1", List.of(delivery1, delivery2));

        String result = service.convertDronePathToGeoJson(dronePath);

        JSONObject geoJson = new JSONObject(result);
        JSONArray coordinates = geoJson.getJSONArray("coordinates");

        // Should have 4 coordinates (3 + 2 - 1 duplicate)
        assertEquals(4, coordinates.length());
    }

    @Test
    @DisplayName("Convert null drone path returns empty object")
    void convertDronePathToGeoJson_Null_ReturnsEmpty() {
        String result = service.convertDronePathToGeoJson(null);

        assertEquals("{}", result);
    }

    @Test
    @DisplayName("Convert drone path with empty deliveries returns empty object")
    void convertDronePathToGeoJson_EmptyDeliveries_ReturnsEmpty() {
        DronePath dronePath = new DronePath("1", List.of());

        String result = service.convertDronePathToGeoJson(dronePath);

        assertEquals("{}", result);
    }

    // ==================
    // convertDronePathWithRestrictedAreasToGeoJson Tests
    // ==================

    @Test
    @DisplayName("Convert drone path with restricted areas to FeatureCollection")
    void convertWithRestrictedAreas_ValidData_ReturnsFeatureCollection() throws JSONException {
        List<Position> flightPath = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));

        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        // Create a restricted area
        List<Position> vertices = List.of(
                new Position(-3.190, 55.940),
                new Position(-3.190, 55.950),
                new Position(-3.180, 55.950),
                new Position(-3.180, 55.940));

        Limits limits = new Limits(100, 200);
        Region restrictedArea = new Region("restricted-zone", 1, limits, vertices);

        String result = service.convertDronePathWithRestrictedAreasToGeoJson(
                dronePath,
                List.of(restrictedArea));

        assertNotNull(result);

        JSONObject featureCollection = new JSONObject(result);
        assertEquals("FeatureCollection", featureCollection.getString("type"));

        JSONArray features = featureCollection.getJSONArray("features");
        // Should have 2 features: 1 flight path + 1 restricted area
        assertEquals(2, features.length());

        // Check first feature (flight path)
        JSONObject pathFeature = features.getJSONObject(0);
        assertEquals("Feature", pathFeature.getString("type"));
        assertEquals("LineString", pathFeature.getJSONObject("geometry").getString("type"));

        // Check second feature (restricted area)
        JSONObject restrictedFeature = features.getJSONObject(1);
        assertEquals("Feature", restrictedFeature.getString("type"));
        assertEquals("Polygon", restrictedFeature.getJSONObject("geometry").getString("type"));
    }

    @Test
    @DisplayName("No-fly zone has red color")
    void convertWithRestrictedAreas_NoFlyZone_HasRedColor() throws JSONException {
        List<Position> flightPath = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));

        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        List<Position> vertices = List.of(
                new Position(-3.190, 55.940),
                new Position(-3.190, 55.950),
                new Position(-3.180, 55.950));

        // No-fly zone has limits: lower=0, upper=-1
        Limits noFlyLimits = new Limits(0, -1);
        Region noFlyZone = new Region("no-fly-zone", 1, noFlyLimits, vertices);

        String result = service.convertDronePathWithRestrictedAreasToGeoJson(
                dronePath,
                List.of(noFlyZone));

        JSONObject featureCollection = new JSONObject(result);
        JSONArray features = featureCollection.getJSONArray("features");
        JSONObject restrictedFeature = features.getJSONObject(1);

        JSONObject properties = restrictedFeature.getJSONObject("properties");
        assertEquals("#ff0000", properties.getString("fill"));
    }

    @Test
    @DisplayName("Restricted zone has yellow color")
    void convertWithRestrictedAreas_RestrictedZone_HasYellowColor() throws JSONException {
        List<Position> flightPath = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));

        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        List<Position> vertices = List.of(
                new Position(-3.190, 55.940),
                new Position(-3.190, 55.950),
                new Position(-3.180, 55.950));

        // Regular restricted zone
        Limits limits = new Limits(100, 200);
        Region restrictedZone = new Region("restricted-zone", 1, limits, vertices);

        String result = service.convertDronePathWithRestrictedAreasToGeoJson(
                dronePath,
                List.of(restrictedZone));

        JSONObject featureCollection = new JSONObject(result);
        JSONArray features = featureCollection.getJSONArray("features");
        JSONObject restrictedFeature = features.getJSONObject(1);

        JSONObject properties = restrictedFeature.getJSONObject("properties");
        assertEquals("#ffff00", properties.getString("fill"));
    }

    @Test
    @DisplayName("Polygon is properly closed in GeoJSON")
    void convertWithRestrictedAreas_Polygon_IsClosedProperly() throws JSONException {
        List<Position> flightPath = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));

        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        List<Position> vertices = List.of(
                new Position(-3.190, 55.940),
                new Position(-3.190, 55.950),
                new Position(-3.180, 55.950));

        Limits limits = new Limits(100, 200);
        Region restrictedArea = new Region("test", 1, limits, vertices);

        String result = service.convertDronePathWithRestrictedAreasToGeoJson(
                dronePath,
                List.of(restrictedArea));

        JSONObject featureCollection = new JSONObject(result);
        JSONArray features = featureCollection.getJSONArray("features");
        JSONObject restrictedFeature = features.getJSONObject(1);

        JSONArray coordinates = restrictedFeature.getJSONObject("geometry").getJSONArray("coordinates");
        JSONArray ring = coordinates.getJSONArray(0);

        // Ring should have 4 points (3 vertices + closing point)
        assertEquals(4, ring.length());

        // First and last point should be the same
        JSONArray firstPoint = ring.getJSONArray(0);
        JSONArray lastPoint = ring.getJSONArray(3);

        assertEquals(firstPoint.getDouble(0), lastPoint.getDouble(0), 0.0001);
        assertEquals(firstPoint.getDouble(1), lastPoint.getDouble(1), 0.0001);
    }

    // ==================
    // convertMultipleDronePathsToGeoJson Tests
    // ==================

    @Test
    @DisplayName("Convert multiple drone paths with different colors")
    void convertMultipleDronePaths_MultipleDrones_DifferentColors() throws JSONException {
        // Drone 1 path
        List<Position> path1 = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));
        Delivery delivery1 = new Delivery(1, path1);
        DronePath dronePath1 = new DronePath("1", List.of(delivery1));

        // Drone 2 path
        List<Position> path2 = List.of(
                new Position(-3.188, 55.947),
                new Position(-3.189, 55.948));
        Delivery delivery2 = new Delivery(2, path2);
        DronePath dronePath2 = new DronePath("2", List.of(delivery2));

        String result = service.convertMultipleDronePathsToGeoJson(
                List.of(dronePath1, dronePath2),
                List.of());

        JSONObject featureCollection = new JSONObject(result);
        assertEquals("FeatureCollection", featureCollection.getString("type"));

        JSONArray features = featureCollection.getJSONArray("features");
        assertEquals(2, features.length());

        // Check that paths have different names
        JSONObject feature1 = features.getJSONObject(0);
        JSONObject feature2 = features.getJSONObject(1);

        String name1 = feature1.getJSONObject("properties").getString("name");
        String name2 = feature2.getJSONObject("properties").getString("name");

        assertEquals("Drone 1", name1);
        assertEquals("Drone 2", name2);
    }

    @Test
    @DisplayName("Multiple drone paths with restricted areas includes all features")
    void convertMultipleDronePaths_WithRestrictedAreas_AllFeatures() throws JSONException {
        List<Position> path1 = List.of(
                new Position(-3.186, 55.945),
                new Position(-3.187, 55.946));
        Delivery delivery1 = new Delivery(1, path1);
        DronePath dronePath1 = new DronePath("1", List.of(delivery1));

        List<Position> vertices = List.of(
                new Position(-3.190, 55.940),
                new Position(-3.190, 55.950),
                new Position(-3.180, 55.950));
        Limits limits = new Limits(0, -1);
        Region noFlyZone = new Region("no-fly", 1, limits, vertices);

        String result = service.convertMultipleDronePathsToGeoJson(
                List.of(dronePath1),
                List.of(noFlyZone));

        JSONObject featureCollection = new JSONObject(result);
        JSONArray features = featureCollection.getJSONArray("features");

        // Should have 2 features: 1 drone path + 1 restricted area
        assertEquals(2, features.length());
    }

    @Test
    @DisplayName("Empty drone paths returns empty object")
    void convertMultipleDronePaths_EmptyList_ReturnsEmpty() {
        String result = service.convertMultipleDronePathsToGeoJson(List.of(), List.of());

        assertEquals("{}", result);
    }

    @Test
    @DisplayName("Null drone paths returns empty object")
    void convertMultipleDronePaths_Null_ReturnsEmpty() {
        String result = service.convertMultipleDronePathsToGeoJson(null, List.of());

        assertEquals("{}", result);
    }
}
