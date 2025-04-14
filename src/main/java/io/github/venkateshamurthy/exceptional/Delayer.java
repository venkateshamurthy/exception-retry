package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.IntervalFunction;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.time.Duration.*;

/**
 * Enum for delay
 */
@Slf4j
@RequiredArgsConstructor
public enum Delayer implements BiFunction<Duration, Duration, IntervalFunction>{
    /** Default constant delay.*/
    DEFAULT(){
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return IntervalFunction.of(initial);
        }
    },
    /** Default delay with random jitter.*/
    DEFAULT_JITTER(){
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> {
                    val deltaMillis = RANDOM.nextDouble(0, 1) * maxDelay.minus(initial).toMillis();
                    return Long.min(maxDelay.toMillis(), initial.toMillis() + Double.doubleToLongBits(deltaMillis));
            };
        }
    },
    /** Linear delay with jitter.*/
    LINEAR_JITTER() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> Long.min(maxDelay.toMillis(), initial.toMillis() +
                    (attempt>1?RANDOM.nextInt(1, attempt):0) * maxDelay.minus(initial).toMillis());
        }
    },
    /** Linear delay.*/
    LINEAR() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> Long.min(maxDelay.toMillis(), initial.toMillis() + (attempt - 1) * 1000L);
        }
    },
    /** A delay series along the lines of fibonacci.*/
    FIBONACCI() {
        final double phi = 1.6180339d;
        final double sqrt5 = 2.236067977d;
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> Long.min(maxDelay.toMillis(),
                    initial.toMillis() + round(pow(phi, attempt) / sqrt5 * 1000));
        }
    },
    /** Exponential delay.*/
    EXPONENTIAL() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> IntervalFunction.ofExponentialBackoff(initial, 2, maxDelay).apply(attempt);
        }
    },
    /** Exponential jitter delay.*/
    EXPONENTIAL_JITTER() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            checkArgs(initial, maxDelay);
            return attempt -> IntervalFunction.ofExponentialRandomBackoff(initial, 2, maxDelay).apply(attempt);
        }
    };

    private static void checkArgs(final Duration initial, final Duration maxDelay) {
        if (initial.isNegative() || maxDelay.isZero() || maxDelay.isNegative() || maxDelay.minus(initial).isNegative() )
            throw new IllegalArgumentException(
                    String.format("initial(%s) / maxDelay(%s) must be positive and maxDelay greater than initial",
                            initial, maxDelay));
    }

    private static final Random RANDOM = new Random(37L);

    /**
     * Convenience delay function in milliseconds
     * @param initial delay in ms
     * @param maxDelay delay maximum in ms
     * @return an {@link IntervalFunction} in terms of milliseconds applying initial and max delays
     */
    public IntervalFunction millis(final long initial, final long maxDelay) {
        return apply(ofMillis(initial), ofMillis(maxDelay));
    }

    /**
     * Convenience delay function in seconds
     * @param initial delay in seconds
     * @param maxDelay delay maximum in seconds
     * @return an {@link IntervalFunction} in terms of seconds applying initial and max delays
     */
    public IntervalFunction seconds(final long initial, final long maxDelay) {
        return apply(ofSeconds(initial), ofSeconds(maxDelay));
    }

    /**
     * Convenience delay function in minutes
     * @param initial delay in minutes
     * @param maxDelay delay maximum in minutes
     * @return an {@link IntervalFunction} in terms of minutes applying initial and max delays
     */
    public IntervalFunction minutes(final long initial, final long maxDelay) {
        return minutes(initial*1d, maxDelay*1d);
    }

    /**
     * Convenience delay function in hours
     * @param initial delay in hours
     * @param maxDelay delay maximum in hours
     * @return an {@link IntervalFunction} in terms of hours applying initial and max delays
     */
    public IntervalFunction hours(final long initial, final long maxDelay) {
        return hours(initial*1d, maxDelay*1d);
    }

    /**
     * A convenient function to obtain {@link IntervalFunction} in hours preferably fractional hours
     * @param initial a double quantity that can represent decimal/fractional hours
     * @param maxDelay a double quantity that can represent decimal/fractional hours
     * @return {@code IntervalFunction}
     */
    public IntervalFunction hours(final double initial, final double maxDelay) {
        return apply(
                of(round(initial*3600_000d), ChronoUnit.MILLIS),
                of(round(maxDelay*3600_000d), ChronoUnit.MILLIS));
    }

    public IntervalFunction minutes(final double initial, final double maxDelay) {
        return apply(
                of(round(initial*60_000d), ChronoUnit.MILLIS),
                of(round(maxDelay*60_000d), ChronoUnit.MILLIS));
    }
/* commented for future use.
    public final Duration duration(Duration initial, Duration maxDelay, int attempt) {
        return Stream.ofAll(iterable(initial, maxDelay)).get(attempt-1);
    }
    public final Iterable<Duration> iterable(Duration initial, Duration maxDelay) {
        return () -> new Iterator<>() {
            int i = 0;
            final IntervalFunction func = apply(initial, maxDelay);
            Duration lastDelay = initial;
            public boolean hasNext() {return maxDelay.compareTo(lastDelay) > 0;}
            public Duration next() {return lastDelay = ofMillis(func.apply(++i));}
        };
    }
    */
}