package ilp.coursework2.kasia.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a time slot when a drone is available on a specific day.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AvailabilitySlot(
                DayOfWeek dayOfWeek,
                LocalTime from,
                LocalTime until) {
}