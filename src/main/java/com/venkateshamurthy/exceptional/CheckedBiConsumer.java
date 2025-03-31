package com.venkateshamurthy.exceptional;

import lombok.SneakyThrows;

@FunctionalInterface
public interface CheckedBiConsumer<T, T2>{ void accept(T t, T2 t2) throws Throwable;}