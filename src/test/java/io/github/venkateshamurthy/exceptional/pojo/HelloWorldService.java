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

/**
 * An interface for testing pojo to conveniently mock or use default implementation.
 */
public interface HelloWorldService {
    /** invoked to indicate th ivocation.*/
    AtomicBoolean invoked = new AtomicBoolean(false);
    /** A greeting used for returning string.*/
    String greeting = "Hello World!";
    /** A counter used to count thrown exception during testing of test pojo.*/
    AtomicInteger throwExceptionCount = new AtomicInteger(0);
    /** Logger.*/
    Logger log = LoggerFactory.getLogger(HelloWorldService.class);
    /** Check throw based on exception count non-zero.
     * @return true if {@link #throwExceptionCount} >0
     */
    default boolean shouldThrow() { return throwExceptionCount.get()>0; }
    /** Check if invoked is set.
     * @return true if {@link #invoked} is set.
     */
    default boolean isInvoked() { return invoked.get(); }
    /** set the invoked (typically all interface methods.)*/
    default void setInvoked() { invoked.set(true);}
    /** set the throw exception.*/
    default void throwException() { throwExceptionCount.set(Integer.MAX_VALUE);}
    /**
     * Set the counter for throwing exception
     * @param attempts to count
     */
    default void throwException(int attempts) { throwExceptionCount.set(attempts);}
    /** reset the invoked flag typically done during test class's after methods.*/
    default void resetInvoked() { invoked.set(false);  }
    /** revoke/reset exception count typically done during test class's after methods.*/
    default void revokeException() { throwExceptionCount.set(0); }

    /**
     * This method tries to return string however for most times throws runtime exception due to {@link #throwExceptionCount}.
     * @return string (i.e {@link #greeting})
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorld() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("Should throw exception:"+ throwExceptionCount.get());
        return greeting;
    }

    /**
     * Return greeting wrapped in {@code Future}.This method can throw {@code RuntimeException} based on {@link #throwExceptionCount}
     * @return greeting
     */
    default Future<String> returnHelloWorldFuture() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        return CompletableFuture.completedFuture(greeting)
                .handle((a,b)->{if(shouldThrow()) {throw new RuntimeException();} else {return a;}});
    }

    /**
     * Return Either&lt;RuntimeException, String&gt;
     * @return Either greeting or an exception
     */
    default Either<RuntimeException, String> returnEither() {
        setInvoked();
        return Either.right(greeting);
    }

    /**
     * A return try method
     * @return Try wrapping the greeting
     */
    default Try<String> returnTry() {
        setInvoked();
        return Try.success(greeting);
    }

    /**
     * This method tries to return string however for most times throws checked exception  due to {@link #throwExceptionCount}.
     * @return string (i.e {@link #greeting})
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorldWithException() throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return  greeting;
    }

    /**
     * This method tries to return {@link #greeting} with a name however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @return string (i.e {@link #greeting} and a name)
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorldWithName(String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        return greeting+" "+name;
    }

    /**
     * This method tries to return {@link #greeting} augmented with name; however for most times throws IOException
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @return string (i.e {@link #greeting} and a name)
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return greeting +" "+name;
    }

    /**
     * This method tries to return {@link #greeting} with a name, title however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @param title  title of person
     * @return string (i.e {@link #greeting} and a name along with title)
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        return greeting + " " + title + " " +name;
    }

    /**
     * This method tries to return {@link #greeting} with a name, title however for most times throws checked exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @param title  title of person
     * @return string (i.e {@link #greeting} and a name along with title)
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default String returnHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        return greeting + " " + title + " " +name;
    }

    /**
     * This method tries to consume {@link #greeting} however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorld() {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.debug(greeting);
    }

    /**
     * This method tries to consume {@link #greeting} however for most times throws checked exception
     * due to {@link #throwExceptionCount}.
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorldWithException() throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.debug(greeting);
    }

    /**
     * This method tries to consume {@link #greeting} and name however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorldWithName(String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.debug(greeting);
    }

    /**
     * This method tries to consume {@link #greeting} and name.however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorldWithNameWithException(String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.debug(greeting);
    }

    /**
     * This method tries to consume {@link #greeting} and title, name however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @param title  title of person
     * @throws RuntimeException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorldWithTitleName(String title, String name) {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new RuntimeException("runtime exception:"+ throwExceptionCount.get());
        log.debug(greeting+" "+title+" "+name);
    }

    /**
     * This method tries to consume {@link #greeting} and name, title however for most times throws runtime exception
     * due to {@link #throwExceptionCount}.
     * @param name a simple name
     * @param title  title of person
     * @throws IOException whenever the {@code throwExceptionCount} > 0
     */
    default void sayHelloWorldWithTitleNameWithException(String title, String name) throws IOException {
        setInvoked();
        throwExceptionCount.decrementAndGet();
        if (shouldThrow()) throw new IOException("ioException:"+ throwExceptionCount.get());
        log.debug(greeting +" "+title+" "+name);
    }
}