package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents a geographical region (no-fly zone or restricted area).
 */
public record Region(
        String name,
        int id,
        Limits limits,
        List<Position> vertices) {
}