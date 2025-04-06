package com.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.IntervalFunction;
import io.vavr.collection.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.time.Duration.*;

@RequiredArgsConstructor
public enum Delayer implements BiFunction<Duration, Duration, IntervalFunction>{
    
    DEFAULT(){
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return IntervalFunction.of(initial);
        }
    },

    DEFAULT_JITTER(){
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            val deltaMillis = RANDOM.nextDouble(0,1) * maxDelay.minus(initial).toMillis();
            return attempt -> Long.min(maxDelay.toMillis(), initial.toMillis() + Double.doubleToLongBits(deltaMillis));
        }
    },

    LINEAR_JITTER() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return attempt -> Long.min(maxDelay.toMillis(), initial.toMillis() +
                    RANDOM.nextInt(1, attempt) * maxDelay.minus(initial).toMillis());
        }
    },

    LINEAR() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return attempt -> Long.min(maxDelay.toMillis(), initial.toMillis() + (attempt - 1) * 1000);
        }
    },

    FIBONACCI() {
        final double phi = 1.6180339d;
        final double sqrt5 = 2.236067977d;
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return attempt -> Math.min(
                    maxDelay.toMillis(),
                    initial.toMillis() + round( pow(phi, attempt) / sqrt5 * 1000)
            );
        }
    },

    EXPONENTIAL() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return attempt -> IntervalFunction.ofExponentialBackoff(initial, 2, maxDelay).apply(attempt);
        }
    },

    EXPONENTIAL_JITTER() {
        @Override
        public IntervalFunction apply(final Duration initial, final Duration maxDelay) {
            return attempt -> IntervalFunction.ofExponentialRandomBackoff(initial, 2, maxDelay).apply(attempt);
        }
    };

    private static final Random RANDOM = new Random(37L);

    /** Convenience functions. */
    public IntervalFunction millis(final long initial, final long maxDelay) {
        return apply(ofMillis(initial), ofMillis(maxDelay));
    }

    public IntervalFunction seconds(final long initial, final long maxDelay) {
        return apply(ofSeconds(initial), ofSeconds(maxDelay));
    }

    public IntervalFunction minutes(final long initial, final long maxDelay) {
        return apply(ofMinutes(initial), ofMinutes(maxDelay));
    }

    public IntervalFunction hours(final long initial, final long maxDelay) {
        return apply(ofHours(initial), ofHours(maxDelay));
    }

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

    public static void main(String[] args) {
        Duration initial = ofSeconds(1);
        Duration maxDelay = ofHours(10).plusSeconds(1);

        IntStream.rangeClosed(1, 20).boxed()
                .map(i -> i + ":" +
                        //" " + DEFAULT.duration(initial, maxDelay, i).getSeconds()+
                        //" " + LINEAR.duration(initial, maxDelay, i).getSeconds()+
                        " " + (FIBONACCI.duration(initial, maxDelay, i).getSeconds() - 1)+
                        " " //+ EXPONENTIAL.duration(initial, maxDelay, i).getSeconds()+
                        //" " //+ EXPONENTIAL_JITTER.duration(initial, maxDelay, i).getSeconds()
                )
                .forEach(System.out::println);

        FIBONACCI.iterable(initial, maxDelay)
                .forEach(d->System.out.println(d.getSeconds() - 1));
    }
}