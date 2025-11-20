package ilp.coursework2.kasia.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import ilp.coursework2.kasia.model.AvailabilitySlot;
import ilp.coursework2.kasia.model.DroneAvailability;
import ilp.coursework2.kasia.model.DroneForServicePoint;
import ilp.coursework2.kasia.model.MedDispatchRec;

/**
 * Service for checking drone availability based on day of week and time slots.
 */
@Service
public class DroneAvailabilityService {

    /**
     * Checks if a specific drone is available at a given date and time.
     *
     * @param droneId         the drone ID to check
     * @param date            the date of delivery
     * @param time            the time of delivery
     * @param allAvailability all drone availability data from service points
     * @return true if the drone is available, false otherwise
     */
    public boolean isDroneAvailable(
            String droneId,
            LocalDate date,
            LocalTime time,
            List<DroneForServicePoint> allAvailability) {

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Find this drone's availability across all service points
        for (DroneForServicePoint servicePoint : allAvailability) {
            for (DroneAvailability droneAvail : servicePoint.drones()) {
                if (droneAvail.id().equals(droneId)) {
                    // Check if drone is available on this day/time
                    return isAvailableAtTime(droneAvail, dayOfWeek, time);
                }
            }
        }

        // Drone not found in availability data
        return false;
    }

    /**
     * Checks if a drone is available at a specific day and time.
     *
     * @param droneAvail the drone's availability schedule
     * @param dayOfWeek  the day to check
     * @param time       the time to check
     * @return true if available, false otherwise
     */
    private boolean isAvailableAtTime(
            DroneAvailability droneAvail,
            DayOfWeek dayOfWeek,
            LocalTime time) {

        for (AvailabilitySlot slot : droneAvail.availability()) {
            if (slot.dayOfWeek() == dayOfWeek) {
                // Check if time is within this slot's range
                // from is inclusive, until is inclusive (based on spec showing 23:59:59)
                if (!time.isBefore(slot.from()) && !time.isAfter(slot.until()))
                    return true;
            }
        }

        return false;
    }

    /**
     * Checks if a drone is available for ALL dispatches.
     *
     * @param droneId         the drone ID
     * @param dispatches      list of all dispatches
     * @param allAvailability all availability data
     * @return true if available for ALL dispatches, false otherwise
     */
    public boolean isAvailableForAllDispatches(
            String droneId,
            List<MedDispatchRec> dispatches,
            List<DroneForServicePoint> allAvailability) {

        for (MedDispatchRec dispatch : dispatches) {
            // Parse date and time
            LocalDate date = LocalDate.parse(dispatch.date());
            LocalTime time = LocalTime.parse(dispatch.time());

            if (!isDroneAvailable(droneId, date, time, allAvailability))
                return false; // Failed one dispatch

        }

        return true; // Available for all dispatches
    }
}