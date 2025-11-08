package ilp.coursework2.kasia.interfaces;

import org.springframework.http.ResponseEntity;

import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.RegionRequest;

public interface IDroneController {
    String uid();

    ResponseEntity<Double> distanceTo(PositionPair positions);

    ResponseEntity<Boolean> isCloseTo(PositionPair positions);

    ResponseEntity<Position> nextPosition(NextPositionRequest request);

    ResponseEntity<Boolean> isInRegion(RegionRequest request);
}
