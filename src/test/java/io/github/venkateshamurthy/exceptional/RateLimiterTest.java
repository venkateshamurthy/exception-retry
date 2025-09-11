package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.RxCallable.toCallable;
import static io.github.venkateshamurthy.exceptional.RxFunction.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@SuppressWarnings("unchecked")
@ExtensionMethod({RxSupplier.class, RxFunction.class, RxCallable.class, RxTry.class})
@Slf4j
class RateLimiterTest {

    private static final int LIMIT = 50;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REFRESH_PERIOD = Duration.ofNanos(500);
    private  RateLimiter rl;
    private HelloWorldService service = new HelloWorldService() {};
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

    @AfterEach
    void reset() {
        service.resetInvoked();
        service.revokeException();
    }

    @AllArgsConstructor @Getter
    private enum  RxCallableSource  implements Callable {
        DEFAULT(toCallable(()->"Heelo World!")),
        EXCEPTION(toCallable(()->{throw new Exception();}));

        private Callable core;
        private void setRateLimiter(RateLimiter RL){core = core.rateLimitCallable(RL);}
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        RxCallableSource resetInvoked() {invoked.set(false);return this;}
        boolean isInvoked(){ return invoked.get(); }
        @SneakyThrows public Object call() {var result= core.call();invoked.set(true);return result;}
    }

