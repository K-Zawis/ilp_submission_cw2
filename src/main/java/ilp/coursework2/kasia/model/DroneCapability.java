package ilp.coursework2.kasia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the capabilities and costs of a drone.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DroneCapability(
        Boolean cooling,
        Boolean heating,
        double capacity,
        int maxMoves,
        double costPerMove,
        double costInitial,
        double costFinal) {
    /**
     * Returns whether this drone supports cooling.
     * 
     * @return true if cooling is explicitly true, false otherwise
     */
    public boolean hasCooling() {
        return cooling != null && cooling;
    }

    /**
     * Returns whether this drone supports heating.
     * 
     * @return true if heating is explicitly true, false otherwise
     */
    public boolean hasHeating() {
        return heating != null && heating;
    }
}