package io.github.venkateshamurthy.exceptional.pojo;

import io.github.resilience4j.core.functions.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface HelloWorldService {
    AtomicBoolean invoked = new AtomicBoolean(false);
    String greeting = "Hello World!";
    AtomicInteger throwExceptionCount = new AtomicInteger(0);
    Logger log = LoggerFactory.getLogger(HelloWorldService.class);
    default boolean shouldThrow() { return throwExceptionCount.get()>0; }
    default boolean isInvoked() { return invoked.get(); }
    default HelloWorldService setInvoked() { invoked.set(true); return this; }
    default HelloWorldService throwException() { throwExceptionCount.set(Integer.MAX_VALUE); return this; }
    default HelloWorldService throwException(int attempts) { throwExceptionCount.set(attempts); return this; }
    default HelloWorldService resetInvoked() { invoked.set(false); return this; }
    default HelloWorldService revokeException() { throwExceptionCount.set(0); return this; }

    default String returnHelloWorld() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("Should throw exception:"+ throwExceptionCount.get());
        return greeting;
    }

    default Future<String> returnHelloWorldFuture() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        return CompletableFuture.completedFuture(greeting)
                .handle((a,b)->{if(shouldThrow())
                {throw new RuntimeException();} else {return a;}
                });
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
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return  greeting;
    }

    default String returnHelloWorldWithName(String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        return greeting+" "+name;
    }

    default String returnHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return greeting +" "+name;
    }

    default String returnHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        return greeting + " " + title + " " +name;
    }

    default String returnHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return greeting + " " + title + " " +name;
    }

    default void sayHelloWorld() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.info(greeting);
    }

    default void sayHelloWorldWithException() throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.info(greeting);
    }

    default void sayHelloWorldWithName(String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.info(greeting);
    }

    default void sayHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.info(greeting);
    }

    default void sayHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.info(greeting+" "+title+" "+name);
    }

    default void sayHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.info(greeting +" "+title+" "+name);
    }
}