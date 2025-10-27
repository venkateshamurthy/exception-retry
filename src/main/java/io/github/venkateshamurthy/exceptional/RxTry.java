package io.github.venkateshamurthy.exceptional;

import com.google.common.collect.ImmutableSortedMap;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.vavr.API;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Predicates.isIn;
import static org.apache.commons.lang3.exception.ExceptionUtils.throwableOfType;

/**
 * A convenience set of utility methods making use of Try methods for callable, runnables etc
 *<p>
 *  Please notethat exception such as {@link InterruptedException}, and errors such as{@link LinkageError},
 *  {@link VirtualMachineError} and may be few others are treated as <b>fatal</b> and coded as such within
 *  {@link io.vavr.control.Try TryModule#isFatal}. So, these cannot be mapped/consumed with {@code Try}.
 *  <b>So do not try mapping {@code InterruptedException} and such above exceptions</b>
 *</p>
 * @author venkateshamurthy.
 */
@UtilityClass  @SuppressWarnings("java:S1186")
public class RxTry {
    /** A common Checked Exception Mapper. */
    public static final Function<Throwable, Exception> ceMapper = t -> throwableOfType(t, Exception.class);

    /** A common Runtime Exception Mapper. */
    public static final Function<Throwable, RuntimeException> rteMapper = t -> throwableOfType(t, RuntimeException.class);

    /** A simple class comparator based on the hierarchy so that we can favour whic of the map entries to be executed earlier.*/
    private static final Comparator<Class<?>> classComparator = new ClassHierarchyComparator();

