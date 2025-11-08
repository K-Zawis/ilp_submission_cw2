package ilp.coursework2.kasia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for REST client used to communicate with external services.
 */
@Configuration
public class RestClientConfig {

    /**
     * Provides a RestTemplate bean for making HTTP requests.
     *
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}