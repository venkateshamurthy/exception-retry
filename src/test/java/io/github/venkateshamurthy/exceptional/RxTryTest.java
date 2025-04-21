package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.functions.CheckedSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.github.venkateshamurthy.exceptional.RxCallable.toCallable;
import static io.github.venkateshamurthy.exceptional.RxRunnable.toCheckedRunnable;
import static io.github.venkateshamurthy.exceptional.RxSupplier.toCheckedSupplier;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A test for {@link RxTry}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod({RxTry.class, RxRunnable.class, RxCallable.class, RxSupplier.class})
public class RxTryTest {

    @Test
    void testMapExceptionWithSupplier() {
        assertThrows(IOException.class, toCallable(() -> {throw new Exception("test");})
                .tryWrap()
                .mapException(Exception.class, ()->new IOException("exception"))::get);

    }

    @Test
    void testTryWraps() {
        assertThrows(IOException.class,
                toCheckedRunnable(() -> {throw new Exception("test");}).tryWrap()
                .mapException(Exception.class, ()->new IOException("exception"))::get);
        CheckedSupplier<String> chSupply = toCheckedSupplier(() -> {throw new Exception("test");});
        assertThrows(IOException.class, chSupply.tryWrap()
                .mapException(Exception.class, ()->new IOException("exception"))::get);
        assertThrows(IOException.class, chSupply
                .errorMappedCheckedSupplier(Exception.class, (ignore)->new IOException("exception"))::get);
    }
}
