package com.wallet.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayName("ErrorResponse — тесты JSON-сериализации")
class ErrorResponseTest {

    @Autowired
    private JacksonTester<ErrorResponse> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Сериализация с заполненными полями")
    void serialize_withAllFields_shouldMatchExpectedJson() throws Exception {
        // Given
        ErrorResponse error = ErrorResponse.builder()
                .status(404)
                .error("WalletNotFound")
                .message("Wallet not found")
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .build();

        // When
        JsonContent<ErrorResponse> result = json.write(error);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.status").isEqualTo(404);
        assertThat(result).extractingJsonPathStringValue("$.error").isEqualTo("WalletNotFound");
        assertThat(result).extractingJsonPathStringValue("$.message").isEqualTo("Wallet not found");
        assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo("2024-01-01T12:00:00");
    }

    @Test
    @DisplayName("Сериализация с null полями — поля не должны попадать в JSON")
    void serialize_withNullFields_shouldOmitNullValues() throws Exception {
        // Given
        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .error("InternalError")
                // message, timestamp, path = null
                .build();

        // When
        JsonContent<ErrorResponse> result = json.write(error);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.status").isEqualTo(500);
        assertThat(result).extractingJsonPathStringValue("$.error").isEqualTo("InternalError");
        assertThat(result).doesNotHaveJsonPath("$.message");
        assertThat(result).doesNotHaveJsonPath("$.timestamp");
        assertThat(result).doesNotHaveJsonPath("$.path");
    }

    @Test
    @DisplayName("Десериализация JSON в объект")
    void deserialize_validJson_shouldCreateObject() throws Exception {
        // Given
        String jsonInput = """
                {
                  "status": 400,
                  "error": "ValidationError",
                  "message": "Invalid UUID",
                  "timestamp": "2024-05-20T14:30:00"
                }
                """;

        // When — используем objectMapper напрямую
        ErrorResponse result = objectMapper.readValue(jsonInput, ErrorResponse.class);

        // Then
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getError()).isEqualTo("ValidationError");
        assertThat(result.getMessage()).isEqualTo("Invalid UUID");
        assertThat(result.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 5, 20, 14, 30, 0));
    }

}