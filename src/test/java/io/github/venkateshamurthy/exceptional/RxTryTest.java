package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.venkateshamurthy.exceptional.exceptions.CommonRuntimeException;
import io.github.venkateshamurthy.exceptional.exceptions.ExceptionCodes;
import io.vavr.control.Try;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.PackagePrivate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.venkateshamurthy.exceptional.RxCallable.toCallable;
import static io.github.venkateshamurthy.exceptional.RxRunnable.toCheckedRunnable;
import static io.github.venkateshamurthy.exceptional.RxRunnable.toRunnable;
import static io.github.venkateshamurthy.exceptional.RxSupplier.toCheckedSupplier;
import static io.github.venkateshamurthy.exceptional.exceptions.ExceptionCodes.VALIDATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A test for {@link RxTry}
 */
@ExtensionMethod({RxTry.class, RxRunnable.class, RxCallable.class, RxSupplier.class})
class RxTryTest {

    public static final String SOME_ERROR = "some error";

    @Test
    void testMapExceptionWithSupplier() {
        var e1 = new Exception("test");
        var e2 = new ArrayIndexOutOfBoundsException("arrayIOb");
        var e3 = new SQLTimeoutException("timeout");
        var i = new AtomicInteger();
        Callable<Object> test = toCallable(() -> {
            switch (i.get() % 3) {
                case 0:throw e1;
                case 1:throw e2;
                case 2:throw e3;
                default: throw e1;
            }
        });
        while (i.incrementAndGet() < 6) {
            assertThrows(IOException.class, test.tryWrap()
                    .mapException(Exception.class, () -> new IOException("exception"))::get);
            assertThrows(IOException.class, test.tryWrap().mapException(
                    Exception.class, () -> new IOException("exception"),
                    ArrayIndexOutOfBoundsException.class, () -> new IOException("AIOB"))::get);
            assertThrows(IOException.class, test.tryWrap().mapException(
                    Exception.class, () -> new IOException("exception"),
                    ArrayIndexOutOfBoundsException.class, () -> new IOException("AIOB"),
                    SQLTimeoutException.class, () -> new IOException("Timeout"))::get);
        }

        i.set(0);
        while (i.incrementAndGet() < 6) {
            assertThrows(IOException.class, test.errorMappedCallable(Exception.class, () -> new IOException("exception"))::call);
            assertThrows(IOException.class, test.errorMappedCallable(
                    Exception.class, () -> new IOException("exception"),
                    ArrayIndexOutOfBoundsException.class, () -> new IOException("AIOB"))::call);
            assertThrows(IOException.class, test.errorMappedCallable(
                    Exception.class, () -> new IOException("exception"),
                    ArrayIndexOutOfBoundsException.class, () -> new IOException("AIOB"),
                    SQLTimeoutException.class, () -> new IOException("Timeout"))::call);
        }
    }

    @Test
    void testMapExceptionWithUnary() {
        var e1 = new Exception("test");
        var e2 = new ArrayIndexOutOfBoundsException("arrayIOb");
        var e3 = new SQLTimeoutException("timeout");
        var i = new AtomicInteger();
        var checkedRunnable = toCheckedRunnable(() -> {
            switch(i.getAndIncrement()%3) {
                case 0:throw e1;
                case 1:throw e2;
                case 2:throw e3;
            }
        });
        while (i.get() < 6) {
            assertThrows(IOException.class, checkedRunnable.tryWrap().mapException(
                    Exception.class, e -> new IOException("exception"+e.getMessage()))::get);
            assertThrows(IOException.class, checkedRunnable.tryWrap().mapException(
                    Exception.class, e -> new IOException("exception"+e.getMessage()),
                    ArrayIndexOutOfBoundsException.class, e -> new IOException("AIOB"+e.getMessage()))::get);
            assertThrows(IOException.class, checkedRunnable.tryWrap().mapException(
                    Exception.class, e -> new IOException("exception"+e.getMessage()),
                    ArrayIndexOutOfBoundsException.class, e -> new IOException("AIOB"+e.getMessage()),
                    SQLTimeoutException.class, e -> new IOException("SQLTimeOut"+e.getMessage()))::get);
        }
    }

    @Test
    void testMapExceptions() {
        var e1 = new Exception("test");
        var e2 = new ArrayIndexOutOfBoundsException("arrayIOb");
        var e3 = new SQLTimeoutException("timeout");
        var i = new AtomicInteger();
        var checkedRunnable = toCheckedRunnable(() -> {
            switch(i.getAndIncrement()%3) {
                case 0:throw e1;
                case 1:throw e2;
                case 2:throw e3;
            }
        });
        while (i.get() < 6) {
            assertThrows(IOException.class, checkedRunnable.tryWrap().transformExceptions(
                    e -> new IOException("exception"+e.getMessage()), e1)::get);
            assertThrows(IOException.class, checkedRunnable.tryWrap().transformExceptions(
                    e -> new IOException("exception"), e1, e2)::get);
            assertThrows(IOException.class, checkedRunnable.tryWrap().transformExceptions(
                    e -> new IOException("exception"+e.getMessage()), e1, e2, e3)::get);
        }

    }

    @Test
    void testTryWraps() {
        assertThrows(IOException.class, toCheckedRunnable(() -> {
            throw new Exception("test");
        })
                .tryWrap()
                .mapException(Exception.class, () -> new IOException("exception"))::get);
        CheckedSupplier<String> chSupply = toCheckedSupplier(() -> {
            throw new Exception("test");
        });
        assertThrows(IOException.class, chSupply.tryWrap()
                .mapException(Exception.class, () -> new IOException("exception"))::get);
        assertThrows(IOException.class, chSupply
                .errorMappedCheckedSupplier(Exception.class, (ignore) -> new IOException("exception"))::get);
    }

    @Test
    void testCommonRTE() {
        var e1 = new Exception(SOME_ERROR);
        var e2 = new ArrayIndexOutOfBoundsException(SOME_ERROR);
        var e3 = new SQLTimeoutException(SOME_ERROR);
        var timeStamp = ZonedDateTime.now(ZoneId.of("UTC"));
        for (var e: Arrays.asList(e1,e2,e3)) {
            var cte = assertThrows(CommonRuntimeException.class,
                    ()-> {
                        throw VALIDATION_FAILED.toCommonRTE(e, "validation failed",
                                "Generic Issues during exchange of {keys}", "SHA-1").setTimeStamp(timeStamp);
                    });
            assertThat(cte).extracting(
                            CommonRuntimeException::getMessage,
                            CommonRuntimeException::getTimeStamp,
                            CommonRuntimeException::getCause,
                            CommonRuntimeException::getCode,
                            CommonRuntimeException::getDetailedMessage)
                    .containsExactly("validation failed", timeStamp, e,
                            VALIDATION_FAILED.name(), "Generic Issues during exchange of SHA-1");
            assertEquals(SOME_ERROR, cte.getCause().getMessage());
        }
    }
}
