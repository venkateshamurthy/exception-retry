package com.venkateshamurthy.exceptional;

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

public class RxSingularObservables {
    public static <T> Observable<T> retry(Observable<T> observable, Retry retry){return observable.<T>compose(RetryTransformer.of(retry));}
    public static <T> Observable<T> bulkhead(Observable<T> observable, Bulkhead bulkHead){return observable.<T>compose(BulkheadOperator.of(bulkHead));}
    public static <T> Observable<T> circuitBreak(Observable<T> observable, CircuitBreaker circuitBreaker){return observable.<T>compose(CircuitBreakerOperator.of(circuitBreaker));}
    public static <T> Observable<T> rateLimit(Observable<T> observable, RateLimiter rateLimiter){return observable.<T>compose(RateLimiterOperator.of(rateLimiter));}

    public static <T> Single<T> retry(Single<T> single, Retry retry){return single.<T>compose(RetryTransformer.of(retry));}
    public static <T> Single<T> bulkhead(Single<T> single, Bulkhead bulkHead){return single.<T>compose(BulkheadOperator.of(bulkHead));}
    public static <T> Single<T> circuitBreak(Single<T> single, CircuitBreaker circuitBreaker){return single.<T>compose(CircuitBreakerOperator.of(circuitBreaker));}
    public static <T> Single<T> rateLimit(Single<T> single, RateLimiter rateLimiter){return single.<T>compose(RateLimiterOperator.of(rateLimiter));}
}