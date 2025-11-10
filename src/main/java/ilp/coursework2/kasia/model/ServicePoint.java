package ilp.coursework2.kasia.model;

/**
 * Represents a drone service point with location.
 */
public record ServicePoint(
        int id,
        String name,
        Position location // lng, lat (ignore alt if present)
) {
}