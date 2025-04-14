package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.venkateshamurthy.exceptional.pojo.HelloWorldService;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static io.github.venkateshamurthy.exceptional.RxCallable.toCallable;
import static io.github.venkateshamurthy.exceptional.RxFunction.toFunction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@Slf4j
@ExtensionMethod(value = {RxSingularObservables.class, RxCallable.class}, suppressBaseMethods = false)
class RxSingularObservablesTest {
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    HelloWorldService service;
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
        service = new HelloWorldService() {};
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
    void testRetryObservable(){
        Observable<String> s = Observable.fromCallable(() -> {
            if (i.incrementAndGet() < 8) {
                log.error("Error:{}",i);
                throw new TimeoutException();
            }
            return ("Hello");
        });
        assertEquals("Hello", RxSingularObservables.retry(s, rt).blockingSingle());

        s = Observable.fromCallable(() -> {
            if (i.incrementAndGet() < 2) {
                log.error("Error:{}",i);
                throw new TimeoutException();
            }
            return ("Hello");
        });
        assertEquals("Hello", RxSingularObservables.rateLimit(s, rl).blockingSingle());
    }

    @Test
    void testRateLimit(){
        Single<String> s = Single.fromCallable(() -> {
            TimeUnit.MILLISECONDS.sleep(1);
            return ("Hello-" + i.incrementAndGet());
        });

        final Flowable<Integer> range = Flowable.range(1, 1000);
        range.parallel().runOn(Schedulers.computation())
                .map(j -> s.rateLimit(rl).blockingGet())
                //.sequential()
                .doOnComplete(() -> System.out.println("DONE"))
                .doOnNext(System.out::println)
                .doOnError(System.err::println);
    }


    @Test
    void testRateLimitObservable(){
        Observable<String> s = Observable.fromCallable(() -> {
            TimeUnit.MILLISECONDS.sleep(1);
            return ("Hello-" + i.incrementAndGet());
        });

        final Flowable<Integer> range = Flowable.range(1, 1000);
        range.parallel().runOn(Schedulers.computation())
                .map(j -> s.rateLimit(rl).blockingSingle())
                //.sequential()
                .doOnComplete(() -> System.out.println("DONE"))
                .doOnNext(System.out::println)
                .doOnError(System.err::println);


    }

    @Test
    public void circuitBreakSingle() {

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        Single<String> single = Single.fromCallable(service::returnHelloWorldWithException)
                .circuitBreak(circuitBreaker);

        service.revokeException();
        Try<String> tryer= Try.ofCallable(single::blockingGet);
        assertThat(tryer.isSuccess()).isTrue();
        assertThat(tryer.get()).isEqualTo(HelloWorldService.greeting);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);

        service.throwException();
        tryer= Try.ofCallable(single::blockingGet);
        assertThat(tryer.isFailure()).isTrue();
        assertThat(tryer.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(2);
        assertThat(metrics.getNumberOfFailedCalls()).isOne();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isOne();
    }


    @Test
    public void circuitBreakObservable() {

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        Observable<String> single = Observable.fromCallable(service::returnHelloWorldWithException)
                .circuitBreak(circuitBreaker);

        service.revokeException();
        Try<String> tryer= Try.ofCallable(single::blockingSingle);
        assertThat(tryer.isSuccess()).isTrue();
        assertThat(tryer.get()).isEqualTo(HelloWorldService.greeting);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);

        service.throwException();
        tryer= Try.ofCallable(single::blockingSingle);
        assertThat(tryer.isFailure()).isTrue();
        assertThat(tryer.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(2);
        assertThat(metrics.getNumberOfFailedCalls()).isOne();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isOne();
    }
}
