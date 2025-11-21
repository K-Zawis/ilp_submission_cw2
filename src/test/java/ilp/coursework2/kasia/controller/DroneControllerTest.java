package ilp.coursework2.kasia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ilp.coursework2.kasia.interfaces.IDroneService;
import ilp.coursework2.kasia.interfaces.IPositionValidator;
import ilp.coursework2.kasia.interfaces.IRegionValidator;
import ilp.coursework2.kasia.model.Delivery;
import ilp.coursework2.kasia.model.DeliveryPathResponse;
import ilp.coursework2.kasia.model.Drone;
import ilp.coursework2.kasia.model.DroneCapability;
import ilp.coursework2.kasia.model.DronePath;
import ilp.coursework2.kasia.model.Position;
import ilp.coursework2.kasia.service.DeliveryPlannerService;
import ilp.coursework2.kasia.service.DroneMatchingService;
import ilp.coursework2.kasia.service.DroneQueryService;
import ilp.coursework2.kasia.service.GeoJsonService;
import ilp.coursework2.kasia.service.IlpService;

@WebMvcTest(DroneController.class)
@DisplayName("Drone Controller Tests")
class DroneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private IDroneService droneService;
    @SuppressWarnings("removal")
    @MockBean
    private IPositionValidator positionValidator;
    @SuppressWarnings("removal")
    @MockBean
    private IRegionValidator regionValidator;
    @SuppressWarnings("removal")
    @MockBean
    private IlpService ilpService;
    @SuppressWarnings("removal")
    @MockBean
    private DroneQueryService droneQueryService;
    @SuppressWarnings("removal")
    @MockBean
    private DroneMatchingService droneMatchingService;
    @SuppressWarnings("removal")
    @MockBean
    private DeliveryPlannerService deliveryPlannerService;
    @SuppressWarnings("removal")
    @MockBean
    private GeoJsonService geoJsonService;

    @Test
    @DisplayName("GET /uid returns student ID")
    void getUid() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2340338"));
    }

    @Test
    @DisplayName("POST /distanceTo returns correct distance for valid input")
    void distanceToValidInput() throws Exception {
        // Given
        when(positionValidator.isValidPositionPair(any())).thenReturn(true);
        when(droneService.calculateDistance(any())).thenReturn(0.003616);

        // When & Then
        mockMvc.perform(post("/api/v1/distanceTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position1": {"lng": -3.192473, "lat": 55.946233},
                            "position2": {"lng": -3.192473, "lat": 55.942617}
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("0.003616"));
    }

    @Test
    @DisplayName("POST /distanceTo returns 400 for invalid input")
    void distanceToInvalidInput() throws Exception {
        // Given
        when(positionValidator.isValidPositionPair(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/distanceTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position1": {"lng": -3.192473}
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /isCloseTo returns true when positions are close")
    void isCloseToReturnsTrue() throws Exception {
        // Given
        when(positionValidator.isValidPositionPair(any())).thenReturn(true);
        when(droneService.isCloseTo(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/isCloseTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position1": {"lng": -3.192473, "lat": 55.946233},
                            "position2": {"lng": -3.192473, "lat": 55.946234}
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /isCloseTo returns false when positions are far")
    void isCloseToReturnsFalse() throws Exception {
        // Given
        when(positionValidator.isValidPositionPair(any())).thenReturn(true);
        when(droneService.isCloseTo(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/isCloseTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position1": {"lng": -3.192473, "lat": 55.946233},
                            "position2": {"lng": -3.192473, "lat": 55.942617}
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /nextPosition returns new position for valid input")
    void nextPositionValidInput() throws Exception {
        // Given
        when(positionValidator.isValidNextPositionRequest(any())).thenReturn(true);
        when(droneService.calculateNextPosition(any()))
                .thenReturn(new Position(0.00015, 0.0));

        // When & Then
        mockMvc.perform(post("/api/v1/nextPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "start": {"lng": 0.0, "lat": 0.0},
                            "angle": 0.0
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(0.00015))
                .andExpect(jsonPath("$.lat").value(0.0));
    }

    @Test
    @DisplayName("POST /isInRegion returns true when point is inside region")
    void isInRegionReturnsTrue() throws Exception {
        // Given
        when(regionValidator.isValidRegionRequest(any())).thenReturn(true);
        when(droneService.isPointInRegion(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/isInRegion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position": {"lng": -3.188, "lat": 55.944},
                            "region": {
                                "name": "central",
                                "vertices": [
                                    {"lng": -3.192473, "lat": 55.946233},
                                    {"lng": -3.192473, "lat": 55.942617},
                                    {"lng": -3.184319, "lat": 55.942617},
                                    {"lng": -3.184319, "lat": 55.946233},
                                    {"lng": -3.192473, "lat": 55.946233}
                                ]
                            }
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /isInRegion returns 400 for invalid region request")
    void isInRegionInvalidInput() throws Exception {
        // Given
        when(regionValidator.isValidRegionRequest(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/isInRegion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "position": {"lng": -3.188}
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ==================
    // CW2 Static Query Tests
    // ==================

    @Test
    @DisplayName("GET /dronesWithCooling/true returns drones with cooling")
    void dronesWithCooling_True_ReturnsDronesWithCooling() throws Exception {
        // Given
        DroneCapability cap1 = new DroneCapability(true, false, 5.0, 1500, 0.01, 10.0, 5.0);
        DroneCapability cap2 = new DroneCapability(false, false, 5.0, 1500, 0.01, 10.0, 5.0);
        List<Drone> drones = List.of(
                new Drone("1", "Drone1", cap1),
                new Drone("2", "Drone2", cap2));

        when(ilpService.getAllDrones()).thenReturn(drones);

        // When & Then
        mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("1"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /droneDetails/1 returns drone details")
    void droneDetails_ValidId_ReturnsDroneDetails() throws Exception {
        // Given
        DroneCapability cap = new DroneCapability(true, false, 5.0, 1500, 0.01, 10.0, 5.0);
        Drone drone = new Drone("1", "Drone1", cap);
        when(ilpService.getAllDrones()).thenReturn(List.of(drone));

        // When & Then
        mockMvc.perform(get("/api/v1/droneDetails/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Drone1"));
    }

    @Test
    @DisplayName("GET /droneDetails/999 returns 404")
    void droneDetails_InvalidId_Returns404() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/droneDetails/999"))
                .andExpect(status().isNotFound());
    }

    // ==================
    // CW2 Dynamic Query Tests
    // ==================

    @Test
    @DisplayName("GET /queryAsPath/capacity/8 returns matching drones")
    void queryAsPath_ValidQuery_ReturnsMatchingDrones() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());
        when(droneQueryService.queryByAttribute(anyList(), any(), any()))
                .thenReturn(List.of("1", "2"));

        // When & Then
        mockMvc.perform(get("/api/v1/queryAsPath/capacity/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("1"))
                .andExpect(jsonPath("$[1]").value("2"));
    }

    @Test
    @DisplayName("POST /query with multiple criteria returns matching drones")
    void query_MultipleCriteria_ReturnsMatchingDrones() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());
        when(droneQueryService.queryByMultipleCriteria(anyList(), anyList()))
                .thenReturn(List.of("1"));

        // When & Then
        mockMvc.perform(post("/api/v1/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {"attribute": "capacity", "operator": ">", "value": "5"},
                            {"attribute": "cooling", "operator": "=", "value": "true"}
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("1"));
    }

    // ==================
    // CW2 Availability Tests
    // ==================

    @Test
    @DisplayName("POST /queryAvailableDrones returns available drones")
    void queryAvailableDrones_ValidDispatches_ReturnsAvailable() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());
        when(ilpService.getDronesForServicePoints()).thenReturn(List.of());
        when(droneMatchingService.findMatchingDrones(anyList(), anyList(), anyList()))
                .thenReturn(List.of("1", "2"));

        // When & Then
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "id": 1,
                                "date": "2025-11-20",
                                "time": "14:00",
                                "requirements": {"capacity": 2.0},
                                "delivery": {"lng": -3.186, "lat": 55.945}
                            }
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("1"))
                .andExpect(jsonPath("$[1]").value("2"));
    }

    // ==================
    // CW2 Delivery Planning Tests
    // ==================

    @Test
    @DisplayName("POST /calcDeliveryPath returns delivery path response")
    void calcDeliveryPath_ValidDispatches_ReturnsPath() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());
        when(ilpService.getServicePoints()).thenReturn(List.of());
        when(ilpService.getDronesForServicePoints()).thenReturn(List.of());
        when(ilpService.getRestrictedAreas()).thenReturn(List.of());

        DeliveryPathResponse mockResponse = new DeliveryPathResponse(15.5, 10, List.of());
        when(deliveryPlannerService.planDeliveries(anyList(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "id": 1,
                                "date": "2025-11-20",
                                "time": "14:00",
                                "requirements": {"capacity": 2.0},
                                "delivery": {"lng": -3.186, "lat": 55.945}
                            }
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(15.5))
                .andExpect(jsonPath("$.totalMoves").value(10));
    }

    @Test
    @DisplayName("POST /calcDeliveryPathAsGeoJson returns GeoJSON string")
    void calcDeliveryPathAsGeoJson_ValidDispatches_ReturnsGeoJson() throws Exception {
        // Given
        when(ilpService.getAllDrones()).thenReturn(List.of());
        when(ilpService.getServicePoints()).thenReturn(List.of());
        when(ilpService.getDronesForServicePoints()).thenReturn(List.of());
        when(ilpService.getRestrictedAreas()).thenReturn(List.of());

        // Create a mock drone path with actual deliveries (not empty)
        List<Position> flightPath = List.of(new Position(-3.186, 55.945), new Position(-3.187, 55.946));
        Delivery delivery = new Delivery(1, flightPath);
        DronePath dronePath = new DronePath("1", List.of(delivery));

        DeliveryPathResponse mockResponse = new DeliveryPathResponse(15.5, 10, List.of(dronePath));
        when(deliveryPlannerService.planDeliveries(anyList(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(mockResponse);
        when(geoJsonService.convertDronePathToGeoJson(any(DronePath.class)))
                .thenReturn("{\"type\":\"LineString\"}");

        // When & Then
        mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "id": 1,
                                "date": "2025-11-20",
                                "time": "14:00",
                                "requirements": {"capacity": 2.0},
                                "delivery": {"lng": -3.186, "lat": 55.945}
                            }
                        ]
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"type\":\"LineString\"}"));
    }
}