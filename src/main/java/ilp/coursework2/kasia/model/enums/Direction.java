package ilp.coursework2.kasia.model.enums;

import ilp.coursework2.kasia.model.Position;

/**
 * Valid movement directions for the drone (16 compass directions).
 */
public enum Direction {
    E(0),
    ENE(22.5),
    NE(45),
    NNE(67.5),
    N(90),
    NNW(112.5),
    NW(135),
    WNW(157.5),
    W(180),
    WSW(202.5),
    SW(225),
    SSW(247.5),
    S(270),
    SSE(292.5),
    SE(315),
    ESE(337.5);

    private final double angle;

    Direction(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    /**
     * Get the next position by moving in this direction.
     * Movement distance is 0.00015 degrees.
     */
    public Position move(Position from) {
        double radians = Math.toRadians(angle);
        double distance = 0.00015;

        double newLng = from.lng() + distance * Math.cos(radians);
        double newLat = from.lat() + distance * Math.sin(radians);

        return new Position(newLng, newLat);
    }
}