package com.wallet.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("UuidValidator — тесты")
class UuidValidatorTest {

    private UuidValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new UuidValidator();
        // Подавляем возможные вызовы к контексту (не используется в реализации)
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(null);
    }

    /**
     * Тестовый DTO для проверки валидации через Bean Validation API.
     */
    static class TestDto {
        @ValidUUID
        private final UUID walletId;

        TestDto(UUID walletId) {
            this.walletId = walletId;
        }

        public UUID getWalletId() {
            return walletId;
        }
    }

    @Nested
    @DisplayName("isValid() — проверка UUID")
    class IsValidTests {

        @Test
        @DisplayName("Валидный UUID — возвращает true")
        void isValid_withValidUuid_shouldReturnTrue() {
            // Given
            UUID validUuid = UUID.randomUUID();

            // When
            boolean result = validator.isValid(validUuid, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Конкретный валидный UUID — возвращает true")
        void isValid_withSpecificValidUuid_shouldReturnTrue() {
            // Given
            UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            // When
            boolean result = validator.isValid(uuid, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Null значение — возвращает false")
        void isValid_withNull_shouldReturnFalse() {
            // When
            boolean result = validator.isValid(null, context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("UUID с нулевыми битами — возвращает true (это валидный UUID)")
        void isValid_withZeroUuid_shouldReturnTrue() {
            // Given
            UUID zeroUuid = new UUID(0L, 0L);

            // When
            boolean result = validator.isValid(zeroUuid, context);

            // Then
            assertThat(result).isTrue();
        }
    }

    // === Вспомогательный класс для интеграционных тестов ===

    @Nested
    @DisplayName("Интеграция с Bean Validation")
    class IntegrationTests {

        @Test
        @DisplayName("Валидация через ValidatorFactory — невалидный null")
        void validate_withNull_shouldFail() {
            // Given
            try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
                var validator = factory.getValidator();
                var testObject = new TestDto(null);

                // When
                var violations = validator.validate(testObject);

                // Then
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getPropertyPath().toString())
                        .isEqualTo("walletId");
            }
        }

        @Test
        @DisplayName("Валидация через ValidatorFactory — валидный UUID")
        void validate_withValidUuid_shouldPass() {
            // Given
            try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
                var validator = factory.getValidator();
                var testObject = new TestDto(UUID.randomUUID());

                // When
                var violations = validator.validate(testObject);

                // Then
                assertThat(violations).isEmpty();
            }
        }
    }

}