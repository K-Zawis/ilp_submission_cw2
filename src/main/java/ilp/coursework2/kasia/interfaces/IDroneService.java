package ilp.coursework2.kasia.interfaces;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.RegionRequest;

public interface IDroneService {
    double calculateDistance(PositionPair positions);

    boolean isCloseTo(PositionPair positions);

    Position calculateNextPosition(NextPositionRequest request);

    boolean isPointInRegion(RegionRequest request);
}
