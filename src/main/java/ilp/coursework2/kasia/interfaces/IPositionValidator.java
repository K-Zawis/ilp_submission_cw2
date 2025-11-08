package ilp.coursework2.kasia.interfaces;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;

public interface IPositionValidator {
    boolean isValidPositionPair(PositionPair positions);

    boolean isValidPosition(Position position);

    boolean isValidNextPositionRequest(NextPositionRequest request);
}
