package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents a drone's availability schedule.
 */
public record DroneAvailability(
        int id,
        List<AvailabilitySlot> availability) {
}