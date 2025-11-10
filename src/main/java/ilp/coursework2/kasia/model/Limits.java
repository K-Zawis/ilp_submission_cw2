package ilp.coursework2.kasia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents altitude limits for restricted zones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Limits(
        int lower,
        int upper) {
}