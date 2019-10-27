package io.helidon.examples.quickstart.mp;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Supplier;

@Readiness
@ApplicationScoped
public class StateHealthCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "app.state")
    private Supplier<String> state;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("state")
                .state("up".equalsIgnoreCase(state.get()))
                .build();
    }
}