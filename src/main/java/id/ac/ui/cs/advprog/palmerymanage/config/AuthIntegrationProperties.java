package id.ac.ui.cs.advprog.palmerymanage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.integration")
public class AuthIntegrationProperties {

    private String baseUrl = "http://localhost:8080";
    private String serviceClientId = "palmery-internal-service";
    private String serviceClientSecret = "replace-with-service-client-secret";
    private boolean enabled = true;
}
