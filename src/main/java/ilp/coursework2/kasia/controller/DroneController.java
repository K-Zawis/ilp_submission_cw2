package ilp.coursework2.kasia.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ilp.coursework2.kasia.interfaces.IDroneController;
import ilp.coursework2.kasia.interfaces.IDroneService;
import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.interfaces.IRegionValidator;
import ilp.coursework2.kasia.model.DeliveryPathResponse;
import ilp.coursework2.kasia.model.Drone;
import ilp.coursework2.kasia.model.DroneForServicePoint;
import ilp.coursework2.kasia.model.MedDispatchRec;
import ilp.coursework2.kasia.model.NextPositionRequest;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.model.PositionPair;
import ilp.coursework2.kasia.model.QueryCriteria;
import ilp.coursework2.kasia.model.Region;
import ilp.coursework2.kasia.model.RegionRequest;
import ilp.coursework2.kasia.model.ServicePoint;
import ilp.coursework2.kasia.service.DeliveryPlannerService;
import ilp.coursework2.kasia.service.DroneMatchingService;
import ilp.coursework2.kasia.service.DroneQueryService;
import ilp.coursework2.kasia.service.GeoJsonService;
import ilp.coursework2.kasia.service.IlpService;

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
    private final IlpService ilpService;
    private final DroneQueryService droneQueryService;
    private final DroneMatchingService droneMatchingService;
    private final DeliveryPlannerService deliveryPlannerService;
    private final GeoJsonService geoJsonService;

    /**
     * Constructs a new DroneController with required dependencies.
     *
     * @param droneService           service for drone navigation calculations
     * @param positionValidator      validator for position-based requests
     * @param regionValidator        validator for region-based requests
     * @param ilpService             service for ILP REST API integration
     * @param droneQueryService      service for querying drones by attributes
     * @param droneMatchingService   service for matching drones to dispatch
     *                               requirements
     * @param deliveryPlannerService service for planning multi-delivery routes
     * @param geoJsonService         service for GeoJSON conversion
     */
    public DroneController(
            IDroneService droneService,
            IPositionValidator positionValidator,
            IRegionValidator regionValidator,
            IlpService ilpService,
            DroneQueryService droneQueryService,
            DroneMatchingService droneMatchingService,
            DeliveryPlannerService deliveryPlannerService,
            GeoJsonService geoJsonService) {
        this.droneService = droneService;
        this.positionValidator = positionValidator;
        this.regionValidator = regionValidator;
        this.ilpService = ilpService;
        this.droneQueryService = droneQueryService;
        this.droneMatchingService = droneMatchingService;
        this.deliveryPlannerService = deliveryPlannerService;
        this.geoJsonService = geoJsonService;
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

    // ==================
    // CW2 Static Query Endpoints
    // ==================

    /**
     * Retrieves drone IDs that support cooling (or not).
     * 
     * GET /api/v1/dronesWithCooling/{state}
     * 
     * @param state "true" for drones with cooling, "false" for drones without
     * @return list of drone IDs
     */
    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable String state) {
        // Fetch ALL drones from ILP service
        List<Drone> allDrones = ilpService.getAllDrones();

        // Filter based on cooling capability
        boolean requiresCooling = state.equals("true");
        List<String> droneIds = allDrones.stream()
                .filter(drone -> drone.capability().hasCooling() == requiresCooling)
                .map(Drone::id)
                .toList();

        return ResponseEntity.ok(droneIds);
    }

    /**
     * Retrieves detailed information about a specific drone.
     * 
     * GET /api/v1/droneDetails/{id}
     * 
     * @param id the drone ID
     * @return the drone object with full details, or 404 if not found
     */
    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable String id) {
        // Fetch ALL drones from ILP service
        List<Drone> allDrones = ilpService.getAllDrones();

        // Find the specific drone by ID
        Drone drone = allDrones.stream()
                .filter(d -> d.id().equals(
                        id))
                .findFirst()
                .orElse(null);

        if (drone == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(drone);
    }

    // ==================
    // CW2 Dynamic Query Endpoints
    // ==================

    /**
     * Queries drones by a single attribute with exact match.
     * 
     * GET /api/v1/queryAsPath/{attributeName}/{attributeValue}
     * 
     * @param attributeName  the name of the attribute to query (e.g., "capacity",
     *                       "cooling")
     * @param attributeValue the value to match (as string)
     * @return list of drone IDs matching the criteria
     */
    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ResponseEntity<List<String>> queryAsPath(
            @PathVariable String attributeName,
            @PathVariable String attributeValue) {

        List<Drone> allDrones = ilpService.getAllDrones();
        List<String> droneIds = droneQueryService.queryByAttribute(
                allDrones,
                attributeName,
                attributeValue);

        return ResponseEntity.ok(droneIds);
    }

    /**
     * Queries drones by multiple criteria with AND logic.
     * All criteria must match for a drone to be included.
     * 
     * POST /api/v1/query
     * 
     * @param criteria list of query criteria (attribute, operator, value)
     * @return list of drone IDs matching ALL criteria
     */
    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@RequestBody List<QueryCriteria> criteria) {
        List<Drone> allDrones = ilpService.getAllDrones();
        List<String> droneIds = droneQueryService.queryByMultipleCriteria(allDrones, criteria);
        return ResponseEntity.ok(droneIds);
    }

    // ==================
    // CW2 Availability Query Endpoints
    // ==================

    /**
     * Finds drones that can fulfill ALL dispatch requirements.
     * Returns drones that meet capacity/cooling/heating requirements AND
     * are available at all dispatch times.
     * 
     * POST /api/v1/queryAvailableDrones
     * 
     * @param dispatches list of dispatch records to fulfill
     * @return list of drone IDs that can handle all dispatches
     */
    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(
            @RequestBody List<MedDispatchRec> dispatches) {

        List<Drone> allDrones = ilpService.getAllDrones();
        List<DroneForServicePoint> allAvailability = ilpService.getDronesForServicePoints();

        List<String> matchingDrones = droneMatchingService.findMatchingDrones(
                dispatches,
                allDrones,
                allAvailability);

        return ResponseEntity.ok(matchingDrones);
    }

    /**
     * Plans optimal delivery routes for multiple dispatch records.
     * Returns complete flight paths with hover points for deliveries.
     * 
     * POST /api/v1/calcDeliveryPath
     * 
     * @param dispatches list of medical dispatch records to deliver
     * @return DeliveryPathResponse with drone paths, moves, and costs
     */
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<DeliveryPathResponse> calcDeliveryPath(
            @RequestBody List<MedDispatchRec> dispatches) {

        // Fetch fresh data from ILP service (as per spec requirement)
        List<Drone> allDrones = ilpService.getAllDrones();
        List<ServicePoint> servicePoints = ilpService.getServicePoints();
        List<DroneForServicePoint> availability = ilpService.getDronesForServicePoints();
        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        // Plan the deliveries
        DeliveryPathResponse response = deliveryPlannerService.planDeliveries(
                dispatches,
                allDrones,
                servicePoints,
                availability,
                restrictedAreas);

        return ResponseEntity.ok(response);
    }

    /**
     * Plans delivery route and returns as GeoJSON LineString.
     * Guaranteed to work with single drone only.
     * 
     * POST /api/v1/calcDeliveryPathAsGeoJson
     * 
     * @param dispatches list of medical dispatch records (single drone scenario)
     * @return GeoJSON LineString representation of the complete flight path
     */
    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(
            @RequestBody List<MedDispatchRec> dispatches) {

        // Fetch fresh data from ILP service
        List<Drone> allDrones = ilpService.getAllDrones();
        List<ServicePoint> servicePoints = ilpService.getServicePoints();
        List<DroneForServicePoint> availability = ilpService.getDronesForServicePoints();
        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        // Plan the deliveries
        DeliveryPathResponse response = deliveryPlannerService.planDeliveries(
                dispatches, allDrones, servicePoints, availability, restrictedAreas);

        // Check if planning succeeded
        if (response.dronePaths().isEmpty()) {
            return ResponseEntity.ok("{}");
        }

        // Convert to GeoJSON using service
        String geoJson = geoJsonService.convertDronePathToGeoJson(response.dronePaths().get(0));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }

    // ==================
    // Additional stuff
    // ==================

    /**
     * Plans delivery route and returns as GeoJSON FeatureCollection with restricted
     * areas.
     * Shows all drone flight paths (color-coded) and no-fly/restricted zones.
     * Supports both single-drone and multi-drone scenarios.
     * 
     * POST /api/v1/calcDeliveryPathAsGeoJsonWithZones
     * 
     * @param dispatches list of medical dispatch records
     * @return GeoJSON FeatureCollection with flight paths and restricted areas
     */
    @PostMapping("/calcDeliveryPathAsGeoJsonWithZones")
    public ResponseEntity<String> calcDeliveryPathAsGeoJsonWithZones(
            @RequestBody List<MedDispatchRec> dispatches) {

        // Fetch fresh data from ILP service
        List<Drone> allDrones = ilpService.getAllDrones();
        List<ServicePoint> servicePoints = ilpService.getServicePoints();
        List<DroneForServicePoint> availability = ilpService.getDronesForServicePoints();
        List<Region> restrictedAreas = ilpService.getRestrictedAreas();

        // Plan the deliveries
        DeliveryPathResponse response = deliveryPlannerService.planDeliveries(
                dispatches, allDrones, servicePoints, availability, restrictedAreas);

        // Check if planning succeeded
        if (response.dronePaths().isEmpty()) {
            return ResponseEntity.ok("{}");
        }

        // Convert to GeoJSON - supports both single and multi-drone
        String geoJson = geoJsonService.convertMultipleDronePathsToGeoJson(
                response.dronePaths(),
                restrictedAreas);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(geoJson);
    }
}
