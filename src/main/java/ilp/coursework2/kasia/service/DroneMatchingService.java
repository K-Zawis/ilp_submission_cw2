package ilp.coursework2.kasia.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.Drone;
import ilp.coursework2.kasia.model.DroneCapability;
import ilp.coursework2.kasia.model.DroneForServicePoint;
import ilp.coursework2.kasia.model.MedDispatchRec;

/**
 * Service for matching drones to dispatch requirements.
 * Handles checking if drones meet capacity, cooling/heating, and cost
 * requirements.
 */
@Service
public class DroneMatchingService {

    private final DroneAvailabilityService availabilityService;

    public DroneMatchingService(DroneAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Finds all drones that can fulfill ALL dispatches.
     * A drone must meet requirements for every dispatch AND be available at all
     * times.
     *
     * @param dispatches      list of dispatch records
     * @param allDrones       list of all drones with capabilities
     * @param allAvailability drone availability data
     * @return list of drone IDs that can handle all dispatches
     */
    public List<String> findMatchingDrones(
            List<MedDispatchRec> dispatches,
            List<Drone> allDrones,
            List<DroneForServicePoint> allAvailability) {

        if (dispatches == null || dispatches.isEmpty()) {
            return List.of();
        }

        return allDrones.stream()
                .filter(drone -> canHandleAllDispatches(drone, dispatches, allAvailability))
                .map(Drone::id)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a single drone can handle ALL dispatches.
     *
     * @param drone           the drone to check
     * @param dispatches      all dispatches to fulfill
     * @param allAvailability availability data
     * @return true if drone can handle all, false otherwise
     */
    private boolean canHandleAllDispatches(
            Drone drone,
            List<MedDispatchRec> dispatches,
            List<DroneForServicePoint> allAvailability) {

        // Check if drone meets requirements for ALL dispatches
        for (MedDispatchRec dispatch : dispatches) {
            if (!meetsRequirements(drone, dispatch)) {
                return false;
            }
        }

        // Check if drone is available for ALL dispatch times
        return availabilityService.isAvailableForAllDispatches(
                drone.id(),
                dispatches,
                allAvailability);
    }

    /**
     * Checks if a drone meets the requirements for a single dispatch.
     *
     * @param drone    the drone
     * @param dispatch the dispatch record
     * @return true if requirements are met, false otherwise
     */
    public boolean meetsRequirements(Drone drone, MedDispatchRec dispatch) {
        DroneCapability capability = drone.capability();

        if (capability == null)
            return false;

        double requiredCapacity = dispatch.requirements().capacity();
        if (capability.capacity() < requiredCapacity)
            return false;
        if (dispatch.requiresCooling() && !capability.hasCooling())
            return false;
        if (dispatch.requiresHeating() && !capability.hasHeating())
            return false;

        // TODO: maxCost calc stuff

        return true;
    }
}