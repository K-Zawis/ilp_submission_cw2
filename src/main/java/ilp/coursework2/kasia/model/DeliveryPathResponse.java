package ilp.coursework2.kasia.model;

import java.util.List;

/**
 * Response for calcDeliveryPath endpoint.
 * Contains the complete delivery plan with costs and drone paths.
 */
public record DeliveryPathResponse(
        double totalCost,
        int totalMoves,
        List<DronePath> dronePaths) {
}