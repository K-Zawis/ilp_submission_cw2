package ilp.coursework2.kasia.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.Delivery;
import ilp.coursework2.kasia.model.DronePath;
import ilp.coursework2.kasia.model.Position;

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
}