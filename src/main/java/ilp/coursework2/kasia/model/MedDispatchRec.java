package ilp.coursework2.kasia.model;

/**
 * Represents a medicine dispatch record from the ILP service.
 * Only id and capacity are required; all other fields are optional.
 */
public record MedDispatchRec(
        int id,
        String date,
        String time,
        MedDispatchRequirements requirements,
        Position delivery) {

    /**
     * Returns whether cooling is required.
     * 
     * @return true if cooling is explicitly true, false otherwise
     */
    public boolean requiresCooling() {
        return requirements().cooling() != null && requirements().cooling();
    }

    /**
     * Returns whether heating is required.
     * 
     * @return true if heating is explicitly true, false otherwise
     */
    public boolean requiresHeating() {
        return requirements().heating() != null && requirements().heating();
    }

    /**
     * Returns whether a max cost constraint exists.
     * 
     * @return true if maxCost is present, false otherwise
     */
    public boolean hasMaxCost() {
        return requirements().maxCost() != null;
    }
}