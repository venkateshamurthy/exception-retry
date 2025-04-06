package io.github.venkateshamurthy.exceptional;

@FunctionalInterface
public interface CheckedBiConsumer<T, T2>{ void accept(T t, T2 t2) throws Throwable;}