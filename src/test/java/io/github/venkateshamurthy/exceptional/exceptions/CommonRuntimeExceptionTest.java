package io.github.venkateshamurthy.exceptional.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.github.venkateshamurthy.exceptional.exceptions.CommonRuntimeException.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@DisplayName("CommonRuntimeException Tests")
class CommonRuntimeExceptionTest {

    private CommonRuntimeException exception;

    @BeforeEach
    void setUp() {
        exception = new CommonRuntimeException("Default message");
    }

    @Test
    @DisplayName("Should set message and timestamp with single-argument constructor")
    void testConstructorWithMessage() {
        CommonRuntimeException ex = new CommonRuntimeException("Something went wrong");

        assertThat(ex.getMessage()).isEqualTo("Something went wrong");
        assertThat(ex.getTimeStamp()).isNotNull();
    }

    @Test
    @DisplayName("Should set message, cause, and timestamp with two-argument constructor")
    void testConstructorWithMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Invalid argument");
        CommonRuntimeException ex = new CommonRuntimeException("Bad input", cause);

        assertThat(ex.getMessage()).isEqualTo("Bad input");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getTimeStamp()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should construct exception from ExceptionCode and template")
    void testConstructorWithExceptionCodeAndTemplate(ExceptionCode code) {
        CommonRuntimeException ex = code.toCommonRTE("Error in {}", "processing");
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());
    }

    @Test @DisplayName("Testing getStatus of ExceptionCode")
    void testGetStatus() {
        var ec = new ExceptionCode(){
            public String name() {return "SOME_ERROR";}
            public String getDescription() {return "";}
        };
        assertEquals(NO_CONTENT, ec.getStatus());
    }

    @Test
    @DisplayName("Should format detailed message using positional placeholders")
    void testDetailedMessageWithTemplate() {
        CommonRuntimeException ex = new CommonRuntimeException("Base message")
                .detailedMessage("User {} not found in {}.", "alice", "database");

        assertThat(ex.getDetailedMessage()).isEqualTo("User alice not found in database.");
    }

    @Test
    @DisplayName("Should format detailed message using named placeholders")
    void testDetailedMessageWithNamedMap() {
        Map<String, Object> values = Map.of("module", "Auth", "code", 403);
        CommonRuntimeException ex = new CommonRuntimeException("Base message")
                .setDetailedMessage("Error in {module}, status {code}", values);

        assertThat(ex.getDetailedMessage()).isEqualTo("Error in Auth, status 403");
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should format detailed message using named placeholders for a ExceptionCode")
    void testDetailedMessageWithNamedMapWithExceptionCode(ExceptionCode code) {
        Map<String, Object> values = Map.of("module", "Auth", "result", 1000);
        CommonRuntimeException ex = code.toCommonRTE("Error in {module}, result {result}", values);
        assertThat(ex.getDetailedMessage()).isEqualTo("Error in Auth, result 1000");
    }

    @Test
    @DisplayName("Should format detailed message using MessageFormat")
    void testFormatDetailedMessage() {
        MessageFormat format = new MessageFormat("Invalid {0} for {1}");
        CommonRuntimeException ex = new CommonRuntimeException()
                .formatDetailedMessage(format, "input", "service");

        assertThat(ex.getDetailedMessage()).isEqualTo("Invalid input for service");
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should format detailed message using MessageFormat")
    void testFormatDetailedMessageForAllEnums(ExceptionCode code) {
        var dateTime=NOW.get();
        MessageFormat format = new MessageFormat("Invalid {0} for {1}");
        CommonRuntimeException ex = code.toCommonRTE("Base message", format, "input", "service")
                .setTimeStamp(dateTime);
        assertThat(ex.getDetailedMessage()).isEqualTo("Invalid input for service");
        assertThat(ex)
                .extracting(CommonRuntimeException::getMessage,
                        CommonRuntimeException::getCode,
                        CommonRuntimeException::getDetailedMessage,
                        CommonRuntimeException::getHttpStatus,
                        CommonRuntimeException::getTimeStamp)
                .containsExactly("Base message", code.name(), "Invalid input for service", code.getStatus(), dateTime);

    }

    @Test
    @DisplayName("Should support fluent chaining of setters")
    void testFluentChaining() {
        ZonedDateTime now = ZonedDateTime.now();
        CommonRuntimeException ex = new CommonRuntimeException("Chain test")
                .setCode("CHAIN_ERR")
                .setDetailedMessage("Testing fluent chain")
                .setHttpStatus(HttpStatus.BAD_REQUEST)
                .setTimeStamp(now);

        assertThat(ex)
                .extracting(CommonRuntimeException::getCode,
                            CommonRuntimeException::getDetailedMessage,
                            CommonRuntimeException::getHttpStatus,
                            CommonRuntimeException::getTimeStamp)
                .containsExactly("CHAIN_ERR", "Testing fluent chain", HttpStatus.BAD_REQUEST, now);
    }

    @Test
    @DisplayName("Should log information without throwing errors")
    void testLogInfoDoesNotThrow() {
        assertDoesNotThrow(() -> exception.logInfo());
    }

    @Test
    @DisplayName("Should log debug information without throwing errors")
    void testLogDebugDoesNotThrow() {
        exception.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertDoesNotThrow(() -> exception.logDebug());
    }
}