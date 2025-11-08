package ilp.coursework2.kasia.model.enums;

/**
 * Enumeration of queryable drone attributes.
 * Maps attribute names to their locations in the Drone object hierarchy.
 */
public enum DroneAttribute {
    // Top-level attributes
    ID("id", AttributeLocation.DRONE),
    NAME("name", AttributeLocation.DRONE),

    // Capability attributes
    COOLING("cooling", AttributeLocation.CAPABILITY),
    HEATING("heating", AttributeLocation.CAPABILITY),
    CAPACITY("capacity", AttributeLocation.CAPABILITY),
    MAX_MOVES("maxmoves", AttributeLocation.CAPABILITY),
    COST_PER_MOVE("costpermove", AttributeLocation.CAPABILITY),
    COST_INITIAL("costinitial", AttributeLocation.CAPABILITY),
    COST_FINAL("costfinal", AttributeLocation.CAPABILITY);

    private final String attributeName;
    private final AttributeLocation location;

    DroneAttribute(String attributeName, AttributeLocation location) {
        this.attributeName = attributeName;
        this.location = location;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public AttributeLocation getLocation() {
        return location;
    }

    /**
     * Finds a DroneAttribute by its string name (case-insensitive).
     *
     * @param name the attribute name
     * @return the matching DroneAttribute, or null if not found
     */
    public static DroneAttribute fromString(String name) {
        String normalized = name.toLowerCase().trim();
        for (DroneAttribute attr : values()) {
            if (attr.attributeName.equals(normalized)) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Indicates where in the Drone object hierarchy the attribute is located.
     */
    public enum AttributeLocation {
        DRONE,
        CAPABILITY
    }
}