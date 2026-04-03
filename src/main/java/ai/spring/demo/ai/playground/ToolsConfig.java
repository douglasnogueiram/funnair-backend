package ai.spring.demo.ai.playground;

import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.spring.demo.ai.playground.services.BookingTools;

@Configuration
public class ToolsConfig {
    @Bean
    public List<ToolCallback> findTools(BookingTools bookingTools) {
        return List.of(ToolCallbacks.from(bookingTools));
    }
}
