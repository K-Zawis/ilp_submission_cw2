package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents which drones are available at a specific service point.
 */
public record DroneForServicePoint(
        int servicePointId,
        List<DroneAvailability> drones) {
}