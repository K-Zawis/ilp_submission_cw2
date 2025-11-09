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
}