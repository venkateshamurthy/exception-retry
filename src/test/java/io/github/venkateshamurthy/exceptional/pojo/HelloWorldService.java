package io.github.venkateshamurthy.exceptional.pojo;

import io.github.resilience4j.core.functions.Either;
import io.vavr.control.Try;

import java.io.IOException;
import java.util.concurrent.Future;

public interface HelloWorldService {
    String returnHelloWorld() ;
    Future<String> returnHelloWorldFuture() ;
    Either<RuntimeException, String> returnEither();
    Try<String> returnTry() ;
    String returnHelloWorldWithException() throws IOException;
    String returnHelloWorldWithName(String name);
    String returnHelloWorldWithNameWithException(String name) throws IOException;
    void sayHelloWorld();
    void sayHelloWorldWithException() throws IOException;
    void sayHelloWorldWithName(String name);
    void sayHelloWorldWithNameWithException(String name) throws IOException;
}