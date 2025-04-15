package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.experimental.UtilityClass;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import static io.github.venkateshamurthy.exceptional.RxSupplier.*;

/**
 * A {@link Consumer} and {@link CheckedConsumer} wrappering utility.
 */
@UtilityClass
public class RxConsumer {
    /**
     * Reflexive function
     * @param consumer a {@link Consumer} to be returned as-is
     * @return input as is
     * @param <T> type of input
     */
    public <T> Consumer<T> toConsumer(final Consumer<T> consumer) {
        return consumer;
    }

    /**
     * Reflexive function
     * @param consumer a {@link CheckedConsumer} to be returned as-is
     * @return input as is
     * @param <T> type of input
     */
    public <T> CheckedConsumer<T> toCheckedConsumer(CheckedConsumer<T> consumer) {
        return consumer;
    }

    /**
     * Reflexive function
     * @param consumer an instance of {@link BiConsumer} to be returned as-is
     * @return input as is
     * @param <T> input
     * @param <T2> input
     */
    public <T, T2> BiConsumer<T, T2> toBiConsumer(BiConsumer<T, T2> consumer) {
        return consumer;
    }

    /**
     * Reflexive function
     * @param consumer an instance of {@link CheckedBiConsumer} to be returned as-is
     * @return input as is
     * @param <T> input
     * @param <T2> input
     */
    public <T, T2> CheckedBiConsumer<T, T2> toCheckedBiConsumer(CheckedBiConsumer<T, T2> consumer) {
        return consumer;
    }

    /**
     * A {@link Retry} wrapper to the {@link Consumer}
     *
     * @param consumer to be retried
     * @param retry    to be configured for the retry action
     * @param <T>      type of input
     * @return consumer to be wrapped
     */
    public <T> Consumer<T> retryConsumer(Consumer<T> consumer, Retry retry) {
        return r -> Retry.decorateSupplier(retry, rxSupplier(consumer, r)).get();
    }

    /**
     * A {@link RateLimiter} wrapper to the {@link Consumer}.
     *
     * @param consumer    to be wrapped
     * @param rateLimiter to be applied
     * @param <T>         type of iinput
     * @return consumer wrapped
     */
    public <T> Consumer<T> rateLimitConsumer(Consumer<T> consumer, RateLimiter rateLimiter) {
        return RateLimiter.decorateConsumer(rateLimiter, consumer);
    }

    /**
     * A {@link CircuitBreaker} wrapper to the {@link Consumer}.
     *
     * @param consumer       to be wrapped
     * @param circuitBreaker to be applied
     * @param <T>            type of iinput
     * @return consumer wrapped
     */
    public <T> Consumer<T> circuitBreakConsumer(Consumer<T> consumer, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateConsumer(circuitBreaker, consumer);
    }

    /**
     * A {@link Bulkhead} wrapper to the {@link Consumer}.
     *
     * @param consumer to be wrapped
     * @param bulkhead to be applied
     * @param <T>      type of iinput
     * @return consumer wrapped
     */
    public <T> Consumer<T> bulkheadConsumer(Consumer<T> consumer, Bulkhead bulkhead) {
        return Bulkhead.decorateConsumer(bulkhead, consumer);
    }

    /**
     * A {@link Retry} wrapper to the {@link BiConsumer}.
     *
     * @param consumer to be wrapped
     * @param retry    to be applied
     * @param <T>      type of iinput
     * @return consumer wrapped
     */
    public <T, T2> BiConsumer<T, T2> retryBiConsumer(BiConsumer<T, T2> consumer, Retry retry) {
        return (T t, T2 t2) -> Retry.decorateSupplier(retry, rxSupplier(consumer, t, t2)).get();
    }

    /**
     * A {@link RateLimiter} wrapper to the {@link BiConsumer}.
     *
     * @param consumer    to be wrapped
     * @param rateLimiter to be applied
     * @param <T>         type of iinput
     * @return consumer wrapped
     */
    public <T, T2> BiConsumer<T, T2> rateLimitBiConsumer(BiConsumer<T, T2> consumer, RateLimiter rateLimiter) {
        return (T t, T2 t2) -> RateLimiter.decorateSupplier(rateLimiter, rxSupplier(consumer, t, t2)).get();
    }

    /**
     * A {@link CircuitBreaker} wrapper to the {@link BiConsumer}.
     *
     * @param consumer       to be wrapped
     * @param circuitBreaker to be applied
     * @param <T>            type of iinput
     * @return consumer wrapped
     */
    public <T, T2> BiConsumer<T, T2> circuitBreakBiConsumer(BiConsumer<T, T2> consumer, CircuitBreaker circuitBreaker) {
        return (T t, T2 t2) -> CircuitBreaker.decorateSupplier(circuitBreaker, rxSupplier(consumer, t, t2)).get();
    }

