package io.github.venkateshamurthy.exceptional.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor @Getter @ToString(exclude = {"status"})
enum ExceptionCodes implements ExceptionCode {
    CREDENTIAL_MISSING("Credential is missing", HttpStatus.UNPROCESSABLE_ENTITY),
    CREDENTIALS_NOT_FOUND("Credential could not be accessed from store", HttpStatus.INTERNAL_SERVER_ERROR),
    CREDENTIAL_NOT_CREATED("Unable to create credential", HttpStatus.BAD_REQUEST),

    //validation errors
    VALIDATION_FAILED("Input request validation failed", HttpStatus.BAD_REQUEST),
    EVENT_VALIDATION_FAILED("An invalid event", HttpStatus.BAD_REQUEST);
    private final String description;
    private final HttpStatus status;
}