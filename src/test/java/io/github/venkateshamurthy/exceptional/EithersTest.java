package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.functions.*  ;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.*;

import static io.github.venkateshamurthy.exceptional.RxCallable.toCallable;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtensionMethod({Eithers.class, RxCallable.class, RxTry.class})
class EithersTest {
    public static final String greeting = "Hello World!";
    public static final Supplier<Exception> IO_EXCEPTION_SUPPLIER = () -> new IOException("Illegal State");
    public static final UnaryOperator<Exception> IO_EXCEPTION_MAPPER = e -> new IOException("Illegal State", e);
    public static final Supplier<Exception> ILLEGAL_STATE_SUPPLIER = () -> new IllegalStateException("Illegal State");
    public static final UnaryOperator<Exception> ILLEGAL_STATE_MAPPER = e -> new IllegalStateException("Illegal State", e);
    private static Map<Class<? extends Exception>, Supplier<Exception>> map=
            Map.of(IllegalArgumentException.class, IllegalStateException::new,
                    NullPointerException.class, IllegalStateException::new,
                    TimeoutException.class, IOException::new);
    @Test
    void testOrElse(){
        assertEquals(greeting.toLowerCase(), toCallable(greeting::toLowerCase).tryWrap().tryToEither().orElseThrow());
        final String something = null;
        assertThrows(NullPointerException.class, ()->toCallable(something::toLowerCase).tryWrap().tryToEither().orElseThrow());
    }
    @Test
    void testCallables() {
        final Callable<String> call = greeting::toUpperCase;
        final Callable<String> callThrowing = ()->{throw new TimeoutException();};

        assertDoesNotThrow(call.either(IO_EXCEPTION_SUPPLIER)::get);
        assertDoesNotThrow(call.either(IO_EXCEPTION_MAPPER)::get);
        assertDoesNotThrow(call.either(map)::get);
        assertDoesNotThrow(call.either()::get);

        assertThrows(TimeoutException.class, () -> callThrowing.either().orElseThrow());
        assertEquals(IOException.class, callThrowing.either( IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, callThrowing.either(IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, callThrowing.either(map).getLeft().getClass());
    }

    @Test
    void testSuppliers() {
        final Supplier<String> s = ()->greeting;
        final CheckedSupplier<String> cs = ()->{throw new TimeoutException();};

        assertDoesNotThrow(s.either( ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(s.either(ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(s.either( map)::get);
        assertDoesNotThrow(s.either()::get);

        assertThrows(TimeoutException.class, () -> cs.either().orElseThrow());
        assertEquals(IOException.class, cs.either( IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cs.either(IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cs.either(map).getLeft().getClass());

    }
    @Test
    void testFunctions() {
        final Function<String, String> f = String::toUpperCase;
        final BiFunction<String, String, String> bif = String::concat;
        final TriFunction<String, String, String, String> trif = String::join;
        final CheckedFunction<String, String> cf = ignore -> {throw new TimeoutException();};
        final CheckedBiFunction<String, String, String> cbif = (a,b) -> {throw new TimeoutException(a+" "+b);};
        final CheckedTriFunction<String, String, String, String> ctrif = (a,b, c ) ->
            {throw new TimeoutException(a+" "+b+" "+c);};

        assertDoesNotThrow(f.either(greeting, ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(f.either(greeting, ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(f.either(greeting, map)::get);
        assertDoesNotThrow(f.either(greeting)::get);

        assertDoesNotThrow(bif.either(greeting, greeting, ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(bif.either(greeting, greeting, ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(bif.either(greeting, greeting, map)::get);
        assertDoesNotThrow(bif.either(greeting, greeting)::get);

        assertDoesNotThrow(trif.either(greeting, greeting, greeting, ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(trif.either(greeting, greeting, greeting, ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(trif.either(greeting, greeting, greeting, map)::get);
        assertDoesNotThrow(trif.either(greeting, greeting, greeting)::get);

        assertThrows(NullPointerException.class, () -> f.either(null).orElseThrow());
        assertEquals(IllegalStateException.class, f.either(null, ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, f.either(null, ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, f.either(null, map).getLeft().getClass());

        assertThrows(NullPointerException.class, () -> bif.either(null,null).orElseThrow());
        assertEquals(IllegalStateException.class, bif.either(null,null, ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, bif.either(null,null, ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, bif.either(null,null, map).getLeft().getClass());

        assertThrows(NullPointerException.class, () -> trif.either(null,null,null).orElseThrow());
        assertEquals(IllegalStateException.class, trif.either(null,null,null, ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, trif.either(null,null,null, ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, trif.either(null,null,null, map).getLeft().getClass());

        assertThrows(TimeoutException.class, () -> cf.either(null).orElseThrow());
        assertEquals(IOException.class, cf.either(null, IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cf.either(null, IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cf.either(null, map).getLeft().getClass());

        assertThrows(TimeoutException.class, () -> cbif.either(null,null).orElseThrow());
        assertEquals(IOException.class, cbif.either(null,null, IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cbif.either(null,null, IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cbif.either(null,null, map).getLeft().getClass());

        assertThrows(TimeoutException.class, () -> ctrif.either(null,null, null).orElseThrow());
        assertEquals(IOException.class, ctrif.either(null, null,null, IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, ctrif.either(null,null,null, IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, ctrif.either(null,null,null, map).getLeft().getClass());

    }

    @Test
    void testConsumers() {
        final Consumer<String> c = a -> log.debug("Consumer:{}",a);
        final Consumer<String> cThrowing = a -> {throw new IllegalArgumentException(a);};
        final CheckedConsumer<String> cc = a -> {throw new TimeoutException(a);};
        final BiConsumer<String,String> bic = (a,b) -> log.debug("BiConsumer:{}:{}",a,b);
        final BiConsumer<String,String> bicThrowing = (a,b) -> {throw new IllegalArgumentException(a+" "+b);};
        final CheckedBiConsumer<String, String> cbicThrowing = (a, b) -> {throw new TimeoutException(a+" "+b);};

        assertDoesNotThrow(c.either(greeting, ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(c.either(greeting, ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(c.either(greeting, map)::get);
        assertDoesNotThrow(c.either(greeting)::get);

        assertThrows(IllegalArgumentException.class, () -> cThrowing.either(greeting).orElseThrow());
        assertEquals(IllegalStateException.class, cThrowing.either(greeting, ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, cThrowing.either(greeting, ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, cThrowing.either(greeting, map).getLeft().getClass());

        assertThrows(TimeoutException.class, () -> cc.either(greeting).orElseThrow());
        assertEquals(IOException.class, cc.either(greeting, IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cc.either(greeting, IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cc.either(greeting, map).getLeft().getClass());

        assertDoesNotThrow(bic.either(greeting, greeting, ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(bic.either(greeting, greeting, ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(bic.either(greeting, greeting, map)::get);
        assertDoesNotThrow(bic.either(greeting, greeting)::get);

        assertThrows(IllegalArgumentException.class, () -> bicThrowing.either(greeting, greeting).orElseThrow());
        assertEquals(IllegalStateException.class, bicThrowing.either(greeting, greeting, ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, bicThrowing.either(greeting, greeting, ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, bicThrowing.either(greeting, greeting, map).getLeft().getClass());

        assertThrows(TimeoutException.class, () -> cbicThrowing.either(greeting, greeting).orElseThrow());
        assertEquals(IOException.class, cbicThrowing.either(greeting, greeting, IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cbicThrowing.either(greeting, greeting, IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cbicThrowing.either(greeting, greeting, map).getLeft().getClass());
    }

    @Test
    void testRunnables() {
         final Runnable r = ()-> log.debug("{}",greeting);
         final Runnable rThrowing = ()->{throw new IllegalArgumentException();};
         final CheckedRunnable cr = ()->{throw new TimeoutException();};

        assertDoesNotThrow(r.either(ILLEGAL_STATE_SUPPLIER)::get);
        assertDoesNotThrow(r.either(ILLEGAL_STATE_MAPPER)::get);
        assertDoesNotThrow(r.either( map)::get);
        assertDoesNotThrow(r.either()::get);

        assertThrows(TimeoutException.class, ()->cr.either().orElseThrow());
        assertEquals(IOException.class, cr.either(IO_EXCEPTION_SUPPLIER).getLeft().getClass());
        assertEquals(IOException.class, cr.either(IO_EXCEPTION_MAPPER).getLeft().getClass());
        assertEquals(IOException.class, cr.either(  map).getLeft().getClass());

        assertThrows(IllegalArgumentException.class, ()->rThrowing.either().orElseThrow());
        assertEquals(IllegalStateException.class, rThrowing.either(  ILLEGAL_STATE_SUPPLIER).getLeft().getClass());
        assertEquals(IllegalStateException.class, rThrowing.either( ILLEGAL_STATE_MAPPER).getLeft().getClass());
        assertEquals(IllegalStateException.class, rThrowing.either(  map).getLeft().getClass());
    }
}
