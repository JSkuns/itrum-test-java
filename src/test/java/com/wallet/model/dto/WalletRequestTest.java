package com.wallet.model.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.model.enums.OperationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WalletRequest — тесты валидации и сериализации")
class WalletRequestTest {

    private static Validator validator;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Валидация полей")
    class ValidationTests {

        @Test
        @DisplayName("Валидный запрос — не имеет ошибок валидации")
        void validRequest_shouldHaveNoViolations() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.DEPOSIT)
                    .amount(BigDecimal.valueOf(100.50))
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null walletId — ошибка валидации")
        void nullWalletId_shouldFailValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(null)
                    .operationType(OperationType.DEPOSIT)
                    .amount(BigDecimal.TEN)
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Wallet ID cannot be null");
        }

        @Test
        @DisplayName("Null operationType — ошибка валидации")
        void nullOperationType_shouldFailValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(null)
                    .amount(BigDecimal.TEN)
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Operation type cannot be null");
        }

        @Test
        @DisplayName("Null amount — ошибка валидации")
        void nullAmount_shouldFailValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.WITHDRAW)
                    .amount(null)
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Amount cannot be null");
        }

        @Test
        @DisplayName("Отрицательная сумма — ошибка валидации @Positive")
        void negativeAmount_shouldFailValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.DEPOSIT)
                    .amount(BigDecimal.valueOf(-50))
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Amount must be positive");
        }

        @Test
        @DisplayName("Нулевая сумма — ошибка валидации @Positive")
        void zeroAmount_shouldFailValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.DEPOSIT)
                    .amount(BigDecimal.ZERO)
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Amount must be positive");
        }

        @Test
        @DisplayName("Очень маленькая положительная сумма — проходит валидацию")
        void smallPositiveAmount_shouldPassValidation() {
            // Given
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.DEPOSIT)
                    .amount(new BigDecimal("0.01"))
                    .build();

            // When
            Set<ConstraintViolation<WalletRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("JSON-сериализация и десериализация")
    class JsonTests {

        @Test
        @DisplayName("Сериализация в JSON — корректный формат")
        void serialize_toJson_shouldMatchExpectedFormat() throws JsonProcessingException {
            // Given
            UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            WalletRequest request = WalletRequest.builder()
                    .walletId(walletId)
                    .operationType(OperationType.WITHDRAW)
                    .amount(new BigDecimal("1234.56"))
                    .build();

            // When
            String json = objectMapper.writeValueAsString(request);

            // Then
            assertThat(json).contains("\"walletId\":\"550e8400-e29b-41d4-a716-446655440000\"");
            assertThat(json).contains("\"operationType\":\"WITHDRAW\"");
            assertThat(json).contains("\"amount\":\"1234.56\""); // String format due to @JsonFormat
        }

        @Test
        @DisplayName("Десериализация из JSON — корректное создание объекта")
        void deserialize_fromJson_shouldCreateValidObject() throws JsonProcessingException {
            // Given
            String json = """
                    {
                      "walletId": "550e8400-e29b-41d4-a716-446655440000",
                      "operationType": "DEPOSIT",
                      "amount": "999.99"
                    }
                    """;

            // When
            WalletRequest result = objectMapper.readValue(json, WalletRequest.class);

            // Then
            assertThat(result.getWalletId())
                    .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            assertThat(result.getOperationType()).isEqualTo(OperationType.DEPOSIT);
            assertThat(result.getAmount()).isEqualTo(new BigDecimal("999.99"));
        }

        @Test
        @DisplayName("Десериализация с невалидным UUID — исключение")
        void deserialize_invalidUuid_shouldThrowException() {
            // Given
            String invalidJson = """
                    {
                      "walletId": "not-a-uuid",
                      "operationType": "DEPOSIT",
                      "amount": "100"
                    }
                    """;

            // When & Then
            assertThatThrownBy(() -> objectMapper.readValue(invalidJson, WalletRequest.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasMessageContaining("UUID");
        }
    }

    @Nested
    @DisplayName("Builder-паттерн")
    class BuilderTests {

        @Test
        @DisplayName("Builder создаёт объект с заданными полями")
        void builder_shouldCreateObjectWithSpecifiedFields() {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            // When
            WalletRequest request = WalletRequest.builder()
                    .walletId(walletId)
                    .operationType(OperationType.WITHDRAW)
                    .amount(amount)
                    .build();

            // Then
            assertThat(request.getWalletId()).isEqualTo(walletId);
            assertThat(request.getOperationType()).isEqualTo(OperationType.WITHDRAW);
            assertThat(request.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Builder позволяет создать объект с минимумом полей")
        void builder_partialBuild_shouldWork() {
            // When
            WalletRequest request = WalletRequest.builder()
                    .walletId(UUID.randomUUID())
                    .operationType(OperationType.DEPOSIT)
                    .amount(BigDecimal.TEN)
                    .build();

            // Then
            assertThat(request).isNotNull();
            assertThat(request.getWalletId()).isNotNull();
            assertThat(request.getOperationType()).isNotNull();
            assertThat(request.getAmount()).isNotNull();
        }
    }

}