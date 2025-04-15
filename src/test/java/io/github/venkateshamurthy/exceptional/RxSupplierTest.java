package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
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

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static io.github.venkateshamurthy.exceptional.RxSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtensionMethod({RxSupplier.class, RxTry.class})
class RxSupplierTest {
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
                        IllegalAccessException.class,
                        IllegalStateException.class)
                .build());
        cb = CircuitBreaker.ofDefaults("cb");
        rl = RateLimiter.ofDefaults("rl");
        greeting = "greeting";
        clearAllCaches();
        helloWorldService = new HelloWorldService() {
        };
    }

    @AfterEach
    void after() {
        helloWorldService.revokeException();
        helloWorldService.resetInvoked();
    }


    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedSupplier(int number)  {
        class temp implements Supplier<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String get()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new RuntimeException(i+"-runtimeException");
                else if(i.get() < number - 2) throw new NullPointerException(i+"-nullptr");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-arrayIndexException");
                else return greeting;
            }
            public  Supplier<String> make() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()));
                else if (number == 3) return errorMappedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()),
                        NullPointerException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedSupplier(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()),
                        NullPointerException.class, x->new IllegalStateException(x.toString()),
                        RuntimeException.class, x -> new UnsupportedOperationException(x.toString()));
            }
        }
        assertEquals(greeting, assertDoesNotThrow(new temp().make().retrySupplier(rt)::get));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedCheckedSupplier(int number)  {
        class temp implements CheckedSupplier<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String get()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new RuntimeException(i+"-runtimeException");
                else if(i.get() < number - 2) throw new NullPointerException(i+"-nullptr");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-arrayIndexException");
                else return greeting;
            }
            public  CheckedSupplier<String> make() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedCheckedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()));
                else if (number == 3) return errorMappedCheckedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()),
                        NullPointerException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedCheckedSupplier(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        ArrayIndexOutOfBoundsException.class, x->new IllegalArgumentException(x.toString()),
                        NullPointerException.class, x->new IllegalStateException(x.toString()),
                        RuntimeException.class, x -> new UnsupportedOperationException(x.toString()));
            }
        }
        assertEquals(greeting, assertDoesNotThrow(new temp().make().retryCheckedSupplier(rt)::get));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorConsumedSupplier(int number)  {
        class temp implements Supplier<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String get()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new RuntimeException(i+"-runtimeException");
                else if(i.get() < number - 2) throw new NullPointerException(i+"-nullptr");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-arrayIndexException");
                else return greeting;
            }
            public  Supplier<String> make() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->{});
                else if (number == 3) return errorConsumedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->{},
                        NullPointerException.class, x->{});
                else                  return errorConsumedSupplier(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        ArrayIndexOutOfBoundsException.class, x->{},
                        NullPointerException.class, x->{},
                        RuntimeException.class, x -> {});
            }
        }

        assertEquals(greeting, assertDoesNotThrow(new temp().make().retrySupplier(rt)::get));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testErrorConsumedCheckedSupplier(int number)  {
        class temp implements CheckedSupplier<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String get()  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new RuntimeException(i+"-runtimeException");
                else if(i.get() < number - 2) throw new NullPointerException(i+"-nullptr");
                else if(i.get() < number - 1) throw new ArrayIndexOutOfBoundsException(i+"-arrayIndexException");
                else return greeting;
            }
            public  CheckedSupplier<String> make() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedCheckedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->{});
                else if (number == 3) return errorConsumedCheckedSupplier(this,
                        ArrayIndexOutOfBoundsException.class, x->{},
                        NullPointerException.class, x->{});
                else                  return errorConsumedCheckedSupplier(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        ArrayIndexOutOfBoundsException.class, x->{},
                        NullPointerException.class, x->{},
                        RuntimeException.class, x -> {});
            }
        }

        assertEquals(greeting, assertDoesNotThrow(new temp().make().retryCheckedSupplier(rt)::get));
    }

    @Test
    void testCircuitBreakerSupplier()  {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        Supplier<String> supplier = helloWorldService::returnHelloWorld;

        String result = supplier.circuitBreakSupplier(cb).get();

        assertThat(result).isEqualTo(HelloWorldService.greeting);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);
    }

    @Test
    @SneakyThrows
    void testCircuitBreakerCheckedSupplier()  {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        CheckedSupplier<String> supplier = helloWorldService::returnHelloWorldWithException;
        String result = supplier.circuitBreakCheckedSupplier(cb).get();

        assertThat(result).isEqualTo(HelloWorldService.greeting);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);
    }

    @Test
    void testCircuitBreakerAndReturnWithException() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        helloWorldService.throwException();
        Supplier<String> supply = toSupplier(helloWorldService::returnHelloWorld)
                .circuitBreakSupplier(cb);
        //catchIllegalStateException(callable::call);
        Try<String> result = Try.ofSupplier(supply::get);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
    }

    @Test
    void testRetry() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Supplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        Supplier<String> supplier = toSupplier(helloWorldService::returnHelloWorld).retrySupplier(rt);
        helloWorldService.throwException(retryMaxAttempts+4);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(RuntimeException.class, supplier::get);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(HelloWorldService.greeting, assertDoesNotThrow(supplier::get));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testRetryCheckedSupplier() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        CheckedSupplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        HelloWorldService spy = spy(helloWorldService);
        CheckedSupplier<String> supplier = toCheckedSupplier(spy::returnHelloWorldWithException)
                .retryCheckedSupplier(rt); //Set for retryMaxAttempts = 5

        var error = new IllegalStateException("Illegal State!"+ attempt.incrementAndGet());
        var mck = when(spy.returnHelloWorldWithException())
                .thenThrow(error, error, error, error, error) // exhaust all retries (5 times)
                .thenThrow(error, error, error)               // again 3 times throw
                .thenReturn(greeting.get());                  // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IllegalStateException.class, supplier::get);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(greeting.get(), assertDoesNotThrow(supplier::get));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

}
