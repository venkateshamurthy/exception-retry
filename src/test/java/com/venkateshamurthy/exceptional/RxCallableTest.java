package com.venkateshamurthy.exceptional;

import com.venkateshamurthy.exceptional.pojo.HelloWorldService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static com.venkateshamurthy.exceptional.RxCallable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@Slf4j
@ExtensionMethod({RxCallable.class, RxTry.class})
public class RxCallableTest {
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
                .intervalFunction(FIBONACCI.seconds(1,120))
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
        helloWorldService = mock(HelloWorldService.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedCall(int number)  {
        class temp implements Callable<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String call() throws Exception {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IOException(i+"-ioException");
                else if(i.get() < number - 2) throw new RemoteException(i+"-remoteException");
                else if(i.get() < number - 1) throw new AccessException(i+"-accessException");
                else return greeting;
            }
            public  Callable<String> get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedCallable(this,
                        AccessException.class, x->new NullPointerException(x.toString()));
                else if (number == 3) return errorMappedCallable(this,
                        AccessException.class, x->new NullPointerException(x.toString()),
                        RemoteException.class, x->new IllegalStateException(x.toString()));
                else                  return errorMappedCallable(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        AccessException.class, x->new NullPointerException(x.toString()),
                        RemoteException.class, x->new IllegalStateException(x.toString()),
                        IOException.class, x -> new UnsupportedOperationException(x.toString()));
            }
        }
        assertEquals(greeting, assertDoesNotThrow(new temp().get().retryCallable(rt)::call));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorConsumedCall(int number)  {
        class temp implements Callable<String> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String call() throws Exception {
                System.out.println("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IOException(i+"-ioException");
                else if(i.get() < number - 2) throw new RemoteException(i+"-remoteException");
                else if(i.get() < number - 1) throw new AccessException(i+"-accessException");
                else return greeting;
            }
            public  Callable<String> get() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedCallable(this,
                        AccessException.class, x -> System.out.println("x="+x.toString()));
                else if (number == 3) return errorConsumedCallable(this,
                        AccessException.class, x -> System.out.println("x1="+x.toString()),
                        RemoteException.class, x -> System.out.println("x2="+x.toString()));
                else                  return errorConsumedCallable(this,
                            // remember you need to align the exception class in hierarchy here
                            // always the least child hierarchy in the first to base exception in the last
                            AccessException.class, x -> System.out.println("xa="+x.toString()),
                            RemoteException.class, x -> System.out.println("xb="+x.toString()),
                            IOException.class,     x -> System.out.println("xc="+x.toString()));
            }
        }

        assertEquals(greeting, assertDoesNotThrow(new temp().get().retryCallable(rt)::call));
    }

    @Test
    void testCircuitBreaker() throws Exception {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        when(helloWorldService.returnHelloWorldWithException()).thenReturn("Hello world");
        Callable<String> callable = helloWorldService::returnHelloWorldWithException;

        String result = callable.circuitBreakCallable(cb).call();

        assertThat(result).isEqualTo("Hello world");
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);
        then(helloWorldService).should().returnHelloWorldWithException();
    }
    @Test
    void testCircuitBreakerAndReturnWithException() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        when(helloWorldService.returnHelloWorldWithException())
                .thenThrow(new IllegalStateException("Illegal State!"));
        Callable<String> c = helloWorldService::returnHelloWorldWithException;
        var callable = c.circuitBreakCallable(cb);
        //catchIllegalStateException(callable::call);
        Try<String> result = Try.of(callable::call);
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
        then(helloWorldService).should().returnHelloWorldWithException();
    }

    @Test
    void testRetry() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Supplier<String> greeting = ()->"Hello World!"+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        Callable<String> callable = toCallable(helloWorldService::returnHelloWorldWithException)
                .retryCallable(rt); //Set for retryMaxAttempts = 5

        var error = new IllegalStateException("Illegal State!"+ attempt.incrementAndGet());
        var mck = when(helloWorldService.returnHelloWorldWithException())
                .thenThrow(error, error, error, error, error) // exhaust all retries (5 times)
                .thenThrow(error, error, error)               // again 3 times throw
                .thenReturn(greeting.get());                  // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IllegalStateException.class, callable::call);

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(greeting.get(), assertDoesNotThrow(callable::call));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

}
