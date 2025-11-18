package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents a single drone's complete delivery route.
 */
public record DronePath(
                String droneId,
                List<Delivery> deliveries) {
}