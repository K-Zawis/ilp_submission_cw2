package ilp.coursework2.kasia.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.*;

/**
 * Service for planning multi-delivery drone routes.
 * Implements Phase 1 (simple routing) for CW2.
 */
@Service
public class DeliveryPlannerService {

    private final PathfindingService pathfindingService;
    private final DroneMatchingService droneMatchingService;

    public DeliveryPlannerService(
            PathfindingService pathfindingService,
            DroneMatchingService droneMatchingService) {
        this.pathfindingService = pathfindingService;
        this.droneMatchingService = droneMatchingService;
    }

    /**
     * Plans delivery routes for all dispatches.
     * Phase 1: Simple single-drone routing with cost validation.
     * 
     * @param dispatches      list of deliveries to make
     * @param allDrones       all available drones
     * @param servicePoints   all service points
     * @param availability    drone availability data
     * @param restrictedAreas no-fly zones
     * @return complete delivery plan with paths and costs
     */
    public DeliveryPathResponse planDeliveries(
            List<MedDispatchRec> dispatches,
            List<Drone> allDrones,
            List<ServicePoint> servicePoints,
            List<DroneForServicePoint> availability,
            List<Region> restrictedAreas) {

        if (dispatches == null || dispatches.isEmpty()) {
            return new DeliveryPathResponse(0.0, 0, List.of());
        }

        // Find drones that can handle ALL dispatches
        // (capacity/cooling/heating/availability)
        List<Integer> matchingDroneIds = droneMatchingService.findMatchingDrones(
                dispatches,
                allDrones,
                availability);

        if (matchingDroneIds.isEmpty()) {
            return new DeliveryPathResponse(0.0, 0, List.of());
        }

        // Try each matching drone until we find one that meets all constraints
        // including cost
        for (int droneId : matchingDroneIds) {
            Drone drone = allDrones.stream()
                    .filter(d -> d.id() == droneId)
                    .findFirst()
                    .orElse(null);

            if (drone == null) {
                continue;
            }

            // Find which service point has this drone
            ServicePoint servicePoint = findServicePointForDrone(
                    droneId,
                    servicePoints,
                    availability);

            if (servicePoint == null) {
                continue;
            }

            // Build the complete route
            DronePath dronePath = buildRoute(
                    drone,
                    dispatches,
                    servicePoint,
                    restrictedAreas);

            if (dronePath == null || dronePath.deliveries().isEmpty()) {
                continue; // Try next drone
            }

            // Validate maxCost constraints
            if (!validateMaxCostConstraints(dispatches, dronePath, drone)) {
                continue; // This drone works but exceeds cost limit - try next one
            }

            // Success! This drone meets all constraints
            int totalMoves = calculateTotalMoves(dronePath);
            double totalCost = calculateTotalCost(drone, totalMoves);

            return new DeliveryPathResponse(
                    totalCost,
                    totalMoves,
                    List.of(dronePath));
        }

        // No drone could fulfill all constraints (including maxCost)
        return new DeliveryPathResponse(0.0, 0, List.of());
    }

