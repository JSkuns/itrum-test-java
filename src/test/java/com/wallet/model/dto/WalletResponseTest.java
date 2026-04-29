package com.wallet.model.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.model.entity.Wallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WalletResponse — тесты")
class WalletResponseTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Метод fromEntity()")
    class FromEntityTests {

        @Test
        @DisplayName("Конвертация валидной сущности — корректный DTO")
        void fromEntity_withValidWallet_shouldCreateDto() {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal balance = BigDecimal.valueOf(1500.50);
            Wallet wallet = Wallet.builder()
                    .id(walletId)
                    .balance(balance)
                    .version(1L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            WalletResponse response = WalletResponse.fromEntity(wallet);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getWalletId()).isEqualTo(walletId);
            assertThat(response.getBalance()).isEqualTo(balance);
        }

        @Test
        @DisplayName("Конвертация null-сущности — возвращает null")
        void fromEntity_withNullWallet_shouldReturnNull() {
            // When
            WalletResponse response = WalletResponse.fromEntity(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Конвертация сущности с нулевым балансом")
        void fromEntity_withZeroBalance_shouldCreateDto() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.ZERO)
                    .build();

            // When
            WalletResponse response = WalletResponse.fromEntity(wallet);

            // Then
            assertThat(response.getBalance()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Конвертация сущности с очень большим балансом")
        void fromEntity_withLargeBalance_shouldPreservePrecision() {
            // Given
            BigDecimal largeBalance = new BigDecimal("999999999.99");
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(largeBalance)
                    .build();

            // When
            WalletResponse response = WalletResponse.fromEntity(wallet);

            // Then
            assertThat(response.getBalance()).isEqualTo(largeBalance);
            assertThat(response.getBalance().scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("JSON-сериализация")
    class JsonSerializationTests {

        @Test
        @DisplayName("Сериализация — корректный JSON формат")
        void serialize_shouldProduceValidJson() throws JsonProcessingException {
            // Given
            UUID walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            WalletResponse response = WalletResponse.builder()
                    .walletId(walletId)
                    .balance(new BigDecimal("1234.56"))
                    .build();

            // When
            String json = objectMapper.writeValueAsString(response);

            // Then
            assertThat(json).contains("\"walletId\":\"550e8400-e29b-41d4-a716-446655440000\"");
            assertThat(json).contains("\"balance\":1234.56");  // BigDecimal сериализуется как число
            assertThat(json).doesNotContain("null");  // @JsonInclude исключает null-поля
        }

        @Test
        @DisplayName("Сериализация с нулевым балансом")
        void serialize_withZeroBalance_shouldIncludeZero() throws JsonProcessingException {
            // Given
            WalletResponse response = WalletResponse.builder()
                    .walletId(UUID.randomUUID())
                    .balance(BigDecimal.ZERO)
                    .build();

            // When
            String json = objectMapper.writeValueAsString(response);

            // Then
            assertThat(json).contains("\"balance\":0");  // или "0.0" в зависимости от настроек
        }

        @Test
        @DisplayName("Десериализация — создание объекта из JSON")
        void deserialize_shouldCreateValidObject() throws JsonProcessingException {
            // Given
            String json = """
                    {
                      "walletId": "550e8400-e29b-41d4-a716-446655440000",
                      "balance": 999.99
                    }
                    """;

            // When
            WalletResponse result = objectMapper.readValue(json, WalletResponse.class);

            // Then
            assertThat(result.getWalletId())
                    .isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            assertThat(result.getBalance()).isEqualTo(new BigDecimal("999.99"));
        }

        @Test
        @DisplayName("Десериализация с невалидным UUID — исключение")
        void deserialize_invalidUuid_shouldThrowException() {
            // Given
            String invalidJson = """
                    {
                      "walletId": "not-a-uuid",
                      "balance": 100
                    }
                    """;

            // When & Then
            assertThatThrownBy(() -> objectMapper.readValue(invalidJson, WalletResponse.class))
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
            BigDecimal balance = BigDecimal.valueOf(500);

            // When
            WalletResponse response = WalletResponse.builder()
                    .walletId(walletId)
                    .balance(balance)
                    .build();

            // Then
            assertThat(response.getWalletId()).isEqualTo(walletId);
            assertThat(response.getBalance()).isEqualTo(balance);
        }

        @Test
        @DisplayName("Builder позволяет создать частично заполненный объект")
        void builder_partialBuild_shouldWork() {
            // When
            WalletResponse response = WalletResponse.builder()
                    .walletId(UUID.randomUUID())
                    // balance не установлен
                    .build();

            // Then
            assertThat(response.getWalletId()).isNotNull();
            assertThat(response.getBalance()).isNull();  // Допустимо для внутреннего использования
        }

        @Test
        @DisplayName("Lombok-аннотации генерируют equals/hashCode")
        void equals_and_hashCode_shouldWorkCorrectly() {
            // Given
            UUID id = UUID.randomUUID();
            BigDecimal balance = BigDecimal.TEN;
            WalletResponse r1 = WalletResponse.builder().walletId(id).balance(balance).build();
            WalletResponse r2 = WalletResponse.builder().walletId(id).balance(balance).build();
            WalletResponse r3 = WalletResponse.builder().walletId(id).balance(BigDecimal.ONE).build();

            // Then
            assertThat(r1).isEqualTo(r2);
            assertThat(r1).isNotEqualTo(r3);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }
    }

    @Nested
    @DisplayName("Интеграция с бизнес-логикой")
    class BusinessLogicTests {

        @Test
        @DisplayName("fromEntity после операции DEPOSIT")
        void fromEntity_afterDeposit_shouldReflectUpdatedBalance() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.valueOf(1000))
                    .build();
            wallet.deposit(BigDecimal.valueOf(500));  // Бизнес-метод сущности

            // When
            WalletResponse response = WalletResponse.fromEntity(wallet);

            // Then
            assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
        }

        @Test
        @DisplayName("fromEntity после операции WITHDRAW")
        void fromEntity_afterWithdraw_shouldReflectUpdatedBalance() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.valueOf(1000))
                    .build();
            wallet.withdraw(BigDecimal.valueOf(300));  // Бизнес-метод сущности

            // When
            WalletResponse response = WalletResponse.fromEntity(wallet);

            // Then
            assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(700));
        }
    }

}