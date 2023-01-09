package eu.mansipi.meta_server.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

//TODO: autocreate from rip plugin
@Configuration
public class CustomGlobalCircuitBreakerConfig {

  @Bean
  public CircuitBreakerConfig globalCircuitBreakerConfig() {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .slowCallRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(100000))
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .permittedNumberOfCallsInHalfOpenState(3)
        .minimumNumberOfCalls(10)
        //.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
        .slidingWindowSize(5)
       // .recordException(e -> INTERNAL_SERVER_ERROR
            //   .equals(getResponse().getStatus()))
            //.recordExceptions(IOException.class, TimeoutException.class)
            //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
            .build();
        return circuitBreakerConfig;
  }

}
