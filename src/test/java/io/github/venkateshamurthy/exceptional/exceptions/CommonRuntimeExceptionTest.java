package io.github.venkateshamurthy.exceptional.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import java.nio.channels.OverlappingFileLockException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.github.venkateshamurthy.exceptional.exceptions.CommonRuntimeException.NOW;
import static io.github.venkateshamurthy.exceptional.exceptions.DetailsMessageFormatters.*;
import static io.github.venkateshamurthy.exceptional.exceptions.DetailsMessageFormatters.detectAndFormat;
import static io.github.venkateshamurthy.exceptional.exceptions.ExceptionCode.NULL_CAUSE;
import static io.github.venkateshamurthy.exceptional.exceptions.ExceptionCodes.CREDENTIAL_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@DisplayName("CommonRuntimeException Tests")
class CommonRuntimeExceptionTest {
    private final ObjectMapper objectMapper= JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
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

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Test serialization/deserialization")
    void testSerialization(ExceptionCode code) throws JsonProcessingException {
        var fixedTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneId.of("UTC"));
        var cte = code.toCommonRTE(new OverlappingFileLockException(), "Some error")
                .setTimeStamp(fixedTime)
                .setDetailedMessage(SLF4J.format("This error is {} needs to be addressed within {}", "URGENT", fixedTime) )
                .logDebug();
        String serialized = objectMapper.writeValueAsString(cte);
        CommonRuntimeException ex = objectMapper.readValue(serialized, CommonRuntimeException.class);
        assertEquals(cte.getCode(), ex.getCode());
        assertThat(cte.getTimeStamp()).isEqualTo(ex.getTimeStamp());
        assertEquals(cte.getMessage(), ex.getMessage());
        assertEquals(cte.getDetailedMessage(), ex.getDetailedMessage());
        assertEquals(cte.getHttpStatus(), ex.getHttpStatus());
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
        CommonRuntimeException ex = code.toCommonRTE(code.getDescription(),"Error in {}", "processing");
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());

        ex = code.toCommonRTE(code.getDescription(),"Error in {}", "processing");
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());

        ex = code.toCommonRTE(code.getDescription(),"{} in {}", "Error", "processing");
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());

        ex = code.toCommonRTE().setDetailedMessage(detectAndFormat("{} in {}", "Error", "processing"));
        assertThat(ex.getDetailedMessage()).isEqualTo("Error in processing");
        ex.setDetailedMessage(detectAndFormat("{type} in {state}", Map.of("type","Error","state","processing")));
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());

        ex.formatDetailedMessage(new MessageFormat("{0} in {1}"), "Error", "processing");
        assertEquals("Error in processing", ex.getDetailedMessage());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());

        ex = code.toCommonRTE(code.getDescription(), "Error in {}", "processing");
        assertEquals("Error in processing", ex.getDetailedMessage());
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());

        Map map = Map.of("key","processing");
        Object obj = map;
        ex = code.toCommonRTE().setDetailedMessage(detectAndFormat("Error in {key}", obj));
        assertThat(ex.getCode()).isEqualTo(code.name());
        assertThat(ex.getDetailedMessage()).contains("processing");
        assertThat(ex.getMessage()).isEqualTo(code.getDescription());
        assertThat(ex.getHttpStatus()).isEqualTo(code.getStatus());

        ex = code.toCommonRTE(NULL_CAUSE, code.getDescription(), "Error in {key}", map);
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
                .setDetailedMessage(SLF4J.format("User {} not found in {}.", "alice", "database"));

        assertThat(ex.getDetailedMessage()).isEqualTo("User alice not found in database.");
    }

    @Test
    @DisplayName("Should format detailed message using named placeholders")
    void testDetailedMessageWithNamedMap() {
        Map<String, Object> values = Map.of("module", "Auth", "code", 403);
        CommonRuntimeException ex = new CommonRuntimeException("Base message")
                .setDetailedMessage(NAMEDARGS.format("Error in {module}, status {code}", values));

        assertThat(ex.getDetailedMessage()).isEqualTo("Error in Auth, status 403");
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should format detailed message using named placeholders for a ExceptionCode")
    void testDetailedMessageWithNamedMapWithExceptionCode(ExceptionCode code) {
        Map<String, Object> values = Map.of("module", "Auth", "result", 1000);
        CommonRuntimeException ex = code.toCommonRTE(NULL_CAUSE, code.getDescription(),"Error in {module}, code: {result}", values);
        assertThat(ex.getDetailedMessage()).isEqualTo("Error in Auth, code: 1000");
        CommonRuntimeException ex2 = code.toCommonRTE(NULL_CAUSE,  code.getDescription(),"Error in {module}, code: {result}", "Auth", 1000);
        assertThat(ex2.getDetailedMessage()).isEqualTo("Error in Auth, code: 1000");
        assertEquals("Error in {progress=progress}", code.toCommonRTE(NULL_CAUSE, code.getDescription(),"Error in {}",
                Map.of("progress", "progress"), "junk").getDetailedMessage());
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should format detailed message using MessageFormat")
    void testFormatDetailedMessage(ExceptionCode code) {
        MessageFormat format = new MessageFormat("Invalid {0} for {1}");
        CommonRuntimeException ex = code.toCommonRTE(format, "input", "service");
        assertThat(ex.getDetailedMessage()).isEqualTo("Invalid input for service");
    }

    @ParameterizedTest
    @EnumSource(ExceptionCodes.class)
    @DisplayName("Should format detailed message using MessageFormat")
    void testFormatDetailedMessageForAllEnums(ExceptionCode code) {
        var dateTime=NOW.get();
        MessageFormat format = new MessageFormat("Invalid {0} for {1}");
        CommonRuntimeException ex = code.toCommonRTE(NULL_CAUSE, code.getDescription(), format.toPattern(), "input", "service")
                .setTimeStamp(dateTime);
        assertThat(ex.getDetailedMessage()).isEqualTo("Invalid input for service");
        assertThat(ex)
                .extracting(CommonRuntimeException::getMessage,
                        CommonRuntimeException::getCode,
                        CommonRuntimeException::getDetailedMessage,
                        CommonRuntimeException::getHttpStatus,
                        CommonRuntimeException::getTimeStamp)
                .containsExactly(code.getDescription(), code.name(), "Invalid input for service", code.getStatus(), dateTime);

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

    @Test
    @DisplayName("Test deprecated methods as well untill removed")
    void testDeprecatedMethods() {
        var ex = CREDENTIAL_MISSING.toCommonRTE("Information in {}",(Object)"pre-processing");
        assertNull(ex.getCause());
        assertEquals("Information in pre-processing", ex.getDetailedMessage());
        ex = CREDENTIAL_MISSING.toCommonRTE("{fault} in {phase}", Map.of("fault","Information", "phase","pre-processing"));
        assertNull(ex.getCause());
        assertEquals("Information in pre-processing", ex.getDetailedMessage());

        assertSame(ex, ex.setFormatterStyle(SLF4J));
        assertEquals("Error in processing",
                ex.detailedMessage("{} in {}", "Error", "processing").getDetailedMessage());
        assertEquals("{0} in {1}",
                ex.detailedMessage("{0} in {1}", "Error", "processing").getDetailedMessage());
        assertEquals("Error in processing",
                ex.setDetailedMessage("{fault} in {phase}", Map.of("fault","Error",
                        "phase", "processing")).getDetailedMessage());
    }
}