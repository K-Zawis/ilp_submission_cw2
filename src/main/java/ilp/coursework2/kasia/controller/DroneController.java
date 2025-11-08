package ilp.coursework2.kasia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ilp.coursework2.kasia.interfaces.IDroneController;
import ilp.coursework2.kasia.interfaces.IDroneService;
import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.interfaces.IRegionValidator;
import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.RegionRequest;

/**
 * REST controller for drone navigation endpoints.
 * Provides API endpoints for distance calculations, proximity checks,
 * position updates, and region containment queries.
 * All endpoints use the /api/v1 prefix as per specification.
 */
@RestController
@RequestMapping("/api/v1")
public class DroneController implements IDroneController {

    private final IDroneService droneService;
    private final IPositionValidator positionValidator;
    private final IRegionValidator regionValidator;

    /**
     * Constructs a new DroneController with required dependencies.
     *
     * @param droneService      service for drone navigation calculations
     * @param positionValidator validator for position-based requests
     * @param regionValidator   validator for region-based requests
     */
    public DroneController(
            IDroneService droneService,
            IPositionValidator positionValidator,
            IRegionValidator regionValidator) {
        this.droneService = droneService;
        this.positionValidator = positionValidator;
        this.regionValidator = regionValidator;
    }

    /**
     * Returns the student ID.
     *
     * @return the student ID as a string
     */
    @Override
    @GetMapping("/uid")
    public String uid() {
        return "s2340338";
    }

    /**
     * Calculates the Euclidean distance between two positions.
     *
     * @param positions a pair of positions to measure
     * @return ResponseEntity containing the distance, or 400 Bad Request if input
     *         is invalid
     */
    @Override
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody PositionPair positions) {
        if (!positionValidator.isValidPositionPair(positions)) {
            return ResponseEntity.badRequest().build();
        }

        double distance = droneService.calculateDistance(positions);
        return ResponseEntity.ok(distance);
    }

    /**
     * Determines if two positions are close to each other.
     *
     * @param positions a pair of positions to compare
     * @return ResponseEntity containing true if close, false otherwise, or 400 Bad
     *         Request if input is invalid
     */
    @Override
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody PositionPair positions) {
        if (!positionValidator.isValidPositionPair(positions)) {
            return ResponseEntity.badRequest().build();
        }

        boolean isClose = droneService.isCloseTo(positions);
        return ResponseEntity.ok(isClose);
    }

    /**
     * Calculates the next position of the drone after moving in a specified
     * direction.
     *
     * @param request contains the starting position and movement angle (in degrees)
     * @return ResponseEntity containing the new position, or 400 Bad Request if
     *         input is invalid
     */
    @Override
    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(@RequestBody NextPositionRequest request) {
        if (!positionValidator.isValidNextPositionRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        Position next = droneService.calculateNextPosition(request);
        return ResponseEntity.ok(next);
    }

    /**
     * Determines if a point is inside a given region (polygon).
     * Points on the boundary are considered inside as per specification.
     *
     * @param request contains the point to test and the region definition
     * @return ResponseEntity containing true if inside or on boundary, false
     *         otherwise,
     *         or 400 Bad Request if input is invalid
     */
    @Override
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionRequest request) {
        if (!regionValidator.isValidRegionRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        boolean inRegion = droneService.isPointInRegion(request);
        return ResponseEntity.ok(inRegion);
    }

}
