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
}