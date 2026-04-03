package ai.spring.demo.ai.playground.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("chroma")
public class ChromaHealthIndicator implements HealthIndicator {

    private final String chromaHost;
    private final int chromaPort;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChromaHealthIndicator(
            @Value("${spring.ai.vectorstore.chroma.client.host:http://localhost}") String chromaHost,
            @Value("${spring.ai.vectorstore.chroma.client.port:8000}") int chromaPort) {
        this.chromaHost = chromaHost;
        this.chromaPort = chromaPort;
    }

    @Override
    public Health health() {
        String url = chromaHost + ":" + chromaPort + "/api/v2/heartbeat";
        try {
            restTemplate.getForObject(url, String.class);
            return Health.up()
                    .withDetail("url", url)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("url", url)
                    .withException(e)
                    .build();
        }
    }
}
