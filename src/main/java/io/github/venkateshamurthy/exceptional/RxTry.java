package io.github.venkateshamurthy.exceptional;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.vavr.API;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Callable;
import java.util.function.*;

import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;
import static io.github.venkateshamurthy.exceptional.RxSupplier.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.throwableOfType;

/**
 * A convenience set of utility methods making use of Try methods for callable, runnables etc
 * @author venkateshamurthy.
 */
@UtilityClass
public class RxTry {
    /** A common Checked Exception Mapper .*/
    public static final Function<Throwable, Exception> ceMapper = t -> throwableOfType(t, Exception.class);

    /** A common Runtime Exception Mapper.*/
    public static final Function<Throwable, RuntimeException> rteMapper = t -> throwableOfType(t, RuntimeException.class);

    /**
     * Consume Failures while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Consumer that silently accepts the exception
     * @param ex3   Class&lt;Exception&gt; that can be encountered
     * @param op3   Consumer that silently accepts the exceptionr
     * @return      Try
     * @param <X1>  Exception type
     * @param <X2>  Exception type 2nd
     * @param <X3>  Exception type 3rd
     * @param <R>   Return type
     */
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer, Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2, Class<X3> ex3, Consumer<X3> op3) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2).onFailure(ex3, op3);
    }

    /**
     * Consume Failures while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Consumer that silently accepts the exception
     * @return      Try
     * @param <X1>  Exception type
     * @param <X2>  Exception type 2nd
     * @param <R>   Return type
     */
    public <X1 extends Exception, X2 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer,Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2);
    }

    /**
     * Consume Failures while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @return      Try
     * @param <X>   Exception type
     * @param <R>   Return type
     */
    public <X extends Exception, R> Try<R> consumeFailure(Try<R> tryer,Class<X> ex, Consumer<X> op) {
        return tryer.onFailure(ex, op);
    }

    /**
     * Map/Trnsform Failures to desired exception while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Supplier of a new/different exception in lieu of exception encountered
     * @return      Try
     * @param <X>   Exception type
     * @param <R>   Return type
     */
    public <X extends Exception,R> Try<R> mapException(Try<R> tryer, Class<X> ex, Supplier<? extends Exception> op) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op));
    }

    /**
     * Map/Trnsform Failures to desired exception while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @return      Try
     * @param <X>   Exception type
     * @param <R>   Return type
     */
    public <X extends Exception,R> Try<R> mapException(Try<R> tryer,Class<X> ex, UnaryOperator<Exception> op) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op));
    }

    /**
     * Map/Trnsform Failures to desired exception while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Transform for the exception to be mapped to another
     * @return      Try
     * @param <X>   Exception type
     * @param <X2>  Exception type 2nd
     * @param <R>   Return type
     */
    public <X extends Exception, X2 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op),
                                API.Case($(instanceOf(ex2)), op2));
    }

    /**
     * Map/Trnsform Failures to desired exception while executing {@link Try}
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Transform for the exception to be mapped to another
     * @param ex3   Class&lt;Exception&gt; that can be encountered
     * @param op3   Transform for the exception to be mapped to another
     * @return      Try
     * @param <X>   Exception type
     * @param <X2>  Exception type 2nd
     * @param <X3>  Exception type 3rd
     * @param <R>   Return type
     */
    public <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op),
                                API.Case($(instanceOf(ex2)), op2),
                                API.Case($(instanceOf(ex3)), op3));
    }

    /**
     * Wrapping {@link CheckedBiFunction} with a {@link Try}.
     *
     * @param checkedBiFunction the {@link CheckedBiFunction}
     * @param t    first input argument
     * @param t2   second input argument
     * @param <T>  first input type
     * @param <T2> second input type
     * @param <R>  result
     */
    public <T, T2, R> Try<R> tryWrap(CheckedBiFunction<T, T2, R> checkedBiFunction, T t, T2 t2){
        return Try.of(rxCheckedSupplier(checkedBiFunction, t, t2)::get);
    }

    /**
     * Wrapping {@link BiFunction} with a {@link Try}.
     *
     * @param biFunction the {@link BiFunction}
     * @param t    first input argument
     * @param t2   second input argument
     * @param <T>  first input type
     * @param <T2> second input type
     * @param <R>  result
     */
    public <T, T2, R> Try<R> tryWrap(BiFunction<T, T2, R> biFunction, T t, T2 t2){
        return Try.of(RxSupplier.rxSupplier(biFunction, t, t2)::get);
    }

    /**
     * Wrapping {@link CheckedFunction} with a {@link Try}.
     *
     * @param checkedFunction the {@link CheckedFunction}
     * @param t    first input argument
     * @param <T>  first input type
     * @param <R>  result
     */
    public <T,  R> Try<R> tryWrap(CheckedFunction<T,  R> checkedFunction, T t){
        return Try.of(RxSupplier.rxCheckedSupplier(checkedFunction, t)::get);
    }

    /**
     * Wrapping {@link Function} with a {@link Try}.
     *
     * @param function the {@link Function}
     * @param t    first input argument
     * @param <T>  first input type
     * @param <R>  result
     */
    public <T, R> Try<R> tryWrap(Function<T,  R> function, T t){
        return Try.of(RxSupplier.rxSupplier(function, t)::get);
    }

    /** Wrapping {@link Supplier} with a {@link Try}.*/
    public <R> Try<R> tryWrap(Supplier<R> supplier){return Try.ofSupplier(supplier);}

    /** Wrapping {@link CheckedSupplier} with a {@link Try}.*/
    public  <R> Try<R> tryWrap(CheckedSupplier<R> checkedSupplier){return Try.of(checkedSupplier::get);}

    /** Wrapping {@link Callable} with a {@link Try}.*/
    public <R> Try<R> tryWrap(Callable<R> callable) {return Try.ofCallable(callable);}

    /** Wrapping {@link Runnable} with a {@link Try}.*/
    public Try<Void>  tryWrap(Runnable runnable){return Try.runRunnable(runnable);}

    /** Wrapping {@link CheckedRunnable} with a {@link Try}.*/
    public Try<Void>  tryWrap(CheckedRunnable runnable){return Try.run(runnable::run);}

}

