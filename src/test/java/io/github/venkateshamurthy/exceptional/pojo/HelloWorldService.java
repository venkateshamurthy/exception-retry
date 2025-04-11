package io.github.venkateshamurthy.exceptional.pojo;

import io.github.resilience4j.core.functions.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public interface HelloWorldService {
    AtomicBoolean invoked = new AtomicBoolean(false);
    String greeting = "Hello World!";
    AtomicBoolean throwException = new AtomicBoolean(false);
    Logger log = LoggerFactory.getLogger(HelloWorldService.class);
    default boolean shouldThrow() { return throwException.get(); }
    default boolean isInvoked() { return invoked.get(); }
    default HelloWorldService setInvoked() { invoked.set(true); return this; }
    default HelloWorldService throwException() { throwException.set(true); return this; }
    default HelloWorldService resetInvoked() { invoked.set(false); return this; }
    default HelloWorldService revokeException() { throwException.set(false); return this; }

    default String returnHelloWorld() {
        setInvoked();
        return greeting;
    }

    default Future<String> returnHelloWorldFuture() {
        setInvoked();
        return CompletableFuture.completedFuture(greeting);
    }

    default Either<RuntimeException, String> returnEither() {
        setInvoked();
        return Either.right(greeting);
    }

    default Try<String> returnTry() {
        setInvoked();
        return Try.success(greeting);
    }

    default String returnHelloWorldWithException() throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        return  greeting;
    }

    default String returnHelloWorldWithName(String name) {
        setInvoked();
        if (shouldThrow()) throw new RuntimeException();
        return greeting+" "+name;
    }

    default String returnHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        return greeting +" "+name;
    }

    default String returnHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        if (shouldThrow()) throw new RuntimeException();
        return greeting + " " + title + " " +name;
    }

    default String returnHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        return greeting + " " + title + " " +name;
    }

    default void sayHelloWorld() {
        setInvoked();
        if (shouldThrow()) throw new RuntimeException();
        log.info(greeting);
    }

    default void sayHelloWorldWithException() throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        log.info(greeting);
    }

    default void sayHelloWorldWithName(String name) {
        setInvoked();
        if (shouldThrow()) throw new RuntimeException();
        log.info(greeting);
    }

    default void sayHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        log.info(greeting);
    }

    default void sayHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        if (shouldThrow()) throw new RuntimeException();
        log.info(greeting+" "+title+" "+name);
    }

    default void sayHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        if (shouldThrow()) throw new IOException();
        log.info(greeting +" "+title+" "+name);
    }
}