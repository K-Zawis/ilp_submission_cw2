package ilp.coursework2.kasia.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.Drone;
import ilp.coursework2.kasia.model.DroneCapability;
import ilp.coursework2.kasia.model.enums.DroneAttribute;
import ilp.coursework2.kasia.model.enums.QueryOperator;

/**
 * Service for querying and filtering drones based on their attributes.
 * Handles attribute extraction, type conversion, and comparison operations.
 */
@Service
public class DroneQueryService {

    /**
     * Filters drones based on a single attribute-value pair using equals
     * comparison.
     *
     * @param drones         the list of drones to filter
     * @param attributeName  the attribute name as string
     * @param attributeValue the value to match as string
     * @return list of drone IDs matching the criteria
     */
    public List<Integer> queryByAttribute(List<Drone> drones, String attributeName, String attributeValue) {
        DroneAttribute attribute = DroneAttribute.fromString(attributeName);

        if (attribute == null) {
            // Unknown attribute, return empty list
            return List.of();
        }

        return drones.stream()
                .filter(drone -> matchesAttribute(drone, attribute, attributeValue, QueryOperator.EQUALS))
                .map(Drone::id)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a drone matches a given attribute-value pair with specified
     * operator.
     *
     * @param drone     the drone to check
     * @param attribute the attribute to compare
     * @param valueStr  the value to compare against (as string)
     * @param operator  the comparison operator
     * @return true if the drone matches, false otherwise
     */
    public boolean matchesAttribute(Drone drone, DroneAttribute attribute, String valueStr, QueryOperator operator) {
        Object actualValue = extractAttributeValue(drone, attribute);

        if (actualValue == null) {
            return false;
        }

        return compareValues(actualValue, valueStr, operator);
    }

    /**
     * Extracts the value of a specific attribute from a drone.
     *
     * @param drone     the drone
     * @param attribute the attribute to extract
     * @return the attribute value, or null if not found
     */
    private Object extractAttributeValue(Drone drone, DroneAttribute attribute) {
        switch (attribute.getLocation()) {
            case DRONE:
                return extractDroneAttribute(drone, attribute);
            case CAPABILITY:
                if (drone.capability() == null)
                    return null;

                return extractCapabilityAttribute(drone.capability(), attribute);
            default:
                return null;
        }
    }

    /**
     * Extracts a top-level drone attribute.
     */
    private Object extractDroneAttribute(Drone drone, DroneAttribute attribute) {
        switch (attribute) {
            case ID:
                return drone.id();
            case NAME:
                return drone.name();
            default:
                return null;
        }
    }

    /**
     * Extracts a capability attribute from a drone's capabilities.
     */
    private Object extractCapabilityAttribute(DroneCapability capability, DroneAttribute attribute) {
        switch (attribute) {
            case COOLING:
                return capability.hasCooling();
            case HEATING:
                return capability.hasHeating();
            case CAPACITY:
                return capability.capacity();
            case MAX_MOVES:
                return capability.maxMoves();
            case COST_PER_MOVE:
                return capability.costPerMove();
            case COST_INITIAL:
                return capability.costInitial();
            case COST_FINAL:
                return capability.costFinal();
            default:
                return null;
        }
    }

    /**
     * Compares an actual value against an expected value using the specified
     * operator.
     *
     * @param actualValue      the actual value from the drone
     * @param expectedValueStr the expected value as string
     * @param operator         the comparison operator
     * @return true if comparison succeeds, false otherwise
     */
    private boolean compareValues(Object actualValue, String expectedValueStr, QueryOperator operator) {
        if (actualValue instanceof Boolean) {
            return compareBooleans((Boolean) actualValue, expectedValueStr, operator);
        } else if (actualValue instanceof Integer) {
            return compareNumbers(((Integer) actualValue).doubleValue(), expectedValueStr, operator);
        } else if (actualValue instanceof Double) {
            return compareNumbers((Double) actualValue, expectedValueStr, operator);
        } else if (actualValue instanceof String) {
            return compareStrings((String) actualValue, expectedValueStr, operator);
        }
        return false;
    }

    /**
     * Compares boolean values.
     */
    private boolean compareBooleans(boolean actual, String expectedStr, QueryOperator operator) {
        boolean expected = Boolean.parseBoolean(expectedStr);

        switch (operator) {
            case EQUALS:
                return actual == expected;
            case NOT_EQUALS:
                return actual != expected;
            default:
                return false;
        }
    }

    /**
     * Compares numeric values.
     */
    private boolean compareNumbers(double actual, String expectedStr, QueryOperator operator) {
        try {
            double expected = Double.parseDouble(expectedStr);

            switch (operator) {
                case EQUALS:
                    return Math.abs(actual - expected) < 0.0001; // Epsilon comparison - might remove later idk
                case NOT_EQUALS:
                    return Math.abs(actual - expected) >= 0.0001;
                case LESS_THAN:
                    return actual < expected;
                case GREATER_THAN:
                    return actual > expected;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Compares string values.
     */
    private boolean compareStrings(String actual, String expected, QueryOperator operator) {
        switch (operator) {
            case EQUALS:
                return actual.equals(expected);
            case NOT_EQUALS:
                return !actual.equals(expected);
            default:
                return false;
        }
    }
}