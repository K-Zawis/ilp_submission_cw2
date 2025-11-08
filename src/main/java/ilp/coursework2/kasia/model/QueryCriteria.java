package ilp.coursework2.kasia.model;

/**
 * Represents a single query criterion with attribute, operator, and value.
 */
public record QueryCriteria(
        String attribute,
        String operator,
        String value) {
}