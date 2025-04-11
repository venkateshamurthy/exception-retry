package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
//@ExtensionMethod(value = {SingularObservables.class}, suppressBaseMethods = false)
class SingularsTest {
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", RetryConfig.custom()
                .maxAttempts(50)
                .failAfterMaxAttempts(true)
                .intervalFunction(FIBONACCI.millis(10,1200))
                .retryExceptions(TimeoutException.class)
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
    }

    @Test
    void testRetry(){
        Single<String> s = Single.fromCallable(() -> {
            if (i.incrementAndGet() < 8) {
                log.error("Error:{}",i);
                throw new TimeoutException();
            }
            return ("Hello");
        });
        assertEquals("Hello", RxSingularObservables.retry(s, rt).blockingGet());

        s = Single.fromCallable(() -> {
            if (i.incrementAndGet() < 2) {
                log.error("Error:{}",i);
                throw new TimeoutException();
            }
            return ("Hello");
        });
        assertEquals("Hello", RxSingularObservables.rateLimit(s, rl).blockingGet());
    }

    @Test
    void testRateLimit(){
        Single<String> s = Single.fromCallable(() -> {
            TimeUnit.MILLISECONDS.sleep(1);
            return ("Hello-" + i.incrementAndGet());
        });

        final Flowable<Integer> range = Flowable.range(1, 1000);
        range.parallel().runOn(Schedulers.computation())
                .map(j -> s.blockingGet())
                //.sequential()
                .doOnComplete(() -> System.out.println("DONE"))
                .doOnNext(System.out::println)
                .doOnError(System.err::println);
    }
}
