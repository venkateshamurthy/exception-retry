package io.github.venkateshamurthy.exceptional.exceptions;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.Map;

/**
 * An interface to model exception code, short description and enable creating
 * {@link CommonRuntimeException}
 */
public interface ExceptionCode {
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
     * A {@link CommonRuntimeException} generator given
     *
     * @param message for Exception
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String message) {
        return new CommonRuntimeException(message).setCode(name()).setHttpStatus(getStatus());
    }

    /**
     * A {@link CommonRuntimeException} generator setting the message of {@link #getDescription()}
     *
     * @param template for a detailed message that needs variable interpolated based on substitution markers
     * @param args     the parameters to be used to replace the markered template
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String template, Object... args) {
        return toCommonRTE(getDescription()).detailedMessage(template, args);
    }

    /**
     * A {@link CommonRuntimeException} with message set with {@link #getDescription()} with following other parameters.
     *
     * @param template for a detailed message that needs variable interpolated based on substitution markers
     * @param args     the parameters to be used to replace the markered template
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String template, Map<String, Object> args) {
        return toCommonRTE(getDescription()).setDetailedMessage(template, args);
    }

    /**
     * A {@link CommonRuntimeException} generator given
     *
     * @param message  for Exception
     * @param template a {@link MessageFormat formatted detailed message} that would be processed by  {@code MessageFormat}
     * @param args     the parameters to be used to replace the markers supported by {@link MessageFormat}
     * @return {@link CommonRuntimeException}
     */
    default CommonRuntimeException toCommonRTE(String message, MessageFormat template, Object... args) {
        return toCommonRTE(message).formatDetailedMessage(template, args);
    }
}