package com.venkateshamurthy.exceptional;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@SuppressWarnings("unchecked")
@ExtensionMethod({RxSupplier.class})
@Slf4j
public class RateLimiterTest {

    private static final int LIMIT = 50;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REFRESH_PERIOD = Duration.ofNanos(500);
    private  RateLimiter rl;

    @BeforeEach
    public void init() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(TIMEOUT)
                .limitRefreshPeriod(REFRESH_PERIOD)
                .limitForPeriod(LIMIT)
                .build();
        rl= Mockito.spy(RateLimiter.of("RateLimiterDefault", config));
        given(rl.getRateLimiterConfig()).willReturn(config);
    }


    @AllArgsConstructor @Getter
    private enum RxCheckedSupplierSource  implements CheckedSupplier {
        CC(RxSupplier.rxCheckedSupplier((String s)->System.out.println(s), "Checked Consumer: Hello World!")),
        CBIF(RxSupplier.rxCheckedSupplier(String::concat, "Checked Bi Function: Hello", " World!")),
        CBIC(RxSupplier.rxCheckedSupplier((String s, String s2)->System.out.println(s+s2), "Checked Bi Consumer: Hello", " World!"));

        private CheckedSupplier core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitCheckedSupplier(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxCheckedSupplierSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {var result= core.get();invoked.set(true);return result;}
    }

    @AllArgsConstructor
    @Getter
    private enum RxSupplierSource  implements Supplier {
        CONSUMER(RxSupplier.rxSupplier((String s)->System.out.println(s), "Consumer: Hello World!")),
        BI_FUNCTION(RxSupplier.rxSupplier(String::concat, "Bi Function: Hello", " World!")),
        BI_CONSUMER(RxSupplier.rxSupplier((String s1, String s2)->System.out.println(s1+s2), "Bi Consumer: Hello", " World!"));

        private Supplier core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitSupplier(RL);}
        private void setCircuitBreaker(CircuitBreaker cb){core = core.circuitBreakSupplier(cb);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxSupplierSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object get() {var result= core.get();invoked.set(true);return result;}
    }

    @ParameterizedTest
    @EnumSource(RxCheckedSupplierSource.class)
    void rateLimitCheckedSupplier(RxCheckedSupplierSource decorated) throws Throwable {
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

    @ParameterizedTest
    @EnumSource(RxSupplierSource.class)
    void rateLimitSupplier(RxSupplierSource decorated)  {
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.ofSupplier(decorated.resetInvoked())
                .onFailure(RequestNotPermitted.class, e->System.out.println("HHHHHAAAA:"+e));
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.ofSupplier(decorated.resetInvoked())
                .onSuccess(s->System.out.println("secondSupplierResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->System.out.println("HHHHHAAAA:"+e));;
        assertThat(secondSupplierResult.isSuccess()).isTrue();
        assertTrue(decorated.isInvoked(), "The invocation  should  have occurred");
    }

    @ParameterizedTest
    @EnumSource(RxSupplierSource.class)
    public void circuitBreakSupplierAndReturnWithSuccess(RxSupplierSource decorated) {
        CircuitBreaker circuitBreaker = spy(CircuitBreaker.of(decorated.name(), CircuitBreakerConfig.custom().build()));
        decorated.setCircuitBreaker(circuitBreaker);
        decorated = spy(decorated);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        doThrow(CallNotPermittedException.class).when(circuitBreaker).acquirePermission();
        var result=Try.ofCallable(decorated::get);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(CallNotPermittedException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();

        doCallRealMethod().when(circuitBreaker).acquirePermission();
        doThrow(new RuntimeException()).when(decorated).get();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();
    }
}