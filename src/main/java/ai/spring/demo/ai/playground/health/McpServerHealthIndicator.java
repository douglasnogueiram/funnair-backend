package ai.spring.demo.ai.playground.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("mcpServer")
public class McpServerHealthIndicator implements HealthIndicator {

    private final String mcpServerUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public McpServerHealthIndicator(
            @Value("${spring.ai.mcp.client.sse.connections.travelServer.url:http://localhost:8085}") String mcpServerUrl) {
        this.mcpServerUrl = mcpServerUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Health health() {
        String url = mcpServerUrl + "/actuator/health";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "UP".equals(response.get("status"))) {
                return Health.up()
                        .withDetail("url", url)
                        .build();
            }
            return Health.down()
                    .withDetail("url", url)
                    .withDetail("response", response)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("url", url)
                    .withException(e)
                    .build();
        }
    }
}
