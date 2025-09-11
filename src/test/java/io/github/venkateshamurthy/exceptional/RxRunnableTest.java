package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.venkateshamurthy.exceptional.pojo.HelloWorldService;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static io.github.venkateshamurthy.exceptional.RxRunnable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtensionMethod({RxRunnable.class, RxTry.class})
class RxRunnableTest {
    private final int retryMaxAttempts = 5;
    Bulkhead bh;
    Retry rt;
    CircuitBreaker cb;
    RateLimiter rl;
    AtomicInteger i;
    String greeting = "Hello World";
    HelloWorldService helloWorldService;

    @BeforeEach
    void before() {
        i = new AtomicInteger();
        bh = Bulkhead.ofDefaults("bh");
        rt = Retry.of("rt", RetryConfig.custom()
                .maxAttempts(retryMaxAttempts)
                .failAfterMaxAttempts(true)
                .intervalFunction(FIBONACCI.millis(10,120))
                .retryExceptions(Exception.class,
                        TimeoutException.class,
                        NullPointerException.class,
                        UnsupportedOperationException.class,
                        IllegalAccessException.class,
                        IllegalStateException.class)
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
        clearAllCaches();
        helloWorldService = new HelloWorldService(){};
    }

    @AfterEach
    void reset() {
        helloWorldService.resetInvoked();
        helloWorldService.revokeException();
    }

    @Test
    void testRateLimitCheckedRunnable()  {
        var rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.getRateLimiterConfig()).thenReturn(rl.getRateLimiterConfig());
        CheckedRunnable runnable = toCheckedRunnable(helloWorldService::sayHelloWorldWithException)
                .rateLimitCheckedRunnable(rateLimiter);
        when(rateLimiter.acquirePermission(1)).thenReturn(false);
        Try<Void> tryRun = runnable.tryWrap()
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryRun.isFailure()).isTrue();
        assertThat(tryRun.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(helloWorldService.isInvoked(), "The invocation  should not have occurred");

        when(rateLimiter.acquirePermission(1)).thenReturn(true);
        helloWorldService.resetInvoked();
        Try<Void> tryRun2 = runnable.tryWrap()
                .onSuccess(s->log.debug("secondResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryRun2.isSuccess()).isTrue();
        assertTrue(helloWorldService.isInvoked(), "The invocation  should  have occurred");
    }

    @Test
    void testWrappers() {
        assertDoesNotThrow(rxRunnable(x->{},null)::run);
        assertDoesNotThrow(rxRunnable((x,y)->{},null, null)::run);
        assertDoesNotThrow(bulkheadRunnable(()->{}, bh)::run);
        assertDoesNotThrow(rxCheckedRunnable(x->{},null)::run);
        assertDoesNotThrow(rxCheckedRunnable((x,y)->{},null, null)::run);
        assertDoesNotThrow(bulkheadCheckedRunnable(()->{}, bh)::run);

        assertThrows(NullPointerException.class, errorConsumedRunnable(()->{throw new NullPointerException();},
                NullPointerException.class, npe->{})::run);
        assertThrows(UnsupportedOperationException.class, errorConsumedRunnable(()->{throw new UnsupportedOperationException();},
                NullPointerException.class, (NullPointerException npe)->{})::run);
    }

    @Test
    void testRateLimitRunnable()  {
        var rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.getRateLimiterConfig()).thenReturn(rl.getRateLimiterConfig());
        Runnable runnable = toRunnable(helloWorldService::sayHelloWorld)
                .rateLimitRunnable(rateLimiter);
        when(rateLimiter.acquirePermission(1)).thenReturn(false);
        Try<Void> tryRun = runnable.tryWrap()
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryRun.isFailure()).isTrue();
        assertThat(tryRun.getCause()).isInstanceOf(RequestNotPermitted.class);
        assertFalse(helloWorldService.isInvoked(), "The invocation  should not have occurred");

        when(rateLimiter.acquirePermission(1)).thenReturn(true);
        helloWorldService.resetInvoked();
        Try<Void> tryRun2 = runnable.tryWrap()
                .onSuccess(s->log.debug("secondResult:SUCCESS:"+s))
                .onFailure(RequestNotPermitted.class, e->log.debug("HHHHHAAAA:"+e));
        assertThat(tryRun2.isSuccess()).isTrue();
        assertTrue(helloWorldService.isInvoked(), "The invocation  should  have occurred");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedRun(int number)  {
        class temp implements Runnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.debug("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-ArrayIndexOutOfBoundsException");
                else log.debug(greeting);
            }
            public  Runnable get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedRunnable(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalStateException(x.toString()));
                else if (number == 3) return errorMappedRunnable(this,
                        ArithmeticException.class, x->new NullPointerException(x.toString()),
                        ArrayIndexOutOfBoundsException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedRunnable(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                            IllegalArgumentException.class, x->new UnsupportedOperationException(x.toString()),
                            ArithmeticException.class, x->new NullPointerException(x.toString()),
                            ArrayIndexOutOfBoundsException.class, x -> new IllegalStateException(x.toString()));
            }
        }
        assertDoesNotThrow(new temp().get().retryRunnable(rt)::run);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedCheckedRunnable(int number)  {
        class temp implements CheckedRunnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.debug("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-ArrayIndexOutOfBoundsException");
                else log.debug(greeting);
            }
            public  CheckedRunnable get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedCheckedRunnable(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalStateException(x.toString()));
                else if (number == 3) return errorMappedCheckedRunnable(this,
                        ArithmeticException.class, x->new NullPointerException(x.toString()),
                        ArrayIndexOutOfBoundsException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedCheckedRunnable(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        IllegalArgumentException.class, x->new UnsupportedOperationException(x.toString()),
                        ArithmeticException.class, x->new NullPointerException(x.toString()),
                        ArrayIndexOutOfBoundsException.class, x -> new IllegalStateException(x.toString()));
            }
        }
        assertDoesNotThrow(new temp().get().retryCheckedRunnable(rt)::run);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorConsumedRunnable(int number)  {
        class temp implements Runnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.debug("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-accessException");
                else log.debug(greeting);
            }
            public  Runnable get() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedRunnable(this,
                        IllegalArgumentException.class, x->{});
                else if (number == 3) return errorConsumedRunnable(this,
                        ArithmeticException.class, x->{},
                        ArrayIndexOutOfBoundsException.class, x->{});
                else                  return errorConsumedRunnable(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        IllegalArgumentException.class, x->{},
                        ArithmeticException.class, x->{},
                        ArrayIndexOutOfBoundsException.class, x -> {});
            }
        }

        assertDoesNotThrow(new temp().get().retryRunnable(rt)::run);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorConsumedCheckedRunnable(int number)  {
        class temp implements CheckedRunnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.debug("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-accessException");
                else log.debug(greeting);
            }
            public  CheckedRunnable get() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedCheckedRunnable(this,
                        IllegalArgumentException.class, x->{});
                else if (number == 3) return errorConsumedCheckedRunnable(this,
                        ArithmeticException.class, x->{},
                        ArrayIndexOutOfBoundsException.class, x->{});
                else                  return errorConsumedCheckedRunnable(this,
                            // remember you need to align the exception class in hierarchy here
                            // always the least child hierarchy in the first to base exception in the last
                            IllegalArgumentException.class, x->{},
                            ArithmeticException.class, x->{},
                            ArrayIndexOutOfBoundsException.class, x -> {});
            }
        }

        assertDoesNotThrow(new temp().get().retryCheckedRunnable(rt)::run);
    }

    @Test
    void testCircuitBreaker() throws Exception {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        Runnable runnable = toRunnable(helloWorldService::sayHelloWorld)
                .circuitBreakRunnable(cb);
        helloWorldService.throwException(2);
        Try<Void> result = Try.runRunnable(runnable);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isOne();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(0);
        assertDoesNotThrow(()->Try.runRunnable(runnable));
    }

    @SneakyThrows
    @Test
    void testCircuitBreakerAndReturnWithException() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        CheckedRunnable c = toCheckedRunnable(helloWorldService::sayHelloWorldWithException)
                .circuitBreakCheckedRunnable(cb);
        helloWorldService.throwException(2);
        //catchIllegalStateException(runnable::call);
        Try<Void> result = Try.run(c::run);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(IOException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
        assertDoesNotThrow(()->Try.run(c::run));
    }

    @Test
    void testRetryCheckedRunnable() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Supplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        CheckedRunnable runnable = toCheckedRunnable(helloWorldService::sayHelloWorldWithException)
                .retryCheckedRunnable(rt);          //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts + 4);

        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IOException.class, runnable::run);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(runnable::run);
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testRetryRunnable() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Supplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        Runnable runnable = toRunnable(helloWorldService::sayHelloWorld)
                .retryRunnable(rt);          //Set for retryMaxAttempts = 5
        helloWorldService.throwException(retryMaxAttempts + 4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(RuntimeException.class, runnable::run);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(runnable::run);
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

}
