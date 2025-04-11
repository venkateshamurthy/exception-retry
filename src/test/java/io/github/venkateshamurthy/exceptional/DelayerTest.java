package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.venkateshamurthy.exceptional.Delayer.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@ExtensionMethod({RxSupplier.class, RxFunction.class, RxConsumer.class})
class DelayerTest {
    private final int retryMaxAttempts = 5;
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    String greeting = "Hello World";
    RetryConfig.Builder retryCfgBuilder = RetryConfig.custom()
            .maxAttempts(retryMaxAttempts)
            .failAfterMaxAttempts(true)
            .intervalFunction(FIBONACCI.millis(10,1200))
            .retryExceptions(Exception.class, TimeoutException.class);

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", retryCfgBuilder.intervalFunction(
                FIBONACCI.millis(10,1200))
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
    }
    @ParameterizedTest( name = "Running {0} with {1}, and {2}")
    @ArgumentsSource(DelayArgSource.class)
    void testRetryAllDurations(Delayer delayer, long inital, long maxDelay) throws Exception {
        rt = Retry.of("rt", retryCfgBuilder.intervalFunction(
                        delayer.millis(inital,maxDelay))
                .build());

        i.set(0);
        Callable<String> call  = ()->{
            if (i.incrementAndGet()<retryMaxAttempts)
                throw new Exception("Throwing callable-> "+i.get());
            else return greeting;};
        assertEquals(greeting,
                assertDoesNotThrow(Retry.decorateCallable(rt, call)::call));
    }

    private static class DelayArgSource implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    arguments(FIBONACCI, 10L, 1200L),//millis
                    arguments(FIBONACCI, 1L, 120), //seconds
                    arguments(FIBONACCI, 1L, 3L), //mins
                    arguments(FIBONACCI, 1L, 1L),//hours
                    arguments(EXPONENTIAL, 10L, 1200L),//millis
                    arguments(EXPONENTIAL, 1L, 120), //seconds
                    arguments(EXPONENTIAL, 1L, 3L), //mins
                    arguments(EXPONENTIAL, 1L, 1L),//hours
                    arguments(LINEAR, 10L, 1200L),//millis
                    arguments(LINEAR, 1L, 120), //seconds
                    arguments(LINEAR, 1L, 3L), //mins
                    arguments(LINEAR, 1L, 1L),//hours
                    arguments(DEFAULT, 10L, 1200L),//millis
                    arguments(DEFAULT, 1L, 120), //seconds
                    arguments(DEFAULT, 1L, 3L), //mins
                    arguments(DEFAULT, 1L, 1L),//hours
                    arguments(DEFAULT_JITTER, 10L, 1200L),//millis
                    arguments(DEFAULT_JITTER, 1L, 120), //seconds
                    arguments(DEFAULT_JITTER, 1L, 3L), //mins
                    arguments(DEFAULT_JITTER, 1L, 1L),//hours
                    arguments(LINEAR_JITTER, 10L, 1200L),//millis
                    arguments(LINEAR_JITTER, 1L, 120), //seconds
                    arguments(LINEAR_JITTER, 1L, 3L), //mins
                    arguments(LINEAR_JITTER, 1L, 1L)//hours
            );
        }
    }

}
