package ilp.coursework2.kasia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for external service endpoints.
 * Retrieves the ILP service endpoint from environment variables.
 */
@Configuration
public class ServiceConfig {

    private static final String DEFAULT_ILP_ENDPOINT = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net";

    /**
     * Provides the ILP service endpoint URL.
     * Reads from ILP_ENDPOINT environment variable, falling back to default if not
     * set.
     *
     * @return the ILP service base URL
     */
    @Bean
    public String ilpEndpoint() {
        String endpoint = System.getenv("ILP_ENDPOINT");
        return endpoint != null ? endpoint : DEFAULT_ILP_ENDPOINT;
    }
}