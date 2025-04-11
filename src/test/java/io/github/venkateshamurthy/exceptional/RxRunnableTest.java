package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.venkateshamurthy.exceptional.pojo.HelloWorldService;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
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
                .intervalFunction(FIBONACCI.millis(10,1200))
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
        helloWorldService = mock(HelloWorldService.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedRun(int number)  {
        class temp implements Runnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-ArrayIndexOutOfBoundsException");
                else log.info(greeting);
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
    void testErrorConsumedCall(int number)  {
        class temp implements Runnable {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public void run()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IllegalArgumentException(i+"-IllegalArgumentException");
                else if(i.get() < number - 2) throw new ArithmeticException(i+"-ArithmeticException");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-accessException");
                else log.info(greeting);
            }
            public  Runnable get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedRunnable(this,
                        IllegalArgumentException.class, x->new NullPointerException(x.toString()));
                else if (number == 3) return errorMappedRunnable(this,
                        ArithmeticException.class, x->new NullPointerException(x.toString()),
                        ArrayIndexOutOfBoundsException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedRunnable(this,
                            // remember you need to align the exception class in hierarchy here
                            // always the least child hierarchy in the first to base exception in the last
                            IllegalArgumentException.class, x->new NullPointerException(x.toString()),
                            ArithmeticException.class, x->new IllegalStateException(x.toString()),
                            ArrayIndexOutOfBoundsException.class, x -> new UnsupportedOperationException(x.toString()));
            }
        }

        assertDoesNotThrow(new temp().get().retryRunnable(rt)::run);
    }

    @Test
    void testCircuitBreaker() throws Exception {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        doThrow(RuntimeException.class).doNothing()
                .when(helloWorldService).sayHelloWorld();
        Runnable runnable = toRunnable(helloWorldService::sayHelloWorld)
                .circuitBreakRunnable(cb);
        Try<Void> result = Try.runRunnable(runnable);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isOne();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(0);
        then(helloWorldService).should().sayHelloWorld();
    }

    @SneakyThrows
    @Test
    void testCircuitBreakerAndReturnWithException() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        doThrow(IOException.class).doNothing()
                .when(helloWorldService).sayHelloWorldWithException();
        CheckedRunnable c = toCheckedRunnable(helloWorldService::sayHelloWorldWithException)
                .circuitBreakCheckedRunnable(cb);
        //catchIllegalStateException(runnable::call);
        Try<Void> result = Try.run(c::run);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(IOException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
        then(helloWorldService).should().sayHelloWorldWithException();
    }

    @Test
    void testRetry() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Supplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        CheckedRunnable runnable = toCheckedRunnable(helloWorldService::sayHelloWorldWithException)
                .retryCheckedRunnable(rt);          //Set for retryMaxAttempts = 5

        var error = new IOException("IO Exception!"+ attempt.incrementAndGet());
        doThrow(error, error, error, error, error) // exhaust all retries (5 times)
            .doThrow(error, error, error)          // again 3 times throw
            .doNothing()
            .when(helloWorldService).sayHelloWorldWithException();
            ;                                      // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IOException.class, runnable::run);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertDoesNotThrow(runnable::run);
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

}
