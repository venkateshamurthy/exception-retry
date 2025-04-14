package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;

import java.util.function.*;

import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;
import static io.github.venkateshamurthy.exceptional.RxTry.rteMapper;

/**
 * A convenient wrapper to {@link RxSupplier}
 */
@ExtensionMethod(RxTry.class)
public class RxSupplier {
    /** A reflexive CheckedSupplier wrapper.*/
    public static <R> CheckedSupplier<R> toCheckedSupplier(CheckedSupplier<R> supplier) {return supplier;}

    /** A reflexive Supplier wrapper.*/
    public static <R> Supplier<R> toSupplier(Supplier<R> supplier) {return supplier;}

    /** A {@link Retry} wrapper for the {@link Supplier}.*/
    public static <R> Supplier<R> retrySupplier(Supplier<R> supplier, Retry retry){ return Retry.decorateSupplier(retry, supplier);}

    /** A {@link RateLimiter} wrapper for the {@link Supplier}.*/
    public static <R> Supplier<R> rateLimitSupplier(Supplier<R> supplier, RateLimiter rateLimiter){ return RateLimiter.decorateSupplier(rateLimiter, supplier);}

    /** A {@link CircuitBreaker} wrapper for the {@link Supplier}.*/
    public static <R> Supplier<R> circuitBreakSupplier(Supplier<R> supplier, CircuitBreaker circuitBreaker){ return  CircuitBreaker.decorateSupplier(circuitBreaker, supplier);}

    /** A {@link Bulkhead} wrapper for the {@link Supplier}.*/
    public static <R> Supplier<R> bulkheadSupplier(Supplier<R> supplier, Bulkhead bulkhead){ return  Bulkhead.decorateSupplier(bulkhead, supplier); }

    /** A {@link Retry} wrapper for the {@link CheckedSupplier}.*/
    public static <R> CheckedSupplier<R> retryCheckedSupplier(CheckedSupplier<R> supplier, Retry retry){ return Retry.decorateCheckedSupplier(retry, supplier);}

    /** A {@link RateLimiter} wrapper for the {@link CheckedSupplier}.*/
    public static <R> CheckedSupplier<R> rateLimitCheckedSupplier(CheckedSupplier<R> supplier, RateLimiter rateLimiter){ return RateLimiter.decorateCheckedSupplier(rateLimiter, supplier);}

    /** A {@link CircuitBreaker} wrapper for the {@link CheckedSupplier}.*/
    public static <R> CheckedSupplier<R> circuitBreakCheckedSupplier(CheckedSupplier<R> supplier, CircuitBreaker circuitBreaker){ return  CircuitBreaker.decorateCheckedSupplier(circuitBreaker, supplier);}

    /** A {@link Bulkhead} wrapper for the {@link CheckedSupplier}.*/
    public static <R> CheckedSupplier<R> bulkheadCheckedSupplier(CheckedSupplier<R> supplier, Bulkhead bulkhead){ return  Bulkhead.decorateCheckedSupplier(bulkhead, supplier); }

    // Convenience transforms from other forms to supplier
    /** A  {@link Supplier} form of {@link Consumer}.*/
    public static <T>      Supplier<Void> rxSupplier(Consumer<T> bic, T t) {return () -> {bic.accept(t);return null;};}

    /** A  {@link Supplier} form of {@link BiConsumer}.*/
    public static <T,T2>   Supplier<Void> rxSupplier(BiConsumer<T, T2> bic, T t, T2 t2) {return () -> {bic.accept(t, t2);return null;};}

    /** A  {@link Supplier} form of {@link BiFunction}.*/
    public static <T,T2,R> Supplier<R>    rxSupplier(BiFunction<T, T2, R> bic, T t, T2 t2) {return () -> bic.apply(t, t2);}

    /** A  {@link Supplier} form of {@link Function}.*/
    public static <T,R>    Supplier<R>    rxSupplier(Function<T, R> f, T t) {return () -> f.apply(t); }

    /** A  {@link CheckedSupplier} form of {@link CheckedConsumer}.*/
    public static <T>      CheckedSupplier<Void> rxCheckedSupplier(CheckedConsumer<T> bic, T t)   { return () ->  { bic.accept(t); return null;};}

