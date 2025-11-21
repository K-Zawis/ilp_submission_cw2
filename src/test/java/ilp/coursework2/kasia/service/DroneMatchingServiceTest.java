package ilp.coursework2.kasia.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ilp.coursework2.kasia.model.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Drone Matching Service Tests")
class DroneMatchingServiceTest {

    @Mock
    private DroneAvailabilityService availabilityService;

    private DroneMatchingService service;
    private List<Drone> testDrones;
    private List<DroneForServicePoint> testAvailability;

    @BeforeEach
    void setUp() {
        service = new DroneMatchingService(availabilityService);

        // Create test drones with various capabilities
        // DroneCapability(Boolean cooling, Boolean heating, double capacity, int maxMoves, double costPerMove, double
        // costInitial, double costFinal)
        DroneCapability capability1 = new DroneCapability(true, false, 5.0, 1500, 0.01, 10.0, 5.0);
        DroneCapability capability2 = new DroneCapability(false, true, 10.0, 1500, 0.01, 10.0, 5.0);
        DroneCapability capability3 = new DroneCapability(false, false, 3.0, 1500, 0.01, 10.0, 5.0);
        DroneCapability capability4 = new DroneCapability(true, true, 8.0, 1500, 0.01, 10.0, 5.0);

        testDrones = List.of(
                new Drone("1", "Drone1", capability1),
                new Drone("2", "Drone2", capability2),
                new Drone("3", "Drone3", capability3),
                new Drone("4", "Drone4", capability4));

        // Create availability data
        AvailabilitySlot slot = new AvailabilitySlot(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));
        DroneAvailability droneAvail = new DroneAvailability("1", List.of(slot));
        testAvailability = List.of(new DroneForServicePoint(1, List.of(droneAvail)));
    }

    // ==================
    // meetsRequirements Tests
    // ==================

    @Test
    @DisplayName("Drone meets basic capacity requirement")
    void meetsRequirements_BasicCapacity_ReturnsTrue() {
        Drone drone = testDrones.get(0); // Capacity 5.0, cooling
        MedDispatchRequirements req = new MedDispatchRequirements(4.0, null, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone does not meet capacity requirement")
    void meetsRequirements_InsufficientCapacity_ReturnsFalse() {
        Drone drone = testDrones.get(2); // Capacity 3.0
        MedDispatchRequirements req = new MedDispatchRequirements(5.0, null, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone meets cooling requirement")
    void meetsRequirements_CoolingRequired_ReturnsTrue() {
        Drone drone = testDrones.get(0); // Has cooling
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, true, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone does not meet cooling requirement")
    void meetsRequirements_CoolingNotAvailable_ReturnsFalse() {
        Drone drone = testDrones.get(1); // No cooling
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, true, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone meets heating requirement")
    void meetsRequirements_HeatingRequired_ReturnsTrue() {
        Drone drone = testDrones.get(1); // Has heating
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, null, true, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone does not meet heating requirement")
    void meetsRequirements_HeatingNotAvailable_ReturnsFalse() {
        Drone drone = testDrones.get(0); // No heating
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, null, true, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone meets both cooling and heating requirements")
    void meetsRequirements_BothCoolingAndHeating_ReturnsTrue() {
        Drone drone = testDrones.get(3); // Has both
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, true, true, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Drone with null capability returns false")
    void meetsRequirements_NullCapability_ReturnsFalse() {
        Drone drone = new Drone("5", "Drone5", null);
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, null, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertFalse(result);
    }

    @Test
    @DisplayName("Drone meets exact capacity requirement")
    void meetsRequirements_ExactCapacity_ReturnsTrue() {
        Drone drone = testDrones.get(0); // Capacity 5.0
        MedDispatchRequirements req = new MedDispatchRequirements(5.0, null, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    @Test
    @DisplayName("Cooling requirement false does not require cooling")
    void meetsRequirements_CoolingFalse_ReturnsTrue() {
        Drone drone = testDrones.get(1); // No cooling
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, false, null, null);
        MedDispatchRec dispatch = new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945));

        boolean result = service.meetsRequirements(drone, dispatch);

        assertTrue(result);
    }

    // ==================
    // findMatchingDrones Tests
    // ==================

    @Test
    @DisplayName("Find drones matching single dispatch")
    void findMatchingDrones_SingleDispatch_ReturnsMatching() {
        MedDispatchRequirements req = new MedDispatchRequirements(4.0, true, null, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)));

        // Mock availability service
        when(availabilityService.isAvailableForAllDispatches(eq("1"), anyList(), anyList())).thenReturn(true);
        when(availabilityService.isAvailableForAllDispatches(eq("4"), anyList(), anyList())).thenReturn(true);

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        // Drones 1 and 4 have cooling and capacity >= 4.0
        assertEquals(2, result.size());
        assertTrue(result.contains("1"));
        assertTrue(result.contains("4"));
    }

    @Test
    @DisplayName("Find drones matching multiple dispatches")
    void findMatchingDrones_MultipleDispatches_ReturnsMatching() {
        MedDispatchRequirements req1 = new MedDispatchRequirements(3.0, false, null, null);
        MedDispatchRequirements req2 = new MedDispatchRequirements(4.0, false, null, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req1, new Position(-3.186, 55.945)),
                new MedDispatchRec(2, "2025-11-25", "14:00", req2, new Position(-3.188, 55.943)));

        // Mock availability service - only drone 1, 2, and 4 are available
        when(availabilityService.isAvailableForAllDispatches(eq("1"), anyList(), anyList())).thenReturn(true);
        when(availabilityService.isAvailableForAllDispatches(eq("2"), anyList(), anyList())).thenReturn(true);
        when(availabilityService.isAvailableForAllDispatches(eq("4"), anyList(), anyList())).thenReturn(true);

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        // Drones 1, 2, and 4 have capacity >= 4.0
        assertEquals(3, result.size());
        assertTrue(result.contains("1"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("4"));
    }

    @Test
    @DisplayName("No drones match when none are available")
    void findMatchingDrones_NoneAvailable_ReturnsEmpty() {
        MedDispatchRequirements req = new MedDispatchRequirements(3.0, null, null, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)));

        // Mock availability service - all unavailable
        when(availabilityService.isAvailableForAllDispatches(anyString(), anyList(), anyList())).thenReturn(false);

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("No drones match when requirements too high")
    void findMatchingDrones_RequirementsTooHigh_ReturnsEmpty() {
        MedDispatchRequirements req = new MedDispatchRequirements(15.0, null, null, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)));

        // No need to mock availability service - none of the drones meet capacity requirement

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Empty dispatch list returns empty result")
    void findMatchingDrones_EmptyDispatches_ReturnsEmpty() {
        List<MedDispatchRec> dispatches = List.of();

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Null dispatch list returns empty result")
    void findMatchingDrones_NullDispatches_ReturnsEmpty() {
        List<String> result = service.findMatchingDrones(null, testDrones, testAvailability);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Only one drone matches heating requirement")
    void findMatchingDrones_HeatingRequired_ReturnsCorrect() {
        MedDispatchRequirements req = new MedDispatchRequirements(5.0, null, true, null);
        List<MedDispatchRec> dispatches = List.of(
                new MedDispatchRec(1, "2025-11-24", "10:00", req, new Position(-3.186, 55.945)));

        when(availabilityService.isAvailableForAllDispatches(eq("2"), anyList(), anyList())).thenReturn(true);
        when(availabilityService.isAvailableForAllDispatches(eq("4"), anyList(), anyList())).thenReturn(true);

        List<String> result = service.findMatchingDrones(dispatches, testDrones, testAvailability);

        // Only drones 2 and 4 have heating and capacity >= 5.0
        assertEquals(2, result.size());
        assertTrue(result.contains("2"));
        assertTrue(result.contains("4"));
    }
}
