package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.experimental.ExtensionMethod;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static io.github.venkateshamurthy.exceptional.RxSupplier.*;
import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;
import static io.github.venkateshamurthy.exceptional.RxTry.rteMapper;

/**
 * An utility to wrap {@link Function}, {@link BiFunction}, {@link CheckedFunction} and {@link CheckedBiFunction}
 */
@ExtensionMethod(RxTry.class)
public class RxFunction {
    /**
     * Reflexive convenience method for chaining
     * @param function to be seen as Function
     * @return function
     * @param <T> type of input
     * @param <R> type of result
     */
    public static <T, R> Function<T, R> toFunction(Function<T, R> function) { return function::apply;}
    /**
     * Reflexive convenience method for chaining
     * @param function to be seen as CheckedFunction
     * @return function as is
     * @param <T> type of input
     * @param <R> type of result
     */
    public static <T, R> CheckedFunction<T, R> toCheckedFunction(CheckedFunction<T, R> function) { return function;}
    /**
     * Reflexive convenience method for chaining
     * @param function to be seen as BiFunction
     * @return function as is
     * @param <T> type of input
     * @param <T2> 2nd type
     * @param <R> type of result
     */
    public static <T, T2, R> BiFunction<T, T2, R> toBiFunction(BiFunction<T, T2, R> function) { return function;}
    /**
     * Reflexive convenience method for chaining
     * @param function to be seen as CheckedBiFunction
     * @return function as is
     * @param <T> type of input
     * @param <T2> 2nd type
     * @param <R> type of result
     */
    public static <T, T2, R> CheckedBiFunction<T, T2, R> toCheckedBiFunction(CheckedBiFunction<T, T2, R> function) { return function;}

    /**
     * Retry function wrapper
     * @param function a {@link Function<T, R>} to be retried
     * @param retry    a {@link Retry} instance
     * @param <T>      input type
     * @param <R>      output type
     * @return wrapped function
     */
    public static <T, R> Function<T, R> retryFunction(Function<T, R> function, Retry retry) {
        return Retry.decorateFunction(retry, function);
    }

    /**
     * RateLimiting function wrapper
     *
     * @param function    a {@link Function<T, R>} to be retried
     * @param rateLimiter a {@link RateLimiter} instance
     * @param <T>         input type
     * @param <R>         output type
     * @return wrapped function
     */
    public static <T, R> Function<T, R> rateLimitFunction(Function<T, R> function, RateLimiter rateLimiter) {
        return RateLimiter.decorateFunction(rateLimiter, function);
    }

    /**
     * CircuitBreaker function wrapper
     *
     * @param function       a {@link Function<T, R>} to be retried
     * @param circuitBreaker a {@link CircuitBreaker} instance
     * @param <T>            input type
     * @param <R>            output type
     * @return wrapped function
     */
    public static <T, R> Function<T, R> circuitBreakFunction(Function<T, R> function, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateFunction(circuitBreaker, function);
    }

    /**
     * Bulkhead function wrapper
     *
     * @param function a {@link Function<T, R>} to be retried
     * @param bulkhead a {@link Bulkhead} instance
     * @param <T>      input type
     * @param <R>      output type
     * @return wrapped function
     */
    public static <T, R> Function<T, R> bulkheadFunction(Function<T, R> function, Bulkhead bulkhead) {
        return Bulkhead.decorateFunction(bulkhead, function);
    }

    /**
     * Retry CheckedFunction wrapper
     *
     * @param checkedFunction a {@link CheckedFunction<T, R>} to be retried
     * @param retry           a {@link Retry} instance
     * @param <T>             input type
     * @param <R>             output type
     * @return wrapped checkedFunction
     */
    public static <T, R> CheckedFunction<T, R> retryCheckedFunction(CheckedFunction<T, R> checkedFunction, Retry retry) {
        return Retry.decorateCheckedFunction(retry, checkedFunction);
    }

