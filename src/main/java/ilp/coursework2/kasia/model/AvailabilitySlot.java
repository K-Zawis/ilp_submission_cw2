package ilp.coursework2.kasia.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a time slot when a drone is available on a specific day.
 */
public record AvailabilitySlot(
        DayOfWeek dayOfWeek,
        LocalTime from,
        LocalTime until) {
}