    /**
     * A {@link Bulkhead} wrapper to the {@link BiConsumer}.
     *
     * @param consumer to be wrapped
     * @param bulkhead to be applied
     * @param <T>      type of iinput
     * @return consumer wrapped
     */
    public <T, T2> BiConsumer<T, T2> bulkheadBiConsumer(BiConsumer<T, T2> consumer, Bulkhead bulkhead) {
        return (T t, T2 t2) -> Bulkhead.decorateSupplier(bulkhead, rxSupplier(consumer, t, t2)).get();
    }

    /**
     * A {@link Retry} wrapper to the {@link CheckedConsumer}.
     *
     * @param checkedConsumer to be wrapped
     * @param retry           to be applied
     * @param <T>             type of iinput
     * @return consumer wrapped
     */
    public <T> CheckedConsumer<T> retryCheckedConsumer(CheckedConsumer<T> checkedConsumer, Retry retry) {
        return (T t) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedConsumer, t)).get();
    }

    /**
     * A {@link RateLimiter} wrapper to the {@link CheckedConsumer}.
     *
     * @param checkedConsumer to be wrapped
     * @param rateLimiter     to be applied
     * @param <T>             type of iinput
     * @return consumer wrapped
     */
    public <T> CheckedConsumer<T> rateLimitCheckedConsumer(CheckedConsumer<T> checkedConsumer, RateLimiter rateLimiter) {
        return (T t) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedConsumer, t)).get();
    }

    /**
     * A {@link CircuitBreaker} wrapper to the {@link CheckedConsumer}.
     *
     * @param checkedConsumer to be wrapped
     * @param circuitBreaker  to be applied
     * @param <T>             type of iinput
     * @return consumer wrapped
     */
    public <T> CheckedConsumer<T> circuitBreakCheckedConsumer(CheckedConsumer<T> checkedConsumer, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCheckedConsumer(circuitBreaker, checkedConsumer);
    }

    /**
     * A {@link Bulkhead} wrapper to the {@link CheckedConsumer}.
     *
     * @param checkedConsumer to be wrapped
     * @param bulkhead        to be applied
     * @param <T>             type of iinput
     * @return consumer wrapped
     */
    public <T> CheckedConsumer<T> bulkheadCheckedConsumer(CheckedConsumer<T> checkedConsumer, Bulkhead bulkhead) {
        return Bulkhead.decorateCheckedConsumer(bulkhead, checkedConsumer);
    }

    /**
     * A {@link Retry} wrapper to the {@link CheckedBiConsumer}.
     *
     * @param checkedBiConsumer to be wrapped
     * @param retry             to be applied
     * @param <T>               type of iinput
     * @return consumer wrapped
     */
    public <T, T2> CheckedBiConsumer<T, T2> retryCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, Retry retry) {
        return (T t, T2 t2) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();
    }

    /**
     * A {@link RateLimiter} wrapper to the {@link CheckedBiConsumer}.
     *
     * @param checkedBiConsumer to be wrapped
     * @param rateLimiter       to be applied
     * @param <T>               type of iinput
     * @return consumer wrapped
     */
    public <T, T2> CheckedBiConsumer<T, T2> rateLimitCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, RateLimiter rateLimiter) {
        return (T t, T2 t2) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();
    }

    /**
     * A {@link CircuitBreaker} wrapper to the {@link CheckedBiConsumer}.
     *
     * @param checkedBiConsumer to be wrapped
     * @param circuitBreaker    to be applied
     * @param <T>               type of iinput
     * @return consumer wrapped
     */
    public <T, T2> CheckedBiConsumer<T, T2> circuitBreakCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, CircuitBreaker circuitBreaker) {
        return (T t, T2 t2) -> CircuitBreaker.decorateCheckedSupplier(circuitBreaker, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();
    }

    /**
     * A {@link  Bulkhead} wrapper to the {@link CheckedBiConsumer}.
     *
     * @param checkedBiConsumer to be wrapped
     * @param bulkhead          to be applied
     * @param <T>               type of iinput
     * @return consumer wrapped
     */
    public <T, T2> CheckedBiConsumer<T, T2> bulkheadCheckedBiConsumer(CheckedBiConsumer<T, T2> checkedBiConsumer, Bulkhead bulkhead) {
        return (T t, T2 t2) -> Bulkhead.decorateCheckedSupplier(bulkhead, rxCheckedSupplier(checkedBiConsumer, t, t2)).get();
    }
}