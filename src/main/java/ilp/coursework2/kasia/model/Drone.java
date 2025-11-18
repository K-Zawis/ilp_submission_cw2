package ilp.coursework2.kasia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a drone with its capabilities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Drone(
        String id,
        String name,
        DroneCapability capability) {
}