    /** A  {@link CheckedSupplier} form of {@link CheckedBiConsumer}.*/
    public static <T,T2>   CheckedSupplier<Void> rxCheckedSupplier(CheckedBiConsumer<T, T2> bic, T t, T2 t2)    { return () -> { bic.accept(t, t2); return null;};}

    /** A  {@link CheckedSupplier} form of {@link CheckedConsumer}.*/
    public static <T,T2, R>CheckedSupplier<R>    rxCheckedSupplier(CheckedBiFunction<T, T2, R> bif, T t, T2 t2) { return () -> bif.apply(t, t2);}

    /** A  {@link CheckedSupplier} form of {@link CheckedFunction}.*/
    public static <T,R>    CheckedSupplier<R>    rxCheckedSupplier(CheckedFunction<T, R> f, T t)  { return () -> f.apply(t); }

    /**
     * An exception mapped supplier wrapper
     * @param supplier to be wrapped
     * @param ex a {@link Class<RuntimeException>} encountered by the callable
     * @param op  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2 a {@link Class<RuntimeException>} encountered by the callable
     * @param op2 transforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @param ex3 a {@link Class<RuntimeException>} encountered by the callable
     * @param op3 transforming {@link UnaryOperator} to transforming the ex3 to another exceotion
     * @return supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException,X3 extends RuntimeException,R>
    Supplier<R> errorMappedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->supplier.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2  a {@link Class<RuntimeException>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException,R>
    Supplier<R> errorMappedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->supplier.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, R>
    Supplier<R> errorMappedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  UnaryOperator<Exception> op){
        return ()->supplier.tryWrap().mapException(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @param ex2  a {@link Class<RuntimeException>} encountered by the callable
     * @param op2  a {@link Consumer} to consume the ex encountered
     * @param ex3  a {@link Class<RuntimeException>} encountered by the callable
     * @param op3  a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->supplier.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @param ex2  a {@link Class<RuntimeException>} encountered by the callable
     * @param op2  a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2){
        return ()->supplier.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(
            Supplier<R>    supplier,
            Class<X>  ex,  Consumer<X> op){
        return ()->supplier.tryWrap().consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper.
     * @param supplier a {@link CheckedSupplier} to be wrapped
     * @param ex   a {@link Class<Exception>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <R>  type of the result
     */
    @SneakyThrows
    public static <X extends Exception, R>
    CheckedSupplier<R> errorConsumedCheckedSupplier(
            CheckedSupplier<R> supplier,
            //Remember you cant use checked consumer here
            Class<X>  ex,      Consumer<X> op){
        return ()->supplier.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<Exception>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @param ex2  a {@link Class<Exception>} encountered by the callable
     * @param op2  a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, R>
    CheckedSupplier<R> errorConsumedSupplier(
            CheckedSupplier<R>    supplier,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2){
        return ()->supplier.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<Exception>} encountered by the callable
     * @param op   a {@link Consumer} to consume the ex encountered
     * @param ex2  a {@link Class<Exception>} encountered by the callable
     * @param op2  a {@link Consumer} to consume the ex encountered
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception,X3 extends Exception, R>
    CheckedSupplier<R> errorConsumedSupplier(
            CheckedSupplier<R>    supplier,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3){
        return ()->supplier.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped supplier wrapper
     * @param supplier to be wrapped
     * @param ex   a {@link Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <R>  type of the result
     */
    public static <X extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(
            CheckedSupplier<R>  supplier,
            Class<X>  ex,       UnaryOperator<Exception> op){
        return ()->supplier.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedSupplier wrapper
     * @param supplier (@link CheckedSupplier} to be wrapped
     * @param ex   a {@link Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@link Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(
            CheckedSupplier<R>  supplier,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2 ) {
        return ()->supplier.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedSupplier wrapper
     * @param supplier (@link CheckedSupplier} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(
            CheckedSupplier<R>  supplier,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2,
            Class<X3> ex3,      UnaryOperator<Exception> op3) {
        return ()->supplier.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }
}