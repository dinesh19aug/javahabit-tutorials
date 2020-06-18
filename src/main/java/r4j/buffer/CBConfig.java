package r4j.buffer;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

public class CBConfig
{

    CircuitBreakerConfig circuitBreakerConfig;
    TimeLimiterConfig timeLimiterConfig;
    public CBConfig(){
        this.circuitBreakerConfig = createCircuitBreaker();
        this.timeLimiterConfig = createTimeLimiterConfig();
    }
    private CircuitBreakerConfig createCircuitBreaker(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                                                                        .failureRateThreshold(60)
                                                                        .waitDurationInOpenState(Duration.ofSeconds(10))
                                                                        .permittedNumberOfCallsInHalfOpenState(5)
                                                                        .slidingWindowSize(10)
                                                                        .recordExceptions(IOException.class, TimeoutException.class)
                                                                        .build();

        return circuitBreakerConfig;

    }

    private TimeLimiterConfig createTimeLimiterConfig(){
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(1)).build();
        return timeLimiterConfig;
    }

    protected CircuitBreaker getCircuitBreaker(String circuitBreakerName){
        CircuitBreaker circuitBreaker = CircuitBreaker.of(circuitBreakerName, circuitBreakerConfig);
        return circuitBreaker;
    }

    protected TimeLimiter getTimeLimiter(String name){
        TimeLimiter timeLimiter=TimeLimiter.of(name, createTimeLimiterConfig());
        return timeLimiter;
    }
}
