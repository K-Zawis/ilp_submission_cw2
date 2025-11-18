package ilp.coursework2.kasia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a drone's availability schedule.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DroneAvailability(
        String id,
        List<AvailabilitySlot> availability) {
}