package ilp.coursework2.kasia.model;

/**
 * Requirements for a medicine dispatch.
 * Capacity is required, all other fields are optional.
 * Cooling and heating are mutually exclusive.
 */
public record MedDispatchRequirements(
        double capacity,
        Boolean cooling,
        Boolean heating,
        Double maxCost) {
    /**
     * Returns whether cooling is required.
     * 
     * @return true if cooling is explicitly true, false otherwise
     */
    public boolean requiresCooling() {
        return cooling != null && cooling;
    }

    /**
     * Returns whether heating is required.
     * 
     * @return true if heating is explicitly true, false otherwise
     */
    public boolean requiresHeating() {
        return heating != null && heating;
    }

    /**
     * Returns whether a max cost constraint exists.
     * 
     * @return true if maxCost is present, false otherwise
     */
    public boolean hasMaxCost() {
        return maxCost != null;
    }
}