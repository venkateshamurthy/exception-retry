package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.github.venkateshamurthy.exceptional.RxSupplier.rxCheckedSupplier;
import static io.github.venkateshamurthy.exceptional.RxSupplier.rxSupplier;

/** A {@link Consumer} and {@link CheckedConsumer} wrappering utility. */
public class RxConsumer {
    /**
     * A {@link Retry} wrapper to the {@link Consumer}
     * @param consumer to be retried
     * @param retry to be configured for the retry action
     * @return consumer to be wrapped
     * @param <T> type of input
     */
    public static <T> Consumer<T> retryConsumer(Consumer<T> consumer, Retry retry) {return r -> Retry.decorateSupplier(retry, rxSupplier(consumer, r)).get();}

    /**
     * A {@link RateLimiter} wrapper to the {@link Consumer}.
     * @param consumer to be wrapped
     * @param rateLimiter to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> Consumer<T> rateLimitConsumer(Consumer<T> consumer, RateLimiter rateLimiter) {return  RateLimiter.decorateConsumer(rateLimiter, consumer);}

    /**
     * A {@link CircuitBreaker} wrapper to the {@link Consumer}.
     * @param consumer to be wrapped
     * @param circuitBreaker to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> Consumer<T> circuitBreakConsumer(Consumer<T> consumer, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateConsumer(circuitBreaker, consumer);}

    /**
     * A {@link Bulkhead} wrapper to the {@link Consumer}.
     * @param consumer to be wrapped
     * @param bulkhead to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> Consumer<T> bulkheadConsumer(Consumer<T> consumer, Bulkhead bulkhead){return  Bulkhead.decorateConsumer(bulkhead, consumer);}

    /**
     * A {@link Retry} wrapper to the {@link BiConsumer}.
     * @param consumer to be wrapped
     * @param retry to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> BiConsumer<T, T2> retryBiConsumer(BiConsumer<T, T2> consumer, Retry retry){return  (T t, T2 t2) -> Retry.decorateSupplier(retry, rxSupplier(consumer, t, t2)).get();}

    /**
     * A {@link RateLimiter} wrapper to the {@link BiConsumer}.
     * @param consumer to be wrapped
     * @param rateLimiter to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> BiConsumer<T, T2> rateLimitBiConsumer(BiConsumer<T, T2> consumer, RateLimiter rateLimiter){return  (T t, T2 t2) -> RateLimiter.decorateSupplier(rateLimiter, rxSupplier(consumer, t, t2)).get();}

    /**
     * A {@link CircuitBreaker} wrapper to the {@link BiConsumer}.
     * @param consumer to be wrapped
     * @param circuitBreaker to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> BiConsumer<T, T2> circuitBreakBiConsumer(BiConsumer<T, T2> consumer, CircuitBreaker circuitBreaker){return  (T t, T2 t2) -> CircuitBreaker.decorateSupplier(circuitBreaker, rxSupplier(consumer, t, t2)).get();}

    /**
     * A {@link Bulkhead} wrapper to the {@link BiConsumer}.
     * @param consumer to be wrapped
     * @param bulkhead to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> BiConsumer<T, T2> bulkheadBiConsumer(BiConsumer<T, T2> consumer, Bulkhead bulkhead){return  (T t, T2 t2) -> Bulkhead.decorateSupplier(bulkhead, rxSupplier(consumer, t, t2)).get();}

    /**
     * A {@link Retry} wrapper to the {@link CheckedConsumer}.
     * @param checkedConsumer to be wrapped
     * @param retry to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> CheckedConsumer<T> retryCheckedConsumer(CheckedConsumer<T> checkedConsumer, Retry retry) {return (T t) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedConsumer, t)).get();}

    /**
     * A {@link RateLimiter} wrapper to the {@link CheckedConsumer}.
     * @param checkedConsumer to be wrapped
     * @param rateLimiter to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> CheckedConsumer<T> rateLimitCheckedConsumer(CheckedConsumer<T> checkedConsumer, RateLimiter rateLimiter) {return  (T t) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedConsumer, t)).get();}

    /**
     * A {@link CircuitBreaker} wrapper to the {@link CheckedConsumer}.
     * @param checkedConsumer to be wrapped
     * @param circuitBreaker to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> CheckedConsumer<T> circuitBreakCheckedConsumer(CheckedConsumer<T> checkedConsumer, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateCheckedConsumer(circuitBreaker, checkedConsumer);}
    /**
     * A {@link Bulkhead} wrapper to the {@link CheckedConsumer}.
     * @param checkedConsumer to be wrapped
     * @param bulkhead to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T> CheckedConsumer<T> bulkheadCheckedConsumer(CheckedConsumer<T> checkedConsumer, Bulkhead bulkhead){return  Bulkhead.decorateCheckedConsumer(bulkhead, checkedConsumer);}

    /**
     * A {@link Retry} wrapper to the {@link CheckedBiConsumer}.
     * @param checkedBiConsumer to be wrapped
     * @param retry to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> CheckedBiConsumer<T, T2> retryCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, Retry retry){return  (T t, T2 t2) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();}

    /**
     * A {@link RateLimiter} wrapper to the {@link CheckedBiConsumer}.
     * @param checkedBiConsumer to be wrapped
     * @param rateLimiter to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> CheckedBiConsumer<T, T2> rateLimitCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, RateLimiter rateLimiter){return  (T t, T2 t2) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();}

    /**
     * A {@link CircuitBreaker} wrapper to the {@link CheckedBiConsumer}.
     * @param checkedBiConsumer to be wrapped
     * @param circuitBreaker to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> CheckedBiConsumer<T, T2> circuitBreakCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, CircuitBreaker circuitBreaker){return  (T t, T2 t2) -> CircuitBreaker.decorateCheckedSupplier(circuitBreaker, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();}

    /**
     * A {@link  Bulkhead} wrapper to the {@link CheckedBiConsumer}.
     * @param checkedBiConsumer to be wrapped
     * @param bulkhead to be applied
     * @return consumer wrapped
     * @param <T> type of iinput
     */
    public static <T, T2> CheckedBiConsumer<T, T2> bulkheadCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, Bulkhead bulkhead){return  (T t, T2 t2) -> Bulkhead.decorateCheckedSupplier(bulkhead, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();}
}