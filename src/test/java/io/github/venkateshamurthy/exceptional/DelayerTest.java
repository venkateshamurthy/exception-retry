package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.venkateshamurthy.exceptional.Delayer.*;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@ExtensionMethod({RxSupplier.class, RxFunction.class, RxConsumer.class})
class DelayerTest {
    private final int retryMaxAttempts = 3;
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    String greeting = "Hello World";
    RetryConfig.Builder retryCfgBuilder = RetryConfig.custom()
            .maxAttempts(retryMaxAttempts)
            .failAfterMaxAttempts(true)
            .intervalFunction(FIBONACCI.millis(1,300))
            .retryExceptions(Exception.class, TimeoutException.class);

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", retryCfgBuilder.intervalFunction(
                FIBONACCI.millis(1,300))
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
    }

    @ParameterizedTest(name = "Running CheckArgs for {0}")
    @EnumSource(Delayer.class)
    void testCheckArgs(Delayer delayer) {
        assertThrows(IllegalArgumentException.class, ()-> delayer.millis(1L,0L));
        assertThrows(IllegalArgumentException.class, ()-> delayer.seconds(1L,-10L));
        assertThrows(IllegalArgumentException.class, ()-> delayer.minutes(-1L,0L));
        assertThrows(IllegalArgumentException.class, ()-> delayer.hours(10L,2L));
    }

    @ParameterizedTest( name = "Running {0} with initial:{1}, max:{2} and unit:{3}")
    @ArgumentsSource(DelayArgSource.class)
    void testRetryAllDurations(Delayer delayer, Number initial, Number maxDelay, TimeUnit unit) throws Exception {
        final IntervalFunction interval = switch (unit) {
            case MILLISECONDS-> delayer.millis (initial.longValue(),  maxDelay.longValue());
            case SECONDS     -> delayer.seconds(initial.longValue(),  maxDelay.longValue());
            case MINUTES     -> delayer.minutes(initial.doubleValue(),maxDelay.doubleValue());
            case HOURS       -> delayer.hours  (initial.doubleValue(),maxDelay.doubleValue());
            default -> throw new IllegalStateException("Unexpected time unit:: " + unit);
        };
        rt = Retry.of("rt", retryCfgBuilder.intervalFunction(interval).build());

        i.set(0);
        Callable<String> call  = ()->{
            if (i.incrementAndGet()<retryMaxAttempts)
                throw new Exception("Throwing callable-> "+i.get());
            else return greeting;
        };
        assertEquals(greeting, assertDoesNotThrow(Retry.decorateCallable(rt, call)::call));
    }

    private static class DelayArgSource implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    arguments(FIBONACCI,          1,      120,   MILLISECONDS),//millis
                    arguments(FIBONACCI,          1L,     3,     SECONDS), //seconds
                    arguments(FIBONACCI,          0.01,   0.02,  MINUTES), //mins
                    arguments(FIBONACCI,          0.0001, 0.0005,HOURS),//hours

                    arguments(EXPONENTIAL,        1L,     120,   MILLISECONDS),//millis
                    arguments(EXPONENTIAL,        1,      3,     SECONDS), //seconds
                    arguments(EXPONENTIAL,        0.01,   0.02,  MINUTES), //mins
                    arguments(EXPONENTIAL,        0.0001, 0.0005,HOURS),//hours

                    arguments(EXPONENTIAL_JITTER, 1,      120,   MILLISECONDS),//millis
                    arguments(EXPONENTIAL_JITTER, 1L,     3,     SECONDS), //seconds
                    arguments(EXPONENTIAL_JITTER, 0.01,   0.02,  MINUTES), //mins
                    arguments(EXPONENTIAL_JITTER, 0.0001, 0.0005,HOURS),//hours

                    arguments(LINEAR,             1,      120,   MILLISECONDS),//millis
                    arguments(LINEAR,             1L,     3,     SECONDS), //seconds
                    arguments(LINEAR,             0.01,   0.02,  MINUTES), //mins
                    arguments(LINEAR,             0.0001, 0.0005,HOURS),//hours

                    arguments(DEFAULT,            1,      120,   MILLISECONDS),//millis
                    arguments(DEFAULT,            1L,     3,     SECONDS), //seconds
                    arguments(DEFAULT,            0.01,   0.02,  MINUTES), //mins
                    arguments(DEFAULT,            0.0001, 0.0005,HOURS),//hours

                    arguments(DEFAULT_JITTER,     1,      120,   MILLISECONDS),//millis
                    arguments(DEFAULT_JITTER,     1,      2,     SECONDS), //seconds
                    arguments(DEFAULT_JITTER,     0.01,   0.02,  MINUTES), //mins
                    arguments(DEFAULT_JITTER,     0.0001, 0.0005,HOURS),//hours

                    arguments(LINEAR_JITTER,      1,      120,   MILLISECONDS),//millis
                    arguments(LINEAR_JITTER,      1L,     2,     SECONDS), //seconds
                    arguments(LINEAR_JITTER,      0.01,   0.02,  MINUTES), //mins
                    arguments(LINEAR_JITTER,      0.0001, 0.0005,HOURS),//hours

                    arguments(LOGARITHMIC,        1,      120,   MILLISECONDS),//millis
                    arguments(LOGARITHMIC,        1L,     2,     SECONDS), //seconds
                    arguments(LOGARITHMIC,        0.01,   0.02,  MINUTES), //mins
                    arguments(LOGARITHMIC,        0.0001, 0.0005,HOURS)//hours
            );
        }
    }
}
