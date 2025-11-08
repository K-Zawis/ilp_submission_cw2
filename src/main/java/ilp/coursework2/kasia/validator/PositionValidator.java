package ilp.coursework2.kasia.validator;

import org.springframework.stereotype.Component;

import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;

/**
 * Validator for position-based data structures.
 * Ensures that geographic coordinates and position-related requests contain
 * valid data.
 */
@Component
public class PositionValidator implements IPositionValidator {

    /**
     * Validates a pair of positions.
     *
     * @param positions the position pair to validate
     * @return true if both positions are valid, false otherwise
     */
    public boolean isValidPositionPair(PositionPair positions) {
        return positions != null
                && isValidPosition(positions.position1())
                && isValidPosition(positions.position2());
    }

    /**
     * Validates a single position.
     * A valid position must have non-null longitude and latitude values
     * within valid geographic ranges.
     * Valid ranges: latitude [-90, 90], longitude [-180, 180]
     *
     * @param position the position to validate
     * @return true if the position is valid, false otherwise
     */
    public boolean isValidPosition(Position position) {
        return position != null
                && position.lat() != null
                && position.lng() != null
                && position.lat() >= -90.0 && position.lat() <= 90.0
                && position.lng() >= -180.0 && position.lng() <= 180.0;
    }

    /**
     * Validates a next position request.
     * Ensures the starting position is valid and the angle is provided.
     *
     * @param request the next position request to validate
     * @return true if the request is valid, false otherwise
     */
    public boolean isValidNextPositionRequest(NextPositionRequest request) {
        return request != null
                && isValidPosition(request.start())
                && request.angle() != null;
    }
}