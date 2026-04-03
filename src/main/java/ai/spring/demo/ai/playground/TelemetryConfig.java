package ai.spring.demo.ai.playground;

import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import io.opentelemetry.sdk.trace.samplers.Sampler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arize.instrumentation.OITracer;
import com.arize.instrumentation.TraceConfig;
import com.arize.instrumentation.springAI.SpringAIInstrumentor;

import java.time.Duration;
import java.util.Map;

import static com.arize.semconv.trace.SemanticResourceAttributes.SEMRESATTRS_PROJECT_NAME;

@Configuration
public class TelemetryConfig {

    @Bean
    Resource otelResource(
            @Value("${spring.application.name:flight-booking-assistant}") String serviceName,
            @Value("${service.version:1.0.0}") String serviceVersion) {

        return Resource.getDefault().merge(Resource.create(Attributes.of(
                AttributeKey.stringKey("service.name"), serviceName,
                AttributeKey.stringKey(SEMRESATTRS_PROJECT_NAME), serviceName + "-project",
                AttributeKey.stringKey("service.version"), serviceVersion)));
    }

    @Bean
    OtlpGrpcSpanExporter otlpExporter(
            @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}") String endpoint,
            @Value("${phoenix.api.key:}") String apiKey) {

        var builder = OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .setTimeout(Duration.ofSeconds(10)); // ⚡ Aumentado para 10s

        if (!apiKey.isBlank()) {
            builder.setHeaders(() -> Map.of("Authorization", "Bearer " + apiKey));
        }
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    SdkTracerProvider sdkTracerProvider(
            Resource resource,
            OtlpGrpcSpanExporter otlp,
            @Value("${otel.traces.sampler.ratio:1.0}") double ratio) {

        return SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(Sampler.parentBased(Sampler.traceIdRatioBased(ratio)))
                .setSpanLimits(SpanLimits.builder()
                        .setMaxAttributeValueLength(16384)
                        .setMaxNumberOfAttributes(8192)
                        .setMaxNumberOfAttributesPerEvent(512)
                        .build())
                .addSpanProcessor(
                        BatchSpanProcessor.builder(otlp)
                                .setScheduleDelay(Duration.ofMillis(500)) // ⚡ Reduzido de 1000ms
                                .setMaxExportBatchSize(512) // 🆕 Novo
                                .setExporterTimeout(Duration.ofSeconds(10)) // 🆕 Novo
                                .build())
                .build(); // 🗑️ Removido LoggingSpanExporter
    }

    @Bean
    OITracer oiTracer(SdkTracerProvider provider) {
        return new OITracer(provider.get("ai.spring.demo.ai.playground"), TraceConfig.getDefault());
    }

    @Bean
    SpringAIInstrumentor springAIInstrumentor(OITracer tracer) {
        return new SpringAIInstrumentor(tracer);
    }

    // @Bean
    // ObservationRegistry observationRegistry(SpringAIInstrumentor instrumentor) {
    // var registry = ObservationRegistry.create();
    // registry.observationConfig().observationHandler(instrumentor);
    // return registry;
    // }

    @Bean
    ObservationRegistry observationRegistry(SpringAIInstrumentor instrumentor) {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(instrumentor);
        return registry;
    }
}

