package ilp.coursework2.kasia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Position(Double lng, Double lat) {
}