package com.venkateshamurthy.exceptional.pojo;

import io.github.resilience4j.core.functions.Either;
import io.vavr.control.Try;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface HelloWorldService {
    String greeting = "Hello World!";
    default String returnHelloWorld() {
        return greeting;
    }

    default Future<String> returnHelloWorldFuture() {
        return  CompletableFuture.completedFuture(greeting);
    }

    default Either<RuntimeException, String> returnEither(){
        return Either.right(greeting);
    }

    default Try<String> returnTry() {
        return Try.of(() -> greeting);
    }

    String returnHelloWorldWithException() throws IOException;

    String returnHelloWorldWithName(String name);

    String returnHelloWorldWithNameWithException(String name) throws IOException;

    void sayHelloWorld();

    void sayHelloWorldWithException() throws IOException;

    void sayHelloWorldWithName(String name);

    void sayHelloWorldWithNameWithException(String name) throws IOException;
}