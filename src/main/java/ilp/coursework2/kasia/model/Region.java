package ilp.coursework2.kasia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a geographical region (no-fly zone or restricted area).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Region(
        String name,
        int id,
        Limits limits,
        List<Position> vertices) {
}