    /**
     * Ratelimiting CheckedFunction wrapper
     *
     * @param checkedFunction a {@link CheckedFunction<T, R>} to be retried
     * @param rateLimiter     a {@link RateLimiter} instance
     * @param <T>             input type
     * @param <R>             output type
     * @return wrapped checkedFunction
     */
    public static <T, R> CheckedFunction<T, R> rateLimitCheckedFunction(CheckedFunction<T, R> checkedFunction, RateLimiter rateLimiter) {
        return RateLimiter.decorateCheckedFunction(rateLimiter, checkedFunction);
    }

    /**
     * Circuitbreaker CheckedFunction wrapper
     *
     * @param checkedFunction a {@link CheckedFunction<T, R>} to be retried
     * @param circuitBreaker  a {@link CircuitBreaker} instance
     * @param <T>             input type
     * @param <R>             output type
     * @return wrapped checkedFunction
     */
    public static <T, R> CheckedFunction<T, R> circuitBreakCheckedFunction(CheckedFunction<T, R> checkedFunction, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCheckedFunction(circuitBreaker, checkedFunction);
    }

    /**
     * Bulkhead CheckedFunction wrapper
     *
     * @param checkedFunction a {@link CheckedFunction<T, R>} to be retried
     * @param bulkhead        a {@link Bulkhead} instance
     * @param <T>             input type
     * @param <R>             output type
     * @return wrapped checkedFunction
     */
    public static <T, R> CheckedFunction<T, R> bulkheadCheckedFunction(CheckedFunction<T, R> checkedFunction, Bulkhead bulkhead) {
        return Bulkhead.decorateCheckedFunction(bulkhead, checkedFunction);
    }

    /**
     * Retry BiFunction wrapper
     *
     * @param biFunction a {@link BiFunction<T, T2, R>} to be retried
     * @param retry      a {@link Retry} instance
     * @param <T>        input type
     * @param <T2>       input type
     * @param <R>        output type
     * @return wrapped biFunction
     */
    public static <T, T2, R> BiFunction<T, T2, R> retryBiFunction(BiFunction<T, T2, R> biFunction, Retry retry) {
        return (T t, T2 t2) -> Retry.decorateSupplier(retry, rxSupplier(biFunction, t, t2)).get();
    }

    /**
     * Ratelimiter BiFunction wrapper
     *
     * @param biFunction  a {@link BiFunction<T, T2, R>} to be retried
     * @param rateLimiter a {@link RateLimiter} instance
     * @param <T>         input type
     * @param <T2>        input type
     * @param <R>         output type
     * @return wrapped biFunction
     */
    public static <T, T2, R> BiFunction<T, T2, R> rateLimitBiFunction(BiFunction<T, T2, R> biFunction, RateLimiter rateLimiter) {
        return (T t, T2 t2) -> RateLimiter.decorateSupplier(rateLimiter, rxSupplier(biFunction, t, t2)).get();
    }

    /**
     * CircuitBreaker BiFunction wrapper
     *
     * @param biFunction     a {@link BiFunction<T, T2, R>} to be retried
     * @param circuitBreaker a {@link CircuitBreaker} instance
     * @param <T>            input type
     * @param <T2>           input type
     * @param <R>            output type
     * @return wrapped biFunction
     */
    public static <T, T2, R> BiFunction<T, T2, R> circuitBreakBiFunction(BiFunction<T, T2, R> biFunction, CircuitBreaker circuitBreaker) {
        return (T t, T2 t2) -> CircuitBreaker.decorateSupplier(circuitBreaker, rxSupplier(biFunction, t, t2)).get();
    }

    /**
     * Bulkhead BiFunction wrapper
     *
     * @param biFunction a {@link BiFunction<T, T2, R>} to be retried
     * @param bulkhead   a {@link Bulkhead} instance
     * @param <T>        input type
     * @param <T2>       input type
     * @param <R>        output type
     * @return wrapped biFunction
     */
    public static <T, T2, R> BiFunction<T, T2, R> bulkheadBiFunction(BiFunction<T, T2, R> biFunction, Bulkhead bulkhead) {
        return (T t, T2 t2) -> Bulkhead.decorateSupplier(bulkhead, rxSupplier(biFunction, t, t2)).get();
    }

