package ilp.coursework2.kasia.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.Delivery;
import ilp.coursework2.kasia.model.DronePath;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;

/**
 * Service for converting flight paths to GeoJSON format.
 * Handles conversion of drone paths to standard GeoJSON LineString format
 * for visualization in mapping tools like geojson.io.
 */
@Service
public class GeoJsonService {

    /**
     * Converts a drone path to a GeoJSON LineString.
     * Merges all delivery segments into a single continuous path.
     * 
     * @param dronePath the complete drone path with deliveries
     * @return GeoJSON string representation
     */
    public String convertDronePathToGeoJson(DronePath dronePath) {
        if (dronePath == null || dronePath.deliveries().isEmpty()) {
            return "{}";
        }

        // Merge all delivery flight paths into one continuous path
        List<Position> completePath = mergePaths(dronePath);

        return convertToGeoJson(completePath);
    }

    /**
     * Converts a drone path and restricted areas to a GeoJSON FeatureCollection.
     * Includes the flight path as a LineString and restricted areas as Polygons.
     * 
     * @param dronePath       the complete drone path with deliveries
     * @param restrictedAreas list of restricted/no-fly zones
     * @return GeoJSON FeatureCollection string
     */
    public String convertDronePathWithRestrictedAreasToGeoJson(
            DronePath dronePath,
            List<Region> restrictedAreas) {

        if (dronePath == null || dronePath.deliveries().isEmpty()) {
            return "{}";
        }

        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        // Add flight path as a LineString feature
        List<Position> completePath = mergePaths(dronePath);
        JSONObject pathFeature = createLineStringFeature(completePath, "Flight Path");
        features.put(pathFeature);

        // Add each restricted area as a Polygon feature
        if (restrictedAreas != null) {
            for (Region region : restrictedAreas) {
                JSONObject restrictedFeature = createPolygonFeature(region);
                features.put(restrictedFeature);
            }
        }

        featureCollection.put("features", features);

        return featureCollection.toString();
    }

    /**
     * Creates a GeoJSON Feature containing a LineString geometry.
     * 
     * @param path        list of positions forming the line
     * @param featureName name/description of the feature
     * @return JSONObject representing the Feature
     */
    private JSONObject createLineStringFeature(List<Position> path, String featureName) {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        // Properties
        JSONObject properties = new JSONObject();
        properties.put("name", featureName);
        properties.put("stroke", "#0000ff"); // Blue line
        properties.put("stroke-width", 3);
        properties.put("stroke-opacity", 0.8);
        feature.put("properties", properties);

        // Geometry
        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");

        JSONArray coordinates = new JSONArray();
        for (Position pos : path) {
            JSONArray point = new JSONArray();
            point.put(pos.lng());
            point.put(pos.lat());
            coordinates.put(point);
        }
        geometry.put("coordinates", coordinates);

        feature.put("geometry", geometry);

        return feature;
    }

    /**
     * Creates a GeoJSON Feature containing a Polygon geometry for a restricted
     * area.
     * 
     * @param region the restricted area region
     * @return JSONObject representing the Feature
     */
    private JSONObject createPolygonFeature(Region region) {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        // Properties
        JSONObject properties = new JSONObject();
        properties.put("name", region.name());

        // Color based on restriction type
        if (isNoFlyZone(region)) {
            properties.put("fill", "#ff0000"); // Red for no-fly
            properties.put("fill-opacity", 0.3);
            properties.put("stroke", "#ff0000");
        } else {
            properties.put("fill", "#ffff00"); // Yellow for restricted
            properties.put("fill-opacity", 0.2);
            properties.put("stroke", "#ffff00");
        }
        properties.put("stroke-width", 2);

        feature.put("properties", properties);

        // Geometry - Polygon requires array of arrays (outer ring + holes)
        JSONObject geometry = new JSONObject();
        geometry.put("type", "Polygon");

        JSONArray coordinates = new JSONArray();
        JSONArray ring = new JSONArray();

        // Add all vertices
        for (Position vertex : region.vertices()) {
            JSONArray point = new JSONArray();
            point.put(vertex.lng());
            point.put(vertex.lat());
            ring.put(point);
        }

        // GeoJSON Polygons must close the ring (first point = last point)
        if (!region.vertices().isEmpty()) {
            Position first = region.vertices().get(0);
            JSONArray closingPoint = new JSONArray();
            closingPoint.put(first.lng());
            closingPoint.put(first.lat());
            ring.put(closingPoint);
        }

        coordinates.put(ring); // Outer ring (no holes)
        geometry.put("coordinates", coordinates);

        feature.put("geometry", geometry);

        return feature;
    }

