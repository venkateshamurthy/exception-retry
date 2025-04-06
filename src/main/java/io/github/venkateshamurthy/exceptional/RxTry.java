package io.github.venkateshamurthy.exceptional;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.vavr.API;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Callable;
import java.util.function.*;

import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;
import static org.apache.commons.lang3.exception.ExceptionUtils.throwableOfType;

@UtilityClass
class RxTry {
    public static final Function<Throwable, Exception> ceMapper = t -> throwableOfType(t, Exception.class);
    public static final Function<Throwable, RuntimeException> rteMapper = t -> throwableOfType(t, RuntimeException.class);

    public <X1 extends Exception, X2 extends Exception, X3 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer, Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2, Class<X3> ex3, Consumer<X3> op3) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2).onFailure(ex3, op3);
    }

    public <X1 extends Exception, X2 extends Exception, R> Try<R>
    consumeFailure(Try<R> tryer,Class<X1> ex, Consumer<X1> op, Class<X2> ex2, Consumer<X2> op2) {
        return tryer.onFailure(ex, op).onFailure(ex2, op2);
    }

    public <X extends Exception, R> Try<R> consumeFailure(Try<R> tryer,Class<X> ex, Consumer<X> op) {
        return tryer.onFailure(ex, op);
    }


    public <X extends Exception,R> Try<R> mapException(Try<R> tryer, Class<X> ex, Supplier<? extends Exception> op) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op));
    }

    public <X extends Exception,R> Try<R> mapException(Try<R> tryer,Class<X> ex, UnaryOperator<Exception> op) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op));
    }

    public <X extends Exception, X2 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op),
                                API.Case($(instanceOf(ex2)), op2));
    }

    public <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    Try<R> mapException(
            Try<R> tryer,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return tryer.mapFailure(API.Case($(instanceOf(ex)), op),
                                API.Case($(instanceOf(ex2)), op2),
                                API.Case($(instanceOf(ex3)), op3));
    }

    public <R> Try<R> tryWrap(Supplier<R> supplier){return Try.ofSupplier(supplier);}
    public <R> Try<R> tyrWrap(CheckedSupplier<R> checkedSupplier){return Try.of(checkedSupplier::get);}
    public <R> Try<R> tryWrap(Callable<R> callable) {return Try.ofCallable(callable);}
    public Try<Void> tryWrap(Runnable runnable){return Try.runRunnable(runnable);}
    public Try<Void> tryWrap(CheckedRunnable runnable){return Try.run(runnable::run);}

    /*

    public  Runnable runnable( @NonNull final Runnable runnable,
                                     @NonNull final UnaryOperator<RuntimeException> mapper) {
        return () -> Try.run(runnable::run)¸;
    }

    public  Runnable runnable( @NonNull final Runnable runnable,
                               @NonNull Class<? extends RuntimeException> ex, UnaryOperator<? extends RuntimeException> op) {
        return () -> Try.run(runnable::run)
                .mapFailure(API.Case(Pattern0.of(ex), op))
                .getOrElseThrow(t -> throwableOfType(t, RuntimeException.class));
    }
    public  Runnable runnable( @NonNull final Runnable runnable,
                               @NonNull Class<? extends RuntimeException> ex, UnaryOperator<? extends RuntimeException> op,
                               @NonNull Class<? extends RuntimeException> ex2, UnaryOperator<? extends RuntimeException> op2) {
        return () -> Try.run(runnable::run)
                .mapFailure(API.Case(Pattern0.of(ex), op),
                            API.Case(Pattern0.of(ex2), op2))
                .getOrElseThrow(t -> throwableOfType(t, RuntimeException.class));
    }

    public  Runnable runnable(@NonNull final Runnable runnable,
                                 @NonNull final Consumer<RuntimeException> errorConsumer) {
        return () -> Try.run(runnable::run).onFailure(RuntimeException.class, errorConsumer);
    }

    public  Runnable runnable(@NonNull final Runnable runnable) {
        return runnable(runnable, e->{});
    }
    */
}