    /**
     * Builds a complete route for a single drone handling all deliveries.
     * Route: Service Point → Delivery 1 → Delivery 2 → ... → Service Point
     * Each delivery gets a hover point (duplicate position).
     * 
     * @param drone           the drone to use
     * @param dispatches      deliveries to make
     * @param servicePoint    starting/ending service point
     * @param restrictedAreas no-fly zones
     * @return DronePath with all deliveries, or null if impossible
     */
    private DronePath buildRoute(
            Drone drone,
            List<MedDispatchRec> dispatches,
            ServicePoint servicePoint,
            List<Region> restrictedAreas) {

        Position currentPosition = servicePoint.location();
        List<Delivery> deliveries = new ArrayList<>();
        int movesUsed = 0;
        int maxMoves = drone.capability().maxMoves();

        for (int i = 0; i < dispatches.size(); i++) {
            MedDispatchRec dispatch = dispatches.get(i);
            Position deliveryLocation = dispatch.delivery();

            // Find path from current position to delivery location
            List<Position> pathToDelivery = pathfindingService.findPath(
                    currentPosition,
                    deliveryLocation,
                    restrictedAreas,
                    maxMoves - movesUsed);

            if (pathToDelivery.isEmpty()) {
                return null;
            }

            // Build flight path
            List<Position> flightPath = new ArrayList<>();

            // Add path to delivery
            if (i == 0) {
                flightPath.addAll(pathToDelivery);
            } else {
                flightPath.addAll(pathToDelivery.subList(1, pathToDelivery.size()));
            }

            // Add hover point
            Position lastPosition = flightPath.get(flightPath.size() - 1);
            flightPath.add(lastPosition);

            // If last delivery, add return path
            if (i == dispatches.size() - 1) {
                int movesForThisDelivery = flightPath.size() - 1;
                List<Position> returnPath = pathfindingService.findPath(
                        lastPosition,
                        servicePoint.location(),
                        restrictedAreas,
                        maxMoves - movesUsed - movesForThisDelivery);

                if (returnPath.isEmpty()) {
                    return null;
                }

                // Add return path (skip first to avoid duplicate)
                flightPath.addAll(returnPath.subList(1, returnPath.size()));
            }

            // Now create the delivery with complete flight path
            deliveries.add(new Delivery(dispatch.id(), flightPath));

            // Update state
            movesUsed += (flightPath.size() - 1);
            currentPosition = lastPosition;
        }

        return new DronePath(drone.id(), deliveries);
    }

    /**
     * Finds which service point has a specific drone available.
     * 
     * @param droneId       the drone ID to find
     * @param servicePoints all service points
     * @param availability  drone availability data
     * @return the service point with this drone, or null if not found
     */
    private ServicePoint findServicePointForDrone(
            int droneId,
            List<ServicePoint> servicePoints,
            List<DroneForServicePoint> availability) {

        // Find which service point has this drone
        for (DroneForServicePoint dfsp : availability) {
            boolean hasDrone = dfsp.drones().stream()
                    .anyMatch(da -> da.id() == droneId);

            if (hasDrone) {
                // Find the corresponding ServicePoint
                return servicePoints.stream()
                        .filter(sp -> sp.id() == dfsp.servicePointId())
                        .findFirst()
                        .orElse(null);
            }
        }

        return null;
    }

    /**
     * Calculates total moves from all deliveries in a drone path.
     * Moves = positions - 1 (because n positions = n-1 moves)
     * 
     * @param dronePath the drone's complete path
     * @return total number of moves
     */
    private int calculateTotalMoves(DronePath dronePath) {
        return dronePath.deliveries().stream()
                .mapToInt(delivery -> delivery.flightPath().size() - 1)
                .sum();
    }

    /**
     * Calculates total cost for a drone's flight.
     * Cost = costInitial + costFinal + (moves * costPerMove)
     * 
     * @param drone      the drone used
     * @param totalMoves total number of moves
     * @return total cost
     */
    private double calculateTotalCost(Drone drone, int totalMoves) {
        DroneCapability cap = drone.capability();
        return cap.costInitial()
                + cap.costFinal()
                + (totalMoves * cap.costPerMove());
    }

    /**
     * Validates that all dispatches meet their maxCost constraints.
     * Cost is distributed pro-rata across all deliveries.
     * 
     * @param dispatches the dispatch records with cost constraints
     * @param dronePath  the planned route
     * @param drone      the drone used
     * @return true if all cost constraints are met, false otherwise
     */
    private boolean validateMaxCostConstraints(
            List<MedDispatchRec> dispatches,
            DronePath dronePath,
            Drone drone) {

        // Calculate total moves and total cost
        int totalMoves = calculateTotalMoves(dronePath);
        double totalCost = calculateTotalCost(drone, totalMoves);

        // Number of deliveries to split cost across
        int numDeliveries = dispatches.size();

        // Pro-rata cost per delivery
        double costPerDelivery = totalCost / numDeliveries;

        // Check each dispatch's maxCost constraint
        for (MedDispatchRec dispatch : dispatches) {
            if (dispatch.hasMaxCost()) {
                double maxCost = dispatch.requirements().maxCost();

                if (costPerDelivery > maxCost) {
                    return false; // This delivery exceeds its cost constraint
                }
            }
        }

        return true; // All cost constraints met
    }
}