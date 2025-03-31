package com.venkateshamurthy.exceptional;

import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.vavr.API;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static io.vavr.API.Case;
import static io.vavr.API.Match.Case;
import static io.vavr.API.Match.Pattern0;
import static org.apache.commons.lang3.exception.ExceptionUtils.throwableOfType;

@UtilityClass
class RxTry {

    public  <R> Callable<R> callOrElseThrow(@NonNull Callable<R> callable,
                                           @NonNull UnaryOperator<Exception> mapper) {
        return () -> Try.ofCallable(callable).getOrElseThrow(t -> mapper.apply(throwableOfType(t, Exception.class)));
    }

    public  <R> Callable<R> callOrElseThrow(@NonNull Callable<R> callable,
                                            @NonNull Pair<Class<Exception>, UnaryOperator<Exception>>... mappers) {
        Case<Exception, Exception>[] cases = Arrays.stream(mappers)
                .map(mapper -> Case(Pattern0.of(mapper.getLeft()), mapper.getRight()))
                .toArray(Case[]::new);
        return () -> Try.ofCallable(callable).mapFailure(cases)
                .getOrElseThrow(t -> throwableOfType(t, Exception.class));
    }

    public  <R> Callable<R> callSilently(@NonNull Callable<R> callable,
                                         @NonNull Consumer<Exception> errorConsumer) {
        return () -> Try.ofCallable(callable).onFailure(Exception.class, errorConsumer).get();
    }

    public  <R> Callable<R> callSilently(@NonNull Callable<R> callable) {
        return callSilently(callable, e->{});
    }

    public  Runnable runOrElseThrow( @NonNull final Runnable runnable,
                                     @NonNull final UnaryOperator<RuntimeException> mapper) {
        return () -> Try.run(runnable::run).getOrElseThrow(t -> mapper.apply(throwableOfType(t, RuntimeException.class)));
    }

    public  Runnable runOrElseThrow( @NonNull final Runnable runnable,
                                     @NonNull Pair<Class<Exception>, UnaryOperator<Exception>>... mappers) {
        Case<Exception, Exception>[] cases = Arrays.stream(mappers)
                .map(mapper -> Case(Pattern0.of(mapper.getLeft()), mapper.getRight()))
                .toArray(Case[]::new);
        return () -> Try.run(runnable::run).mapFailure(cases)
                .getOrElseThrow(t -> throwableOfType(t, RuntimeException.class));
    }

    public  Runnable runSilently(@NonNull final Runnable runnable,
                                 @NonNull final Consumer<RuntimeException> errorConsumer) {
        return () -> Try.run(runnable::run).onFailure(RuntimeException.class, errorConsumer);
    }

    public  Runnable runSilently(@NonNull final Runnable runnable) {
        return runSilently(runnable, e->{});
    }
}