    /**
     * Retry CheckedBiFunction wrapper
     *
     * @param checkedBiFunction a {@link CheckedBiFunction<T, T2, R>} to be retried
     * @param retry             a {@link Retry} instance
     * @param <T>               input type
     * @param <T2>              input type
     * @param <R>               output type
     * @return wrapped checkedBiFunction
     */
    public static <T, T2, R> CheckedBiFunction<T, T2, R> retryCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, Retry retry) {
        return (T t, T2 t2) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedBiFunction, t, t2)).get();
    }

    /**
     * Retry CheckedBiFunction wrapper
     *
     * @param checkedBiFunction a {@link CheckedBiFunction<T, T2, R>} to be retried
     * @param rateLimiter       a {@link RateLimiter} instance
     * @param <T>               input type
     * @param <T2>              input type
     * @param <R>               output type
     * @return wrapped checkedBiFunction
     */
    public static <T, T2, R> CheckedBiFunction<T, T2, R> rateLimitCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, RateLimiter rateLimiter) {
        return (T t, T2 t2) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedBiFunction, t, t2)).get();
    }

    /**
     * Retry CheckedBiFunction wrapper
     *
     * @param checkedBiFunction a {@link CheckedBiFunction<T, T2, R>} to be retried
     * @param circuitBreaker    a {@link CircuitBreaker} instance
     * @param <T>               input type
     * @param <T2>              input type
     * @param <R>               output type
     * @return wrapped checkedBiFunction
     */
    public static <T, T2, R> CheckedBiFunction<T, T2, R> circuitBreakCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, CircuitBreaker circuitBreaker) {
        return (T t, T2 t2) -> CircuitBreaker.decorateCheckedSupplier(circuitBreaker, rxCheckedSupplier(checkedBiFunction, t, t2)).get();
    }

    /**
     * Retry CheckedBiFunction wrapper
     *
     * @param checkedBiFunction a {@link CheckedBiFunction<T, T2, R>} to be retried
     * @param bulkhead          a {@link Bulkhead} instance
     * @param <T>               input type
     * @param <T2>              input type
     * @param <R>               output type
     * @return wrapped checkedBiFunction
     */
    public static <T, T2, R> CheckedBiFunction<T, T2, R> bulkheadCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, Bulkhead bulkhead) {
        return (T t, T2 t2) -> Bulkhead.decorateCheckedSupplier(bulkhead, rxCheckedSupplier(checkedBiFunction, t, t2)).get();
    }

    /**
     * An exception mapped CheckedFunction wrapper
     * @param checkedFunction (@link CheckedFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex3  a {@code Class<Exception>} encountered by the callable
     * @param op3  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T,  R>
    CheckedFunction<T,  R> errorMappedCheckedFunction(
            CheckedFunction<T, R>  checkedFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2,
            Class<X3> ex3,      UnaryOperator<Exception> op3) {
        return (T t)->checkedFunction.tryWrap(t).mapException(ex, op, ex2, op2, ex3, op3)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedFunction wrapper
     * @param checkedFunction (@link CheckedFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, T,  R>
    CheckedFunction<T,  R> errorMappedCheckedFunction(
            CheckedFunction<T, R>  checkedFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2) {
        return (T t)->checkedFunction.tryWrap(t).mapException(ex, op, ex2, op2)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedFunction wrapper
     * @param checkedFunction (@link CheckedFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @return     supplier wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, T,  R>
    CheckedFunction<T,  R> errorMappedCheckedFunction(
            CheckedFunction<T, R>  checkedFunction,
            Class<X>  ex,       UnaryOperator<Exception> op) {
        return (T t)->checkedFunction.tryWrap(t).mapException(ex, op)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped Function wrapper
     * @param function (@link Function} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<RuntimeException>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex3  a {@code Class<RuntimeException>} encountered by the callable
     * @param op3  transforming {@link UnaryOperator} to transforming the ex to another exception
     * @return     function wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException,X2 extends RuntimeException, X3 extends RuntimeException,T,  R>
    Function<T,  R> errorMappedFunction(
            Function<T, R>  function,
            Class<X>  ex,   UnaryOperator<Exception> op,
            Class<X2> ex2,  UnaryOperator<Exception> op2,
            Class<X3> ex3,  UnaryOperator<Exception> op3) {
        return (T t)->function.tryWrap(t).mapException(ex, op, ex2, op2, ex3, op3)
                .getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Function wrapper
     * @param function (@link Function} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<RuntimeException>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     function wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException,X2 extends RuntimeException, T,  R>
    Function<T,  R> errorMappedFunction(
            Function<T, R>  function,
            Class<X>  ex,   UnaryOperator<Exception> op,
            Class<X2> ex2,  UnaryOperator<Exception> op2) {
        return (T t)->function.tryWrap(t).mapException(ex, op,ex2, op2)
                .getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Function wrapper
     * @param function (@link Function} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @return     function wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <T>  type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, T,  R>
    Function<T,  R> errorMappedFunction(
            Function<T, R>  function,
            Class<X>  ex,   UnaryOperator<Exception> op) {
        return (T t)->function.tryWrap(t).mapException(ex, op)
                .getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped CheckedBiFunction wrapper
     * @param checkedBiFunction (@link CheckedBiFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     checkedBiFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorMappedCheckedBiFunction(
            CheckedBiFunction<T, T2, R>  checkedBiFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2,
            Class<X3> ex3,      UnaryOperator<Exception> op3) {
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).mapException(ex, op, ex2, op2, ex3, op3)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedBiFunction wrapper
     * @param checkedBiFunction (@link CheckedBiFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<Exception>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     checkedBiFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorMappedCheckedBiFunction(
            CheckedBiFunction<T, T2, R>  checkedBiFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2){
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).mapException(ex, op, ex2, op2)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedBiFunction wrapper
     * @param checkedBiFunction (@link CheckedBiFunction} to be wrapped
     * @param ex   a {@code Class<Exception>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @return     checkedBiFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorMappedCheckedBiFunction(
            CheckedBiFunction<T, T2, R>  checkedBiFunction,
            Class<X>  ex,       UnaryOperator<Exception> op){
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).mapException(ex, op)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped BiFunction wrapper
     * @param biFunction (@link BiFunction} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<RuntimeException>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex3  a {@code Class<RuntimeException>} encountered by the callable
     * @param op3  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     biFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorMappedBiFunction(
            BiFunction<T, T2, R>  biFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2,
            Class<X3> ex3,      UnaryOperator<Exception> op3) {
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).mapException(ex, op, ex2, op2, ex3, op3)
                .getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped BiFunction wrapper
     * @param biFunction (@link BiFunction} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2  a {@code Class<RuntimeException>} encountered by the callable
     * @param op2  transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return     biFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorMappedBiFunction(
            BiFunction<T, T2, R>  biFunction,
            Class<X>  ex,       UnaryOperator<Exception> op,
            Class<X2> ex2,      UnaryOperator<Exception> op2){
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).mapException(ex, op, ex2, op2)
                .getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped BiFunction wrapper
     * @param biFunction (@link BiFunction} to be wrapped
     * @param ex   a {@code Class<RuntimeException>} encountered by the callable
     * @param op   transforming {@link UnaryOperator} to transforming the ex to another exception
     * @return     biFunction wrapped
     * @param <X>  Ist type of exception to be mapped
     * @param <T>  1st type of input
     * @param <T2> 2nd type of input
     * @param <R>  type of the result
     */
    public static <X extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorMappedBiFunction(
            BiFunction<T, T2, R>  biFunction,
            Class<X>  ex,       UnaryOperator<Exception> op){
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).mapException(ex, op)
                .getOrElseThrow(rteMapper);
    }

    //Error Consuming variant
    /**
     * An exception consuming function wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<RuntimeException>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @param ex3 a {@code Class<RuntimeException>} encountered by the callable
     * @param op3 a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, T, R>
    Function<T, R> errorConsumedFunction(
            Function<T, R> function,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming function wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<RuntimeException>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, T, R>
    Function<T, R> errorConsumedFunction(
            Function<T, R> function,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming function wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, T, R>
    Function<T, R> errorConsumedFunction(
            Function<T, R> function,
            Class<X>  ex,  Consumer<X>  op) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    //Checked Function consumption
    /**
     * An exception consuming CheckedFunction wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @param ex3 a {@code Class<Exception>} encountered by the callable
     * @param op3 a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, R>
    CheckedFunction<T, R> errorConsumedCheckedFunction(
            CheckedFunction<T, R> function,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedFunction wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends Exception, X2 extends Exception, T, R>
    CheckedFunction<T, R> errorConsumedCheckedFunction(
            CheckedFunction<T, R> function,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedFunction wrapper
     * @param function to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends Exception, T, R>
    CheckedFunction<T, R> errorConsumedCheckedFunction(
            CheckedFunction<T, R> function,
            Class<X>  ex,  Consumer<X>  op) {
        return (T t)->function.tryWrap(t).consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming BiFunction wrapper
     * @param biFunction to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<RuntimeException>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @param ex3 a {@code Class<RuntimeException>} encountered by the callable
     * @param op3 a {@link Consumer} to consume the ex encountered
     * @return biFunction wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     * @param <T> type of the input
     * @param <T2> 2nd type of the input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorConsumedBiFunction(
            BiFunction<T, T2, R> biFunction,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming BiFunction wrapper
     * @param biFunction to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<RuntimeException>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @return biFunction wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <T> type of the input
     * @param <T2> 2nd type of input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorConsumedBiFunction(
            BiFunction<T, T2, R> biFunction,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2) {
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).consumeFailure(ex, op, ex2 ,op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming BiFunction wrapper
     * @param biFunction to be wrapped
     * @param ex  a {@code Class<RuntimeException>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @return biFunction wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <T> type of the input
     * @param <T2> type of the input
     * @param <R> type of the result
     */
    public static <X extends RuntimeException, T, T2, R>
    BiFunction<T, T2, R> errorConsumedBiFunction(
            BiFunction<T, T2, R> biFunction,
            Class<X>  ex,  Consumer<X>  op) {
        return (T t, T2 t2)->biFunction.tryWrap(t, t2).consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming CheckedBiFunction wrapper
     * @param checkedBiFunction to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @param ex3 a {@code Class<Exception>} encountered by the callable
     * @param op3 a {@link Consumer} to consume the ex encountered
     * @return checkedBiFunction wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     * @param <T> type of the input
     * @param <R> type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorConsumedCheckedBiFunction(
            CheckedBiFunction<T, T2, R> checkedBiFunction,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).consumeFailure(ex, op, ex2, op2, ex3, op3)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedFunction wrapper
     * @param checkedBiFunction to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the callable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @return function wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <T> type of the input
     * @param <T2> 2nd type of input
     * @param <R> type of the result
     */
    public static <X extends Exception, X2 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorConsumedCheckedBiFunction(
            CheckedBiFunction<T, T2, R> checkedBiFunction,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2) {
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).consumeFailure(ex, op, ex2, op2)
                .getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedBiFunction wrapper
     * @param checkedBiFunction to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the callable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @return checkedBiFunction wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <T> type of the input
     * @param <T2> 2nd type of input
     * @param <R> type of the result
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T, T2, R>
    CheckedBiFunction<T, T2, R> errorConsumedCheckedBiFunction(
            CheckedBiFunction<T, T2, R> checkedBiFunction,
            Class<X>  ex,  Consumer<X>  op) {
        return (T t, T2 t2)->checkedBiFunction.tryWrap(t, t2).consumeFailure(ex, op)
                .getOrElseThrow(ceMapper);
    }
}