    /**
     * Checks if a region is a no-fly zone (lower=0, upper=-1).
     * 
     * @param region the region to check
     * @return true if no-fly zone, false if restricted zone
     */
    private boolean isNoFlyZone(Region region) {
        return region.limits() != null
                && region.limits().lower() == 0
                && region.limits().upper() == -1;
    }

    /**
     * Merges all delivery paths into a single continuous path.
     * Removes duplicate positions between deliveries.
     * 
     * @param dronePath the drone path with multiple deliveries
     * @return merged list of positions
     */
    private List<Position> mergePaths(DronePath dronePath) {
        List<Position> completePath = new java.util.ArrayList<>();

        for (int i = 0; i < dronePath.deliveries().size(); i++) {
            Delivery delivery = dronePath.deliveries().get(i);

            if (i == 0) {
                // First delivery: add all positions
                completePath.addAll(delivery.flightPath());
            } else {
                // Subsequent deliveries: skip first position to avoid duplicates
                completePath.addAll(delivery.flightPath().subList(1, delivery.flightPath().size()));
            }
        }

        return completePath;
    }

    /**
     * Converts a list of positions to GeoJSON LineString format.
     * 
     * @param positions the flight path positions
     * @return GeoJSON string representation
     */
    private String convertToGeoJson(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            return "{}";
        }

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "LineString");

        JSONArray coordinates = new JSONArray();
        for (Position pos : positions) {
            JSONArray point = new JSONArray();
            point.put(pos.lng());
            point.put(pos.lat());
            coordinates.put(point);
        }

        geoJson.put("coordinates", coordinates);

        return geoJson.toString();
    }

    /**
     * Converts multiple drone paths to GeoJSON FeatureCollection with restricted
     * areas.
     * Shows all drone routes in different colors.
     * 
     * @param dronePaths      list of all drone paths
     * @param restrictedAreas list of restricted/no-fly zones
     * @return GeoJSON FeatureCollection string
     */
    public String convertMultipleDronePathsToGeoJson(
            List<DronePath> dronePaths,
            List<Region> restrictedAreas) {

        if (dronePaths == null || dronePaths.isEmpty()) {
            return "{}";
        }

        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        // Different colors for different drones
        String[] colors = { "#0000ff", "#00ff00", "#ff00ff", "#00ffff", "#ffff00" };

        // Add each drone's path as a separate LineString
        for (int i = 0; i < dronePaths.size(); i++) {
            DronePath dronePath = dronePaths.get(i);
            List<Position> completePath = mergePaths(dronePath);

            String color = colors[i % colors.length];
            String droneName = "Drone " + dronePath.droneId();

            JSONObject pathFeature = createLineStringFeatureWithColor(completePath, droneName, color);
            features.put(pathFeature);
        }

        // Add restricted areas
        if (restrictedAreas != null) {
            for (Region region : restrictedAreas) {
                JSONObject restrictedFeature = createPolygonFeature(region);
                features.put(restrictedFeature);
            }
        }

        featureCollection.put("features", features);

        return featureCollection.toString();
    }

    /**
     * Creates a GeoJSON Feature containing a LineString geometry with custom color.
     * 
     * @param path        list of positions forming the line
     * @param featureName name/description of the feature
     * @param color       hex color code for the line
     * @return JSONObject representing the Feature
     */
    private JSONObject createLineStringFeatureWithColor(List<Position> path, String featureName, String color) {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        // Properties
        JSONObject properties = new JSONObject();
        properties.put("name", featureName);
        properties.put("stroke", color);
        properties.put("stroke-width", 3);
        properties.put("stroke-opacity", 0.8);
        feature.put("properties", properties);

        // Geometry
        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");

        JSONArray coordinates = new JSONArray();
        for (Position pos : path) {
            JSONArray point = new JSONArray();
            point.put(pos.lng());
            point.put(pos.lat());
            coordinates.put(point);
        }
        geometry.put("coordinates", coordinates);

        feature.put("geometry", geometry);

        return feature;
    }
}