package ilp.coursework2.kasia.model;

/**
 * Represents a drone with its capabilities.
 */
public record Drone(
        int id,
        String name,
        DroneCapability capability) {
}