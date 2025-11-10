package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Represents a single delivery within a drone's path.
 */
public record Delivery(
        int deliveryId,
        List<Position> flightPath) {
}