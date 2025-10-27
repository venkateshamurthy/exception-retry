package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.rxjava3.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.rxjava3.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.rxjava3.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.rxjava3.retry.transformer.RetryTransformer;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;

/**
 * A wrappering utility for {@link Single} and {@link Observable}
 */
@UtilityClass  @SuppressWarnings("java:S1118")
public class RxSingularObservables {
    /**
     * A {@link Retry} wrappering {@link Observable}
     *
     * @param observable input
     * @param retry      to be wrappering
     * @param <T>        input type
     * @return input wrappered
     */
    public <T> Observable<T> retry(Observable<T> observable, Retry retry) {
        return observable.compose(RetryTransformer.of(retry));
    }

    /**
     * A {@link Bulkhead} wrappering {@link Observable}
     *
     * @param observable input
     * @param bulkHead   to be wrappering
     * @param <T>        input type
     * @return input wrappered
     */
    public <T> Observable<T> bulkhead(Observable<T> observable, Bulkhead bulkHead) {
        return observable.compose(BulkheadOperator.of(bulkHead));
    }

    /**
     * A {@link CircuitBreaker} wrappering {@link Observable}
     *
     * @param observable     input
     * @param circuitBreaker to be wrappering
     * @param <T>            input type
     * @return input wrappered
     */
    public <T> Observable<T> circuitBreak(Observable<T> observable, CircuitBreaker circuitBreaker) {
        return observable.compose(CircuitBreakerOperator.of(circuitBreaker));
    }

    /**
     * A {@link RateLimiter} wrappering {@link Observable}
     *
     * @param observable  input
     * @param rateLimiter to be wrappering
     * @param <T>         input type
     * @return input wrappered
     */
    public <T> Observable<T> rateLimit(Observable<T> observable, RateLimiter rateLimiter) {
        return observable.compose(RateLimiterOperator.of(rateLimiter));
    }

    /**
     * A {@link Retry} wrappering {@link Single}
     *
     * @param single input
     * @param retry  to be wrappering
     * @param <T>    input type
     * @return input wrappered
     */
    public <T> Single<T> retry(Single<T> single, Retry retry) {
        return single.compose(RetryTransformer.of(retry));
    }

    /**
     * A {@link Bulkhead} wrappering {@link Single}
     *
     * @param single   input
     * @param bulkHead to be wrappering
     * @param <T>      input type
     * @return input wrappered
     */
    public <T> Single<T> bulkhead(Single<T> single, Bulkhead bulkHead) {
        return single.compose(BulkheadOperator.of(bulkHead));
    }

    /**
     * A {@link CircuitBreaker} wrappering {@link Single}
     *
     * @param single         input
     * @param circuitBreaker to be wrappering
     * @param <T>            input type
     * @return input wrappered
     */
    public <T> Single<T> circuitBreak(Single<T> single, CircuitBreaker circuitBreaker) {
        return single.compose(CircuitBreakerOperator.of(circuitBreaker));
    }

    /**
     * A {@link RateLimiter} wrappering {@link Single}
     *
     * @param single      input
     * @param rateLimiter to be wrappering
     * @param <T>         input type
     * @return input wrappered
     */
    public <T> Single<T> rateLimit(Single<T> single, RateLimiter rateLimiter) {
        return single.compose(RateLimiterOperator.of(rateLimiter));
    }
}