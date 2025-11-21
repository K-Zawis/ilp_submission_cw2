package ilp.coursework2.kasia.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ilp.coursework2.kasia.model.AvailabilitySlot;
import ilp.coursework2.kasia.model.DroneAvailability;
import ilp.coursework2.kasia.model.DroneForServicePoint;
import ilp.coursework2.kasia.model.MedDispatchRec;
import ilp.coursework2.kasia.model.MedDispatchRequirements;
import ilp.coursework2.kasia.model.Position;

@DisplayName("Drone Availability Service Tests")
class DroneAvailabilityServiceTest {

    private DroneAvailabilityService service;
    private List<DroneForServicePoint> testAvailability;

    @BeforeEach
    void setUp() {
        service = new DroneAvailabilityService();

        // Create test availability data
        // Drone 1: Available Monday-Friday, 9:00-17:00
        AvailabilitySlot slot1 = new AvailabilitySlot(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));
        AvailabilitySlot slot2 = new AvailabilitySlot(
                DayOfWeek.TUESDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));
        AvailabilitySlot slot3 = new AvailabilitySlot(
                DayOfWeek.WEDNESDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));
        AvailabilitySlot slot4 = new AvailabilitySlot(
                DayOfWeek.THURSDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));
        AvailabilitySlot slot5 = new AvailabilitySlot(
                DayOfWeek.FRIDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));

        DroneAvailability drone1 = new DroneAvailability(
                "1",
                List.of(slot1, slot2, slot3, slot4, slot5));

        // Drone 2: Available Saturday-Sunday, 10:00-15:00
        AvailabilitySlot slot6 = new AvailabilitySlot(
                DayOfWeek.SATURDAY,
                LocalTime.of(10, 0),
                LocalTime.of(15, 0));
        AvailabilitySlot slot7 = new AvailabilitySlot(
                DayOfWeek.SUNDAY,
                LocalTime.of(10, 0),
                LocalTime.of(15, 0));

        DroneAvailability drone2 = new DroneAvailability(
                "2",
                List.of(slot6, slot7));

        testAvailability = List.of(
                new DroneForServicePoint(1, List.of(drone1, drone2)));
    }

    // ==================
    // isDroneAvailable Tests
    // ==================

    @Test
    @DisplayName("Drone is available on correct day and time")
    void isDroneAvailable_CorrectDayAndTime_ReturnsTrue() {
        // Monday at 14:00
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(14, 0);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone is not available outside time slot")
    void isDroneAvailable_OutsideTimeSlot_ReturnsFalse() {
        // Monday at 18:00 (after 17:00 end time)
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(18, 0);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone is not available on wrong day")
    void isDroneAvailable_WrongDay_ReturnsFalse() {
        // Saturday at 14:00 (Drone 1 doesn't work weekends)
        LocalDate date = LocalDate.of(2025, 11, 29); // Saturday
        LocalTime time = LocalTime.of(14, 0);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone is available at start of time slot")
    void isDroneAvailable_AtStartTime_ReturnsTrue() {
        // Monday at exactly 9:00
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(9, 0);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone is available at end of time slot")
    void isDroneAvailable_AtEndTime_ReturnsTrue() {
        // Monday at exactly 17:00
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(17, 0);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone not found in availability data returns false")
    void isDroneAvailable_DroneNotFound_ReturnsFalse() {
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(14, 0);

        boolean result = service.isDroneAvailable("999", date, time, testAvailability);

        assertFalse(result);
    }

    @Test
    @DisplayName("Weekend drone is available on Saturday")
    void isDroneAvailable_WeekendDrone_ReturnsTrue() {
        // Saturday at 12:00
        LocalDate date = LocalDate.of(2025, 11, 29); // Saturday
        LocalTime time = LocalTime.of(12, 0);

        boolean result = service.isDroneAvailable("2", date, time, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone is not available before time slot starts")
    void isDroneAvailable_BeforeStartTime_ReturnsFalse() {
        // Monday at 8:59 (one minute before 9:00 start)
        LocalDate date = LocalDate.of(2025, 11, 24); // Monday
        LocalTime time = LocalTime.of(8, 59);

        boolean result = service.isDroneAvailable("1", date, time, testAvailability);

        assertFalse(result);
    }

    // ==================
    // isAvailableForAllDispatches Tests
    // ==================

    @Test
    @DisplayName("Drone is available for all dispatches")
    void isAvailableForAllDispatches_AllAvailable_ReturnsTrue() {
        MedDispatchRequirements req = new MedDispatchRequirements(2.0, false, false, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)),
                new MedDispatchRec(2, "2025-11-25", "14:00", req, new Position(-3.188, 55.943)),
                new MedDispatchRec(3, "2025-11-26", "11:30", req, new Position(-3.187, 55.944)));

        boolean result = service.isAvailableForAllDispatches("1", dispatches, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone is not available for one dispatch")
    void isAvailableForAllDispatches_OneUnavailable_ReturnsFalse() {
        MedDispatchRequirements req = new MedDispatchRequirements(2.0, false, false, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)), // Monday - OK
                new MedDispatchRec(2, "2025-11-29", "14:00", req, new Position(-3.188, 55.943))); // Saturday - NOT OK

        boolean result = service.isAvailableForAllDispatches("1", dispatches, testAvailability);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone is not available for dispatch outside time range")
    void isAvailableForAllDispatches_OutsideTimeRange_ReturnsFalse() {
        MedDispatchRequirements req = new MedDispatchRequirements(2.0, false, false, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)), // Monday 10:00 - OK
                new MedDispatchRec(2, "2025-11-24", "18:00", req, new Position(-3.188, 55.943))); // Monday 18:00 - NOT
                                                                                                    // OK

        boolean result = service.isAvailableForAllDispatches("1", dispatches, testAvailability);

        assertFalse(result);
    }

    @Test
    @DisplayName("Empty dispatch list returns true")
    void isAvailableForAllDispatches_EmptyList_ReturnsTrue() {
        List<MedDispatchRec> dispatches = List.of();

        boolean result = service.isAvailableForAllDispatches("1", dispatches, testAvailability);

        assertTrue(result);
    }

    @Test
    @DisplayName("Multiple dispatches on same day at different times")
    void isAvailableForAllDispatches_SameDayDifferentTimes_ReturnsTrue() {
        MedDispatchRequirements req = new MedDispatchRequirements(2.0, false, false, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "09:30", req, new Position(-3.186, 55.945)),
                new MedDispatchRec(2, "2025-11-24", "12:00", req, new Position(-3.188, 55.943)),
                new MedDispatchRec(3, "2025-11-24", "16:45", req, new Position(-3.187, 55.944)));

        boolean result = service.isAvailableForAllDispatches("1", dispatches, testAvailability);

        assertTrue(result);
    }
}