    /**
     * Consume Failures while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Consumer that silently accepts the exception
     * @param ex3   Class&lt;Exception&gt; that can be encountered
     * @param op3   Consumer that silently accepts the exceptionr
     * @param <X1>  Exception type
     * @param <X2>  Exception type 2nd
     * @param <X3>  Exception type 3rd
     * @param <R>   Return type
     * @return Try embellished with exceptions consumed
     */
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer, Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2,
                   Class<X3> ex3, Consumer<X3> op3) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2).onFailure(ex3, op3);
    }


    /**
     * Consume Failures while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Consumer that silently accepts the exception
     * @param <X1>  Exception type
     * @param <X2>  Exception type 2nd
     * @param <R>   Return type
     * @return Try embellished with exceptions consumed
     */
    public <X1 extends Exception, X2 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer, Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2);
    }

    /**
     * Consume Failures while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Consumer that silently accepts the exception
     * @param <X>   Exception type
     * @param <R>   Return type
     * @return Try embellished with exceptions consumed
     */
    public <X extends Exception, R> Try<R> consumeFailure(Try<R> tryer, Class<X> ex, Consumer<X> op) {
        return tryer.onFailure(ex, op);
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Supplier of a new/different exception in lieu of exception encountered
     * @param <X>   Exception type
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, R> Try<R> mapException(Try<R> tryer, Class<X> ex, Supplier<Exception> op) {
        return mapExceptions(tryer, Map.of(ex, op));
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; type 1 that can be encountered
     * @param op    Supplier of a new/different exception in lieu of exception encountered
     * @param ex2   Class&lt;Exception&gt; type 2 that can be encountered
     * @param op2   transformer of exception type 2
     * @param <X>   Exception type 1
     * @param <X2>  Exception type 2
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, X2 extends Exception, R> Try<R> mapException(Try<R> tryer,
                                                                              Class<X> ex, Supplier<Exception> op,
                                                                              Class<X2> ex2, Supplier<Exception> op2) {
        return mapExceptions(tryer, ImmutableSortedMap.copyOf(Map.of(ex, op, ex2, op2), classComparator));
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; type 1 that can be encountered
     * @param op    Supplier of a new/different exception in lieu of exception encountered
     * @param ex2   Class&lt;Exception&gt; type 2 that can be encountered
     * @param op2   transformer of exception type 2
     * @param ex3   Class&lt;Exception&gt; type 3 that can be encountered
     * @param op3   transformer of exception type 3
     * @param <X>   Exception type 1
     * @param <X2>  Exception type 2
     * @param <X3>  Exception type 3
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, X2 extends Exception, X3 extends Exception, R> Try<R> mapException(Try<R> tryer,
                                                                                                    Class<X> ex, Supplier<Exception> op,
                                                                                                    Class<X2> ex2, Supplier<Exception> op2,
                                                                                                    Class<X3> ex3, Supplier<Exception> op3) {
        return mapExceptions(tryer, ImmutableSortedMap.copyOf(Map.of(ex, op, ex2, op2, ex3, op3), classComparator));
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @param <X>   Exception type
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, R> Try<R> mapException(Try<R> tryer, Class<X> ex, UnaryOperator<Exception> op) {
        return transformExceptions(tryer, Map.of(ex, op));
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Transform for the exception to be mapped to another
     * @param <X>   Exception type
     * @param <X2>  Exception type 2nd
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, X2 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return transformExceptions(tryer, ImmutableSortedMap.copyOf(Map.of(ex, op, ex2, op2), classComparator));
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer A {@link Try} to be augmented with exception consumer
     * @param ex    Class&lt;Exception&gt; that can be encountered
     * @param op    Transform for the exception to be mapped to another
     * @param ex2   Class&lt;Exception&gt; that can be encountered
     * @param op2   Transform for the exception to be mapped to another
     * @param ex3   Class&lt;Exception&gt; that can be encountered
     * @param op3   Transform for the exception to be mapped to another
     * @param <X>   Exception type
     * @param <X2>  Exception type 2nd
     * @param <X3>  Exception type 3rd
     * @param <R>   Return type
     * @return Try embellished with exceptions mapping
     */
    public <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return transformExceptions(tryer, ImmutableSortedMap.copyOf(Map.of(ex, op, ex2, op2, ex3, op3), classComparator));
    }

    /**
     * Wrapping {@link CheckedBiFunction} with a {@link Try}.
     *
     * @param checkedBiFunction the {@link CheckedBiFunction}
     * @param t                 first input argument
     * @param t2                second input argument
     * @param <T>               first input type
     * @param <T2>              second input type
     * @param <R>               result
     * @return Try wrapping {@code CheckedBiFunction}
     */
    public <T, T2, R> Try<R> tryWrap(CheckedBiFunction<T, T2, R> checkedBiFunction, T t, T2 t2) {
        return Try.of(RxSupplier.toCheckedSupplier(checkedBiFunction, t, t2)::get);
    }

    /**
     * Wrapping {@link TriFunction} with a {@link Try}.
     *
     * @param triFunction the {@link TriFunction}
     * @param t          first input argument
     * @param t2         second input argument
     * @param t3         third input argument
     * @param <T>        first input type
     * @param <T2>       second input type
     * @param <T3>       third input type
     * @param <R>        result
     * @return Try wrapping {@code TriFunction}
     */
    public  <T, T2, T3, R> Try<R> tryWrap(TriFunction<T, T2, T3, R> triFunction, T t, T2 t2, T3 t3) {
        return Try.of(RxSupplier.toSupplier(triFunction, t, t2, t3)::get);
    }

    /**
     * Wrapping {@link TriFunction} with a {@link Try}.
     *
     * @param checkedTriFunction the {@link CheckedTriFunction}
     * @param t          first input argument
     * @param t2         second input argument
     * @param t3         third input argument
     * @param <T>        first input type
     * @param <T2>       second input type
     * @param <T3>       third input type
     * @param <R>        result
     * @return Try wrappering {@code CheckedTriFunction}
     */
    public  <T, T2, T3, R> Try<R> tryWrap(CheckedTriFunction<T, T2, T3, R> checkedTriFunction, T t, T2 t2, T3 t3) {
        return Try.of(RxSupplier.toCheckedSupplier(checkedTriFunction, t, t2, t3)::get);
    }

    /**
     * Wrapping {@link BiFunction} with a {@link Try}.
     *
     * @param biFunction the {@link BiFunction}
     * @param t          first input argument
     * @param t2         second input argument
     * @param <T>        first input type
     * @param <T2>       second input type
     * @param <R>        result
     * @return Try wrapping {@code BiFunction}
     */
    public <T, T2, R> Try<R> tryWrap(BiFunction<T, T2, R> biFunction, T t, T2 t2) {
        return Try.of(RxSupplier.toSupplier(biFunction, t, t2)::get);
    }

    /**
     * Wrapping {@link CheckedFunction} with a {@link Try}.
     *
     * @param checkedFunction the {@link CheckedFunction}
     * @param t               first input argument
     * @param <T>             first input type
     * @param <R>             result
     * @return Try wrapping {@code CheckedFunction}
     */
    public <T, R> Try<R> tryWrap(CheckedFunction<T, R> checkedFunction, T t) {
        return Try.of(RxSupplier.toCheckedSupplier(checkedFunction, t)::get);
    }

    /**
     * Wrapping {@link Function} with a {@link Try}.
     *
     * @param function the {@link Function}
     * @param t        first input argument
     * @param <T>      first input type
     * @param <R>      result
     * @return Try wrapping {@code Function}
     */
    public <T, R> Try<R> tryWrap(Function<T, R> function, T t) {
        return Try.of(RxSupplier.toSupplier(function, t)::get);
    }

    /**
     * Wrapping {@link Supplier} with a {@link Try}
     *
     * @param supplier to be wrapped
     * @param <R>      type of result
     * @return Try wrapping {@code Supplier}
     */
    public <R> Try<R> tryWrap(Supplier<R> supplier) {
        return Try.ofSupplier(supplier);
    }

    /**
     * Wrapping {@link CheckedSupplier} with a {@link Try}
     *
     * @param checkedSupplier to be wrapped
     * @param <R>             type of result
     * @return Try wrapping {@code CheckedSupplier}
     */
    public <R> Try<R> tryWrap(CheckedSupplier<R> checkedSupplier) {
        return Try.of(checkedSupplier::get);
    }

    /**
     * Wrapping {@link Callable} with a {@link Try}
     *
     * @param callable to be wrapped
     * @param <R>      type of result
     * @return Try wrapping {@code Callable}
     */
    public <R> Try<R> tryWrap(Callable<R> callable) {
        return Try.ofCallable(callable);
    }

    /**
     * Wrapping {@link Runnable} with a {@link Try}
     *
     * @param runnable to be wrapped
     * @return Try wrapping {@code Runnable}
     */
    public Try<Void> tryWrap(Runnable runnable) {
        return Try.runRunnable(runnable);
    }

    /**
     * Wrapping {@link CheckedRunnable} with a {@link Try}
     *
     * @param runnable to be wrapped
     * @return Try wrapping {@code CheckedRunnable}
     */
    public Try<Void> tryWrap(CheckedRunnable runnable) {
        return Try.run(runnable::run);
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}.
     * The exception translation itself is to be sent as a map of source of exception type to a particular supplier of
     * the taget exception. Further the caller of this method can choose to order map entries according to desired order
     * of checking (For eg; one could use {@link SortedMap} to ensure the class order checking based on child class of
     * exception favoured over base classes
     *
     * @param tryer      A {@link Try} to be augmented with exception consumer
     * @param mapper     Map of exception to another exception translation
     * @param <R>        Return type
     * @return Try embellished with exception translation map
     */
    public <R> Try<R> mapExceptions(Try<R> tryer, Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        var tryRef = new AtomicReference<>(tryer);
        mapper.forEach(((ex, op) -> tryRef.set(tryRef.get().mapFailure(API.Case($(instanceOf(ex)), op)))));
        return tryRef.get();
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     * The exception translation itself is to be sent as a map of source of exception type to a particular supplier of
     * the taget exception. Further the caller of this method can choose to order map entries according to desired order
     * of checking (For eg; one could use {@link SortedMap} to ensure the class order checking based on child class of
     * exception favoured over base classes
     *
     * @param tryer      A {@link Try} to be augmented with exception consumer
     * @param mapper     Map of exception to another exception translation
     * @param <R>        Return type
     * @return Try  embellished with exception translation map
     */
    public <R> Try<R> transformExceptions(Try<R> tryer, Map<Class<? extends Exception>, UnaryOperator<Exception>> mapper) {
        var tryRef = new AtomicReference<>(tryer);
        mapper.forEach((ex, op)->tryRef.set(tryRef.get().mapFailure(API.Case($(instanceOf(ex)), op))));
        return tryRef.get();
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer            A {@link Try} to be augmented with exception consumer
     * @param op               Supplier of a new/different exception in lieu of list of exception types encountered
     * @param exceptionClasses Class&lt;? extends Exception&gt;es that can be encountered
     * @param <R>              Return type
     * @return Try embellished with exception mapping
     */
    @SafeVarargs
    public <R> Try<R> mapExceptions(Try<R> tryer, Supplier<? extends Exception> op, Class<? extends Exception>... exceptionClasses) {
        var cases = Arrays.stream(exceptionClasses).map(e -> API.Case($(isIn(e)), op)).toArray(API.Match.Case[]::new);
        return tryer.mapFailure(cases);
    }

    /**
     * Map/Transform Failures to desired exception while executing {@link Try}
     *
     * @param tryer      A {@link Try} to be augmented with exception consumer
     * @param op         UnaryOperator of a new/different exception in lieu of exceptions encountered
     * @param exceptions {@link Exception}s that can be encountered
     * @param <R>        Return type
     * @return Try
     */
    public <R> Try<R> transformExceptions(Try<R> tryer, UnaryOperator<? extends Exception> op, Exception... exceptions) {
        return tryer.mapFailure(API.Case($(isIn(exceptions)), op));
    }

    /** A simple class hierarchy comparator.*/
    private static final class ClassHierarchyComparator implements Comparator<Class<?>> {
        public int compare(Class<?> c1, Class<?> c2) {
            /* we dont use interfaces in exception class sorting so commenting
            if (c1.isInterface() && !c2.isInterface()) {return 1;}
            if (!c1.isInterface() && c2.isInterface()) {return -1;}*/
            if (c1.isAssignableFrom(c2)) {return -1;}
            if (c2.isAssignableFrom(c1)) {return  1;}
            return c1.getCanonicalName().compareTo(c2.getCanonicalName());
        }
    }

}

