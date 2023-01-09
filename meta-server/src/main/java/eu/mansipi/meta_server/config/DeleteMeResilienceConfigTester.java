package eu.mansipi.meta_server.config;

import feign.FeignException;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

//TODO: delete me. Resilience features are best added by the caller by wrapping the rest call in an annotated method.
@Configuration
public class DeleteMeResilienceConfigTester {

 /* @Bean
  public RetryConfig globalRetryConfig() { //TODO: Make this no-op config the default global such as to force users to add a Retry policy incase they want one.
    return RetryConfig.custom()
        .maxAttempts(2) // //Let's configure a noop global config since Retry is really something we can't decorate calls with automatically - the calling jar's developer must him-/herself configure a Retry policy.
        //.waitDuration(Duration.ofMillis(1000))
        //.retryOnResult(response -> 1==2) //Let's configure a noop global config since Retry is really something we can't decorate calls with automatically - the calling jar's developer must him-/herself configure a Retry policy.
        //.retryOnException(e -> e instanceof FeignException.InternalServerError)
        //.retryExceptions(FeignException.InternalServerError.class)
        //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
        //.failAfterMaxAttempts(false)
        .build();
  }*/

 /* @Bean
  public RetryConfig retryConfig4() { //TODO: Make this no-op config the default global such as to force users to add a Retry policy incase they want one.
    return RetryConfig.custom()
        .maxAttempts(4) // //Let's configure a noop global config since Retry is really something we can't decorate calls with automatically - the calling jar's developer must him-/herself configure a Retry policy.
        //.waitDuration(Duration.ofMillis(1000))
        //.retryOnResult(response -> 1==2) //Let's configure a noop global config since Retry is really something we can't decorate calls with automatically - the calling jar's developer must him-/herself configure a Retry policy.
        //.retryOnException(e -> e instanceof FeignException.InternalServerError)
        //.retryExceptions(FeignException.InternalServerError.class)
        //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
        //.failAfterMaxAttempts(false)
        .build();
  }*/



}