    @AllArgsConstructor @Getter
    private enum  RxCheckedSupplierSource  implements CheckedSupplier {
        CC(RxSupplier.toCheckedSupplier((String s)->log.debug(s), "Checked Consumer: Hello World!")),
        CBIF(RxSupplier.toCheckedSupplier(String::concat, "Checked Bi Function: Hello", " World!")),
        CBIC(RxSupplier.toCheckedSupplier((String s, String s2)->log.debug(s+s2), "Checked Bi Consumer: Hello", " World!"));

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
        CONSUMER(RxSupplier.toSupplier((String s)->log.debug(s), "Consumer: Hello World!")),
        BI_FUNCTION(RxSupplier.toSupplier(String::concat, "Bi Function: Hello", " World!")),
        BI_CONSUMER(RxSupplier.toSupplier((String s1, String s2)->log.debug(s1+s2), "Bi Consumer: Hello", " World!"));

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
    @EnumSource(RxCallableSource.class)
    void rateLimitCallable(RxCallableSource decorated) throws Throwable {
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoaratedCallableResult = Try.ofCallable(decorated.resetInvoked());
        assertThat(decoaratedCallableResult.isFailure()).isTrue();
        assertThat(decoaratedCallableResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation should not have occurred");
    }

    @ParameterizedTest
    @EnumSource(RxSupplierSource.class)
    void rateLimitSupplier(RxSupplierSource decorated)  {
        decorated.setRateLimiter(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try decoratedSupplierResult = Try.ofSupplier(decorated.resetInvoked())
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(decoratedSupplierResult.isFailure()).isTrue();
        assertThat(decoratedSupplierResult.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(decorated.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        Try secondSupplierResult = Try.ofSupplier(decorated.resetInvoked())
                .onSuccess(s->log.debug("secondSupplierResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));;
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


    @Test
    void testRateLimitFunction()  {
        Function<String, String> function = toFunction(service::returnHelloWorldWithName).rateLimitFunction(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try<String> tryFunc = function.tryWrap("")
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc.isFailure()).isTrue();
        assertThat(tryFunc.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(service.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        service.resetInvoked();
        Try<String> tryFunc2 = function.tryWrap("")
                .onSuccess(s->log.debug("secondSupplierResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));;
        assertThat(tryFunc2.isSuccess()).isTrue();
        assertTrue(service.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitBiFunction()  {
        BiFunction<String, String, String> function = toBiFunction(service::returnHelloWorldWithTitleName)
                .rateLimitBiFunction(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try<String> tryFunc = function.tryWrap("","")
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc.isFailure()).isTrue();
        assertThat(tryFunc.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(service.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        service.resetInvoked();
        Try<String> tryFunc2 = function.tryWrap("","")
                .onSuccess(s->log.debug("secondSupplierResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc2.isSuccess()).isTrue();
        assertTrue(service.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitCheckedFunction()  {
        CheckedFunction<String, String> function = toCheckedFunction(service::returnHelloWorldWithName)
                .rateLimitCheckedFunction(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try<String> tryFunc = function.tryWrap("")
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc.isFailure()).isTrue();
        assertThat(tryFunc.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(service.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        service.resetInvoked();
        Try<String> tryFunc2 = function.tryWrap("")
                .onSuccess(s-> log.debug("secondResult:SUCCESS:" + s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));;
        assertThat(tryFunc2.isSuccess()).isTrue();
        assertTrue(service.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testRateLimitCheckedBiFunction()  {
        CheckedBiFunction<String, String, String> function = toCheckedBiFunction(service::returnHelloWorldWithTitleNameWithException)
                .rateLimitCheckedBiFunction(rl);
        given(rl.acquirePermission(1)).willReturn(false);
        Try<String> tryFunc = function.tryWrap("","")
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc.isFailure()).isTrue();
        assertThat(tryFunc.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(service.isInvoked(), "The invocation  should not have occurred");

        given(rl.acquirePermission(1)).willReturn(true);
        service.resetInvoked();
        Try<String> tryFunc2 = function.tryWrap("","")
                .onSuccess(s->log.debug("secondResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryFunc2.isSuccess()).isTrue();
        assertTrue(service.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    public void testCircuitBreakFunction() {
        Function<String, String> function = toFunction(service::returnHelloWorldWithName);
        CircuitBreaker circuitBreaker = spy(CircuitBreaker.of("Hello World", CircuitBreakerConfig.custom().build()));
        Function<String, String> decorated = function.circuitBreakFunction(circuitBreaker);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        doThrow(CallNotPermittedException.class).when(circuitBreaker).acquirePermission();
        var result=decorated.tryWrap("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(CallNotPermittedException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();

        doCallRealMethod().when(circuitBreaker).acquirePermission();
        service.throwException();
        //doThrow(new RuntimeException()).when(decorated).apply("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    public void testCircuitBreakBiFunction() {
        BiFunction<String, String, String> function = toBiFunction(service::returnHelloWorldWithTitleName);
        CircuitBreaker circuitBreaker = spy(CircuitBreaker.of("Hello World", CircuitBreakerConfig.custom().build()));
        BiFunction<String, String, String> decorated = function.circuitBreakBiFunction(circuitBreaker);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        doThrow(CallNotPermittedException.class).when(circuitBreaker).acquirePermission();
        var result=decorated.tryWrap("","");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(CallNotPermittedException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();

        doCallRealMethod().when(circuitBreaker).acquirePermission();
        service.throwException();
        //doThrow(new RuntimeException()).when(decorated).apply("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    public void testCircuitBreakCheckedFunction() {
        CheckedFunction<String, String> function = toCheckedFunction(service::returnHelloWorldWithNameWithException);
        CircuitBreaker circuitBreaker = spy(CircuitBreaker.of("Hello World", CircuitBreakerConfig.custom().build()));
        CheckedFunction<String, String> decorated = function.circuitBreakCheckedFunction(circuitBreaker);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        doThrow(CallNotPermittedException.class).when(circuitBreaker).acquirePermission();
        var result=decorated.tryWrap("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(CallNotPermittedException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();

        doCallRealMethod().when(circuitBreaker).acquirePermission();
        service.throwException();
        //doThrow(new RuntimeException()).when(decorated).apply("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    public void testCircuitBreakCheckedBiFunction() {
        CheckedBiFunction<String, String, String> function = toCheckedBiFunction(service::returnHelloWorldWithTitleNameWithException);
        CircuitBreaker circuitBreaker = spy(CircuitBreaker.of("Hello World", CircuitBreakerConfig.custom().build()));
        CheckedBiFunction<String, String, String> decorated = function.circuitBreakCheckedBiFunction(circuitBreaker);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        doThrow(CallNotPermittedException.class).when(circuitBreaker).acquirePermission();
        var result=decorated.tryWrap("","");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(CallNotPermittedException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isEqualTo(0);
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();

        doCallRealMethod().when(circuitBreaker).acquirePermission();
        service.throwException();
        //doThrow(new RuntimeException()).when(decorated).apply("");
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(circuitBreaker.getMetrics().getNumberOfBufferedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfFailedCalls()).isZero();
        assertThat(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls()).isZero();
    }
}