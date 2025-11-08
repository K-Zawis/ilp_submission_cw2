package ilp.coursework2.kasia.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ilp.coursework2.kasia.model.Drone;
import ilp.coursework2.kasia.model.DroneForServicePoint;

/**
 * Client service for interacting with the ILP REST API.
 * Handles all communication with the external ILP service.
 */
@Service
public class IlpService {

    private final RestTemplate restTemplate;
    private final String ilpEndpoint;

    public IlpService(RestTemplate restTemplate, String ilpEndpoint) {
        this.restTemplate = restTemplate;
        this.ilpEndpoint = ilpEndpoint;
    }

    /**
     * Constructs the full URL for an ILP API endpoint.
     *
     * @param path the API path
     * @return the complete URL
     */
    private String buildUrl(String path) {
        return ilpEndpoint + path;
    }

    /**
     * Fetches ALL drones from the ILP service.
     * 
     * GET /drones
     * 
     * @return list of all drones with their capabilities
     */
    public List<Drone> getAllDrones() {
        String url = buildUrl("/drones");

        ResponseEntity<Drone[]> response = restTemplate.getForEntity(
                url,
                Drone[].class);

        Drone[] drones = response.getBody();
        return drones != null ? Arrays.asList(drones) : List.of();
    }

    /**
     * Fetches drone availability information for all service points.
     * 
     * GET /drones-for-service-points
     * 
     * @return list of drone availability by service point
     */
    public List<DroneForServicePoint> getDronesForServicePoints() {
        String url = buildUrl("/drones-for-service-points");

        ResponseEntity<DroneForServicePoint[]> response = restTemplate.getForEntity(
                url,
                DroneForServicePoint[].class);

        DroneForServicePoint[] data = response.getBody();
        return data != null ? Arrays.asList(data) : List.of();
    }
}