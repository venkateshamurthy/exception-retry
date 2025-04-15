package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.venkateshamurthy.exceptional.pojo.HelloWorldService;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static io.github.venkateshamurthy.exceptional.RxConsumer.*;
import static io.github.venkateshamurthy.exceptional.RxSupplier.toSupplier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearAllCaches;
import static org.mockito.Mockito.when;

@Slf4j
@ExtensionMethod(RxConsumer.class)
class RxConsumerTest {
    private static final int LIMIT = 50;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REFRESH_PERIOD = Duration.ofNanos(500);
    private final int retryMaxAttempts = 5;
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    String greeting = "Hello World";
    static HelloWorldService helloWorldService;

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .failAfterMaxAttempts(true)
                .intervalFunction(FIBONACCI.millis(10,1200))
                .retryExceptions(Exception.class,
                        TimeoutException.class,
                        NullPointerException.class,
                        IllegalAccessException.class,
                        IllegalStateException.class)
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
        clearAllCaches();
        helloWorldService = new HelloWorldService() {};
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(TIMEOUT)
                .limitRefreshPeriod(REFRESH_PERIOD)
                .limitForPeriod(LIMIT)
                .build();
        rl= Mockito.spy(RateLimiter.of("RateLimiterDefault", config));
        given(rl.getRateLimiterConfig()).willReturn(config);
    }

    @AfterEach
    void after() {
        i.set(0);
        helloWorldService.revokeException();
        helloWorldService.resetInvoked();
    }


    @Test
    void testRetry() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Retry.Metrics metrics = rt.getMetrics();
        Consumer<String> consumer =
                toConsumer(helloWorldService::sayHelloWorldWithName)
                .retryConsumer(rt); //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts+4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(RuntimeException.class, ()->consumer.accept(""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(()->consumer.accept(""));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }
    @Test
    void testRetryCheckedConsumer() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Retry.Metrics metrics = rt.getMetrics();
        CheckedConsumer<String> consumer =
                toCheckedConsumer(helloWorldService::sayHelloWorldWithNameWithException)
                        .retryCheckedConsumer(rt); //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts+4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IOException.class, ()->consumer.accept(""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(()->consumer.accept(""));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testRetryBiConsumer() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Retry.Metrics metrics = rt.getMetrics();
        BiConsumer<String, String> consumer =
                toBiConsumer(helloWorldService::sayHelloWorldWithTitleName)
                        .retryBiConsumer(rt); //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts+4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(RuntimeException.class, ()->consumer.accept("",""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(()->consumer.accept("",""));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }
    @Test
    void testRetryCheckedBiConsumer() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Retry.Metrics metrics = rt.getMetrics();
        CheckedBiConsumer<String, String> consumer =
                toCheckedBiConsumer(helloWorldService::sayHelloWorldWithTitleNameWithException)
                        .retryCheckedBiConsumer(rt); //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts+4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IOException.class, ()->consumer.accept("",""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(()->consumer.accept("",""));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testConsumerWithCircuitBreaker() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        helloWorldService.throwException();
        Consumer<String> consumer = toConsumer(helloWorldService::sayHelloWorldWithName)
                .circuitBreakConsumer(cb);
        Try<Void> result = Try.of(()->{consumer.accept("");return null;});
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    void testCheckedConsumerWithCircuitBreaker() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        helloWorldService.throwException();
        CheckedConsumer<String> consumer = toCheckedConsumer(helloWorldService::sayHelloWorldWithNameWithException)
                .circuitBreakCheckedConsumer(cb);
        Try<Void> result = Try.of(()->{consumer.accept("");return null;});
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(IOException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    void testBiConsumerWithCircuitBreaker() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        helloWorldService.throwException();
        BiConsumer<String, String> consumer = toBiConsumer(helloWorldService::sayHelloWorldWithTitleName)
                .circuitBreakBiConsumer(cb);
        Try<Void> result = Try.of(()->{consumer.accept("","");return null;});
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    void testCheckedBiConsumerWithCircuitBreaker() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        helloWorldService.throwException();
        CheckedBiConsumer<String, String> consumer = toCheckedBiConsumer(helloWorldService::sayHelloWorldWithTitleNameWithException)
                .circuitBreakCheckedBiConsumer(cb);
        Try<Void> result = Try.of(()->{consumer.accept("","");return null;});
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(IOException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    void testRateLimitConsumer() throws Throwable {
        RxConsumerSource decorated = new RxConsumerSource(helloWorldService::sayHelloWorldWithName);
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(secondSupplierResult.isSuccess()).isTrue();
        assertTrue(decorated.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitBiConsumer() throws Throwable {
        RxBiConsumerSource decorated = new RxBiConsumerSource(helloWorldService::sayHelloWorldWithTitleName);
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(secondSupplierResult.isSuccess()).isTrue();
        assertTrue(decorated.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitCheckedConsumer() throws Throwable {
        RxCheckedConsumerSource decorated = new RxCheckedConsumerSource(helloWorldService::sayHelloWorldWithNameWithException);
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(secondSupplierResult.isSuccess()).isTrue();
        assertTrue(decorated.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitCheckedBiConsumer() throws Throwable {
        RxCheckedBiConsumerSource decorated = new RxCheckedBiConsumerSource(helloWorldService::sayHelloWorldWithTitleNameWithException);
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.of(decorated.resetInvoked()::get);
        assertThat(secondSupplierResult.isSuccess()).isTrue();
        assertTrue(decorated.isInvoked(), "The invocation  should  have occurred");
    }

    // Ratelimiting
    @AllArgsConstructor @Getter
    private class RxCheckedConsumerSource  {
        private CheckedConsumer<String> core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitCheckedConsumer(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxCheckedConsumerSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {core.accept("");invoked.set(true);return null;}
    }
    @AllArgsConstructor @Getter
    private class RxConsumerSource  {
        private Consumer<String> core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitConsumer(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxConsumerSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {core.accept("");invoked.set(true);return null;}
    }

    @AllArgsConstructor @Getter
    private class RxCheckedBiConsumerSource  {
        private CheckedBiConsumer<String, String> core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitCheckedBiConsumer(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxCheckedBiConsumerSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {core.accept("","");invoked.set(true);return null;}
    }
    @AllArgsConstructor @Getter
    private class RxBiConsumerSource  {
        private BiConsumer<String, String> core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitBiConsumer(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxBiConsumerSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {core.accept("","");invoked.set(true);return null;}
    }
}
