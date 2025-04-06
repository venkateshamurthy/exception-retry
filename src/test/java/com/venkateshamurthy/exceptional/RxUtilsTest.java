package com.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@ExtensionMethod({RxSupplier.class, RxFunction.class, RxConsumer.class})
public class RxUtilsTest {
    private final int retryMaxAttempts = 5;
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    String greeting = "Hello World";

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .failAfterMaxAttempts(true)
                .intervalFunction(FIBONACCI.seconds(1,120))
                .retryExceptions(Exception.class, TimeoutException.class)
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
    }

    @Test
    void testRetry() throws Exception {
        CheckedBiConsumer<String, String> biConsumer = (k, v) -> {
            if (i.incrementAndGet() < retryMaxAttempts + 1)
                throw new Exception("Throwing callable-> "+i.get());
            log.info("Greeting: "+v);
        };
        CheckedFunction<String, String> chf = s->s;
        var chfrt = chf.retryCheckedFunction(rt);

        var rxBic = biConsumer.retryCheckedBiConsumer(rt)
                        .bulkheadCheckedBiConsumer(bh);
        var tryRun = Try.run(()->rxBic.accept("null", "null"));
        assertThrows(Exception.class, tryRun::get);

        i.set(0);
        Callable<String> call  = ()->{
            if (i.incrementAndGet()<retryMaxAttempts)
            throw new Exception("Throwing callable-> "+i.get());else return greeting;};
        assertEquals(greeting, assertDoesNotThrow(Retry.decorateCallable(rt, call)::call));
    }
}
