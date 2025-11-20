package ilp.coursework2.kasia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
}