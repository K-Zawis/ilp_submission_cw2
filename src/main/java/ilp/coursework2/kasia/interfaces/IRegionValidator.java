package ilp.coursework2.kasia.interfaces;

import java.util.List;

import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;

public interface IRegionValidator {
    boolean isValidRegionRequest(RegionRequest request);

    boolean isRegionValid(Region region);

    boolean areVerticesValid(List<Position> vertices);

    boolean isValidPolygon(List<Position> points);

    boolean isPolygonClosed(List<Position> points);
}
