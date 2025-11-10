package ilp.coursework2.kasia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a drone service point with location.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServicePoint(
        int id,
        String name,
        Position location) {
}