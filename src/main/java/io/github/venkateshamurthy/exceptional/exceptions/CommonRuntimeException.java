package io.github.venkateshamurthy.exceptional.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.venkateshamurthy.exceptional.exceptions.DetailsMessageFormatters.*;
import static java.time.ZonedDateTime.now;

/**
 * {@code CommonRuntimeException} is a unified runtime exception class that can be used across the
 * project to encapsulate error information in a structured and consistent format.
 * <p>
 * This class extends {@link RuntimeException} and adds contextual fields such as:
 * <ul>
 *     <li>{@code code} - Application or domain-specific error code</li>
 *     <li>{@code detailedMessage} - Detailed, formatted explanation of the error</li>
 *     <li>{@code timeStamp} - UTC timestamp when the error was created</li>
 *     <li>{@code httpStatus} - The {@link HttpStatus} representing the error category</li>
 * </ul>
 * <p>
 * The class supports message formatting with templates, placeholder substitution, and structured logging
 * to simplify debugging and monitoring.
 *
 * <p>Example usage:
 * <pre>{@code
 * throw ExceptionCodes.CREDENTIAL_MISSING
 *     .toCommonRTE(CREDENTIAL_MISSING.getDescription(),
 *     "Missing or invalid credential ID: {}", request.getCredentialId())
 *     .logInfo();
 * }</pre>
 *
 * <p>Another example:
 * <pre>{@code
 * throw ExceptionCodes.CREDENTIAL_MISSING.toCommonRTE(
 *      new OverlappingFileLockException(), "Missing or bad credentials",
 *     "Missing or invalid credential ID: {credentialId}", Map.of("credentialId", request.getCredentialId()))
 *     .setHttpStatus(HttpStatus.BAD_REQUEST)
 *     .logInfo();
 * }</pre>
 *
 * <pre>{@code
 * throw ExceptionCodes.CREDENTIAL_MISSING.toCommonRTE(
 *      ExceptionCode.NULL_CAUSE, "Missing or bad credentials",
 *      "Missing or invalid credential ID: {credentialId}", request.getCredentialId())
 *      .setHttpStatus(HttpStatus.BAD_REQUEST)
 *      .logInfo();
 *  *}</pre>
 *
 * @author venkateshamurthy
 * @since 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder(toBuilder = true)
@Slf4j
@JsonTypeName
@JsonIgnoreProperties(value={"cause","stackTrace","localizedMessage","suppressed"},ignoreUnknown = true)
public class CommonRuntimeException extends RuntimeException implements Serializable {
    /** A supplier of current instant.*/
    protected static final Supplier<ZonedDateTime> NOW = () -> now(ZoneId.of("UTC"));

    /** Error code that is short string to indicate error say such as FILE_LOCKED.*/
    @Schema(description = "Error code")   @JsonProperty  private String code;
    /** A detailed message/text explaining the contextual error with data/details.*/
    @Schema(description = "Error details")@JsonProperty  private String detailedMessage;
    /** Time of occurrence of the error.*/
    @Schema(description = "Error time")   @JsonProperty  private ZonedDateTime timeStamp;
    /** A short description (one-liner perhaps) of this error which can be used for {@link Exception#getMessage()}.*/
    @Schema(description = "Http Status")  @JsonProperty  private HttpStatus httpStatus;

    /**
     * Constructs a new {@code CommonRuntimeException} with the specified message.
     * Automatically sets the timestamp to current UTC time.
     *
     * @param message the exception message
     */
    public CommonRuntimeException(final String message) {
        this(message, null);
    }

    /**
     * Constructs a new {@code CommonRuntimeException} with the specified message and cause.
     * Automatically sets the timestamp to current UTC time.
     *
     * @param message the exception message
     * @param cause   the underlying cause of this exception
     */
    public CommonRuntimeException(final String message, Throwable cause) {
        super(message, cause);
        setTimeStamp(NOW.get());
    }

    /**
     * Constructs a new {@code CommonRuntimeException} with the specified message and cause.
     * Automatically sets the timestamp to current UTC time.
     *
     * @param message the exception message
     * @param cause   the underlying cause of this exception
     * @param template template of details such as - SLF4J: "Error in {}" or NAMEDARGS: "Error in {phase}" or
     *                Standard JAVA:"Error in {0}"
     * @param args var-args for details
     */
    public CommonRuntimeException(final String message, Throwable cause, String template, Object... args) {
        this(message, cause);
        detailedMessage = detectAndFormat(template, args);
    }

    /**
     * Logs this exception’s summary at INFO level, including error code, timestamp, and details.
     *
     * @return this instance for fluent chaining
     */
    public CommonRuntimeException logInfo() {
        log.info("Error:{} | Time:{} | Details:{}", code, timeStamp, detailedMessage);
        return this;
    }

    /**
     * Logs this exception at DEBUG level, including message, code, timestamp, details, and HTTP status.
     *
     * @return this instance for fluent chaining
     */
    public CommonRuntimeException logDebug() {
        log.debug("Message:{} | Error:{} | Time:{} | Details:{} | Status:{}",
                getMessage(), code, timeStamp, detailedMessage, httpStatus);
        return this;
    }

    //-----------------------------Deprecated Methods to be removed---------------------
    /**
     * @deprecated and rendered effect-less in favour of using the
     * {@link ExceptionCode#toCommonRTE(Throwable, String, String, Object...)} or
     * with setDetailedMessage(String) method where the argument can be built by
     * {@link DetailsMessageFormatters#detectAndFormat(String, Object...)}.
     *
     * Sets the Message Formatting style of this instance.
     *
     * @param style an one of {@link DetailsMessageFormatters} of the message format
     * @return this instance
     */
    @Deprecated(since="1.5", forRemoval = true)
    public CommonRuntimeException setFormatterStyle(DetailsMessageFormatters style){
        return this;//Note that this is a no-op call.
    }

    /**
     * @deprecated in favour of using the
     * {@link ExceptionCode#toCommonRTE(Throwable, String, String, Object...)} or
     * with setDetailedMessage(String) method where the argument can be built by
     * {@link DetailsMessageFormatters#detectAndFormat(String, Object...)}.
     *
     * Sets the detailed message by substituting values in the given template. This instance
     * utilizes {@link DetailsMessageFormatters#SLF4J} is using as default which may be on the contrary
     * to the desired formatter.
     *
     * @param template the message template
     * @param values   values to replace in template
     * @return this instance with the details message updated for fluent chaining
     */
    @Deprecated(since="1.5", forRemoval = true)
    public CommonRuntimeException detailedMessage(String template, Object... values) {
        return setDetailedMessage(SLF4J.format(template, values));
    }

    /**
     * @deprecated in favour of using the
     * {@link ExceptionCode#toCommonRTE(Throwable, String, String, Object...)} or
     * with setDetailedMessage(String) method where the argument can be built by
     * {@link DetailsMessageFormatters#detectAndFormat(String, Object...)}.
     *
     * Sets the detailed message by replacing named placeholders in the form of {@code {key}}.
     * <p>
     * Example:
     * <pre>{@code
     * exception.setDetailedMessage("Error in {module} at {time}", Map.of("module", "Auth", "time", "12:00 UTC"));
     * }</pre>
     *
     * @param template the message template with named placeholders
     * @param values   the key-value pairs for substitution
     * @return this instance for fluent chaining
     */
    @Deprecated(since="1.5", forRemoval = true)
    public CommonRuntimeException setDetailedMessage(String template, Map<String, Object> values) {
        return setDetailedMessage(detectAndFormat(template, values));
    }

    /**
     * @deprecated in favour of using the
     * {@link ExceptionCode#toCommonRTE(Throwable, String, String, Object...)} or
     * with setDetailedMessage(String) method where the argument can be built by
     * {@link DetailsMessageFormatters#detectAndFormat(String, Object...)}.
     *
     * Sets the detailed message using a {@link MessageFormat} instance for advanced message formatting.
     *
     * @param template the {@link MessageFormat} instance
     * @param values   the argument values
     * @return this instance for fluent chaining
     */
    @Deprecated(since="1.5", forRemoval = true)
    public CommonRuntimeException formatDetailedMessage(MessageFormat template, Object... values) {
        return setDetailedMessage(detectAndFormat(template.toPattern(), values));
    }
}