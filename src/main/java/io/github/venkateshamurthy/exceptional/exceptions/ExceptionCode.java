package io.github.venkateshamurthy.exceptional.exceptions;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.Map;

import static io.github.venkateshamurthy.exceptional.exceptions.DetailsMessageFormatters.*;

/**
 * An interface to model exception code, short description and enable creating
 * {@link CommonRuntimeException}
 */
public interface ExceptionCode {

    /** Null Throwable used while building exception not requiring the cause.*/
    Throwable NULL_CAUSE = null;

    /**
     * Short name of the error/exception code.
     * @return name/code of the error.
     */
    String name();

    /**
     * Short description of the error.
     * @return description.
     */
    String getDescription();

    /**
     * Get http status.
     * @return {@link HttpStatus}
     */
    default HttpStatus getStatus() {
        return HttpStatus.NO_CONTENT;
    }

    /**
     * A {@link CommonRuntimeException} generator given {@link #getDescription()} serving as a message. This exception
     * should be further updated with detailed message to have all the contextual details.
     *
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE() {
        return toCommonRTE(getDescription());
    }

    /**
     * A {@link CommonRuntimeException} generator given a message. This exception should be further updated with
     * detailed message to have all the contextual details.
     *
     * @param message for Exception
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String message) {
        return toCommonRTE(NULL_CAUSE, message);
    }

    /**
     * A {@link CommonRuntimeException} generator given a cause and message. This exception should be further updated
     * with detailed message to have all the contextual details.
     *
     * @param cause   the original cause of the error to be set within this exception.
     * @param message for Exception
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(Throwable cause, String message) {
        return new CommonRuntimeException(message, cause).setCode(name()).setHttpStatus(getStatus());
    }

    /**
     * A {@link CommonRuntimeException} creator given message, details template and args
     *
     * @param message   the exception message
     * @param template  the details message template
     * @param args      the arguments (either could be just one Map of key-value or var-args)
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String message, String template, Object... args) {
        return toCommonRTE(NULL_CAUSE, message, template, args);
    }

    /**
     * A {@link CommonRuntimeException} creator given cause, message, details template and args
     *
     * @param cause     the original cause of the error to be set within this exception
     * @param message   the exception message
     * @param template  the details message template
     * @param args      the arguments (either could be just one Map of key-value or var-args)
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(Throwable cause, String message,
                                               String template, Object... args) {
        return new CommonRuntimeException(message, cause, template, args).setCode(name()).setHttpStatus(getStatus());
    }

    //-----------------------------Deprecated Methods to be removed---------------------
    /**
     * A {@link CommonRuntimeException} generator setting the message of {@link #getDescription()}
     *
     * @param template a SLF4J style formatted template (for eg: "Server error:{}") where placeholders to be substituted.
     * @param args     the parameters are stringifiable var-args to be used to replace each placeholder template
     * @return {@link CommonRuntimeException}
     * @deprecated in favour of {@link #toCommonRTE(Throwable, String, String, Object...)}. Please do not use this
     */
    @Deprecated(since = "1.5", forRemoval = true)
    default CommonRuntimeException toCommonRTE(String template, Object... args) {
        return toCommonRTE(NULL_CAUSE, getDescription(), template, args);
    }

    /**
     * A {@link CommonRuntimeException} with message using {@link #getDescription()} along with detailed map of parameters.
     *
     * @param template for a detailed message that needs variable interpolated based on substitution markers
     * @param args     the parameters to be used to replace the markered template
     * @return {@link CommonRuntimeException}
     * @deprecated in favour of{@link #toCommonRTE(Throwable, String, String, Object...)}. Please do not use this
     */
    @Deprecated(since = "1.5", forRemoval = true)
    default CommonRuntimeException toCommonRTE(String template, Map<String, Object> args) {
        return toCommonRTE(NULL_CAUSE, getDescription(), template, args);
    }

    /**
     * A {@link CommonRuntimeException} generator given
     *
     * @param template a {@link MessageFormat formatted detailed message} that would be processed by {@code MessageFormat}
     * @param args     the parameters to be used to replace the markers supported by {@link MessageFormat}
     * @return {@link CommonRuntimeException}
     * @deprecated in favour of {@link #toCommonRTE(Throwable, String, String, Object...)}.Please do not use this
     */
    @Deprecated(since = "1.5", forRemoval = true)
    default CommonRuntimeException toCommonRTE(MessageFormat template, Object... args) {
        return toCommonRTE(NULL_CAUSE, getDescription(), template.toPattern(), args);
    }
}