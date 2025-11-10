package ilp.coursework2.kasia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents which drones are available at a specific service point.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DroneForServicePoint(
                int servicePointId,
                List<DroneAvailability> drones) {
}