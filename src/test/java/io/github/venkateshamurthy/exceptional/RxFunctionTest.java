package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
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

import javax.naming.TimeLimitExceededException;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.Delayer.FIBONACCI;
import static io.github.venkateshamurthy.exceptional.RxFunction.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@Slf4j
@ExtensionMethod({RxFunction.class, RxTry.class})
class RxFunctionTest {
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
        helloWorldService = mock(HelloWorldService.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testErrorMappedFunction(int number)  {
        class temp implements Function<String, String>, Supplier<Function<String, String>> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String apply(String in)  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new UnsupportedOperationException(i+"-unsupportedException");
                else if(i.get() < number - 2) throw new IllegalStateException(i+"-illegalStateException");
                else if(i.get() < number - 1) throw new NullPointerException(i+"-nullPtrException");
                else return greeting.toUpperCase();
            }
            public  Function<String, String> get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedFunction(this,
                        NullPointerException.class, x->new IllegalArgumentException(x.toString()));
                else if (number == 3) return errorMappedFunction(this,
                        NullPointerException.class, x->new IllegalArgumentException(x.toString()),
                        IllegalStateException.class, x->new ArrayIndexOutOfBoundsException(x.toString()));
                else                  return errorMappedFunction(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        NullPointerException.class, x->new IllegalArgumentException(x.toString()),
                        IllegalStateException.class, x->new ArrayIndexOutOfBoundsException(x.toString()),
                        UnsupportedOperationException.class, x -> new TimeLimitExceededException(x.toString()));
            }
        }
        assertEquals(greeting.toUpperCase(), assertDoesNotThrow(()->new temp().get().retryFunction(rt).apply(greeting)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @SneakyThrows
    void testErrorMappedCheckedFunction(int number)  {
        class temp implements CheckedFunction<String, String>, Supplier<CheckedFunction<String, String>> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String apply(String in)  throws Throwable{
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new IOException(i+"-ioException");
                else if(i.get() < number - 2) throw new RemoteException(i+"-remoteException");
                else if(i.get() < number - 1) throw new AccessException(i+"-accessException");
                else return greeting.toUpperCase();
            }
            public  CheckedFunction<String, String> get() {
                if      (number == 1) return this;
                else if (number == 2) return errorMappedCheckedFunction(this,
                        AccessException.class, x -> new SQLTimeoutException(x.toString()));
                else if (number == 3) return errorMappedCheckedFunction(this,
                        AccessException.class, x -> new SQLTimeoutException(x.toString()),
                        RemoteException.class, x -> new InterruptedException(x.toString()));
                else return errorMappedCheckedFunction(this,
                        // remember you need to align the exception class in hierarchy here
                        // always the least child hierarchy in the first to base exception in the last
                        AccessException.class, x -> new SQLTimeoutException(x.toString()),
                        RemoteException.class, x -> new InterruptedException(x.toString()),
                        IOException.class, x -> new UnsupportedOperationException(x.toString()));
            }
        }
        assertEquals(greeting.toUpperCase(), assertDoesNotThrow(()->new temp().get().retryCheckedFunction(rt).apply(greeting)));
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testErrorConsumedFunction(int number)  {
        class temp implements Function<String, String>, Supplier<Function<String, String>> {
            final AtomicInteger i = new AtomicInteger(-1);
            @Override
            public String apply(String in)  {
                log.info("I="+i.incrementAndGet());
                if(i.get()      < number - 3) throw new UnsupportedOperationException(i+"-unsupportedException");
                else if(i.get() < number - 2) throw new IllegalStateException(i+"-illegalStateException");
                else if(i.get() < number - 1) throw new NullPointerException(i+"-nullPtrException");
                else return greeting.toUpperCase();
            }
            public  Function<String, String> get() {
                if      (number == 1) return this;
                else if (number == 2) return errorConsumedFunction(this,
                        NullPointerException.class, x->new IllegalArgumentException(x.toString()));
                else if (number == 3) return errorConsumedFunction(this,
                        NullPointerException.class, x->new IllegalArgumentException(x.toString()),
                        IllegalStateException.class, x->new ArrayIndexOutOfBoundsException(x.toString()));
                else                  return errorConsumedFunction(this,
                            // remember you need to align the exception class in hierarchy here
                            // always the least child hierarchy in the first to base exception in the last
                            NullPointerException.class, x->new IllegalArgumentException(x.toString()),
                            IllegalStateException.class, x->new ArrayIndexOutOfBoundsException(x.toString()),
                            UnsupportedOperationException.class, x -> new TimeLimitExceededException(x.toString()));
            }
        }
        assertEquals(greeting.toUpperCase(), assertDoesNotThrow(()->new temp().get().retryFunction(rt).apply(greeting)));
    }

    @Test
    void testCircuitBreaker() throws Exception {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        when(helloWorldService.returnHelloWorldWithName("")).thenReturn("Hello world");
        Function<String, String> callable = helloWorldService::returnHelloWorldWithName;

        String result = callable.circuitBreakFunction(cb).apply("");

        assertThat(result).isEqualTo("Hello world");
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isZero();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(1);
        then(helloWorldService).should().returnHelloWorldWithName("");
    }
    @Test
    void testCircuitBreakerAndReturnWithException() throws Throwable {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        assertThat(metrics.getNumberOfBufferedCalls()).isZero();
        when(helloWorldService.returnHelloWorldWithNameWithException(""))
                .thenThrow(new IllegalStateException("Illegal State!"));
        CheckedFunction<String, String> f = helloWorldService::returnHelloWorldWithNameWithException;
        var cf = f.circuitBreakCheckedFunction(cb);
        //catchIllegalStateException(callable::call);
        Try<String> result = Try.of(()->cf.apply(""));
        assertThat(result.isFailure()).isTrue();
        assertThat(result.failed().get()).isInstanceOf(RuntimeException.class);
        assertThat(metrics.getNumberOfBufferedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCalls()).isZero();
        then(helloWorldService).should().returnHelloWorldWithNameWithException("");
    }

    @Test
    void testRetry() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        Function<String, String> greeting = (in)->in+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        Function<String, String> callable = toFunction(helloWorldService::returnHelloWorldWithName)
                .retryFunction(rt); //Set for retryMaxAttempts = 5

        var error = new IllegalStateException("Illegal State!"+ attempt.incrementAndGet());
        var mck = when(helloWorldService.returnHelloWorldWithName(""))
                .thenThrow(error, error, error, error, error) // exhaust all retries (5 times)
                .thenThrow(error, error, error)               // again 3 times throw
                .thenReturn(greeting.apply(""));                  // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IllegalStateException.class, ()->callable.apply(""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(greeting.apply(""), assertDoesNotThrow(()->callable.apply("")));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testRetryBiFunction() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        BiFunction<String, String, String> greeting = (in,out)->in+out+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        BiFunction<String, String, String> callable = toBiFunction(helloWorldService::returnHelloWorldWithTitleName)
                .retryBiFunction(rt); //Set for retryMaxAttempts = 5

        var error = new IllegalStateException("Illegal State!"+ attempt.incrementAndGet());
        var mck = when(helloWorldService.returnHelloWorldWithTitleName("",""))
                .thenThrow(error, error, error, error, error) // exhaust all retries (5 times)
                .thenThrow(error, error, error)               // again 3 times throw
                .thenReturn(greeting.apply("",""));      // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IllegalStateException.class, ()->callable.apply("",""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(greeting.apply("",""), assertDoesNotThrow(()->callable.apply("","")));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }

    @Test
    void testRetryCheckedBiFunction() throws Throwable {
        final AtomicInteger attempt=new AtomicInteger();
        CheckedBiFunction<String, String, String> greeting = (in, out)->in+out+ attempt.get();
        Retry.Metrics metrics = rt.getMetrics();
        CheckedBiFunction<String, String, String> callable = toCheckedBiFunction(helloWorldService::returnHelloWorldWithTitleName)
                .retryCheckedBiFunction(rt); //Set for retryMaxAttempts = 5

        var error = new IOException("IOException!"+ attempt.incrementAndGet());
        var mck = when(helloWorldService.returnHelloWorldWithTitleName("",""))
                .thenThrow(error, error, error, error, error) // exhaust all retries (5 times)
                .thenThrow(error, error, error)               // again 3 times throw
                .thenReturn(greeting.apply("",""));      // so at retryMaxAttempts+4 it should work
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(0);
        assertThrows(IOException.class, ()->callable.apply("",""));

        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts);
        assertThat(metrics.getNumberOfFailedCallsWithRetryAttempt()).isEqualTo(1);
        assertThat(metrics.getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(0);

        assertEquals(greeting.apply("",""), assertDoesNotThrow(()->callable.apply("","")));
        assertThat(metrics.getNumberOfTotalCalls()).isEqualTo(retryMaxAttempts+4);
    }
}
