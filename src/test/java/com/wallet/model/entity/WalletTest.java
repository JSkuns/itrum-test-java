package com.wallet.model.entity;

import com.wallet.exception.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wallet — тесты бизнес-логики")
class WalletTest {

    private Wallet createWalletWithBalance(BigDecimal balance) {
        return Wallet.builder()
                .id(UUID.randomUUID())
                .balance(balance)
                .version(0L)
                .build();
    }

    @Nested
    @DisplayName("deposit() — пополнение баланса")
    class DepositTests {

        @Test
        @DisplayName("Успешное пополнение увеличивает баланс")
        void deposit_validAmount_shouldIncreaseBalance() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.valueOf(1000))
                    .build();
            BigDecimal depositAmount = BigDecimal.valueOf(500);

            // When
            wallet.deposit(depositAmount);

            // Then
            assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
        }

        @Test
        @DisplayName("Пополнение на очень маленькую сумму")
        void deposit_smallAmount_shouldWork() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.ZERO);

            // When
            wallet.deposit(new BigDecimal("0.01"));

            // Then
            assertThat(wallet.getBalance()).isEqualTo(new BigDecimal("0.01"));
        }

        @Test
        @DisplayName("Пополнение на большую сумму с сохранением точности")
        void deposit_largeAmount_shouldPreservePrecision() {
            // Given
            Wallet wallet = createWalletWithBalance(new BigDecimal("999999.99"));
            BigDecimal amount = new BigDecimal("0.01");

            // When
            wallet.deposit(amount);

            // Then
            assertThat(wallet.getBalance()).isEqualTo(new BigDecimal("1000000.00"));
            assertThat(wallet.getBalance().scale()).isEqualTo(2);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-100", "-0.01"})
        @DisplayName("Отрицательная сумма — выбрасывает IllegalArgumentException")
        void deposit_negativeAmount_shouldThrow(String amountStr) {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.TEN);
            BigDecimal negativeAmount = new BigDecimal(amountStr);

            // When & Then
            assertThatThrownBy(() -> wallet.deposit(negativeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Null сумма — выбрасывает IllegalArgumentException")
        void deposit_nullAmount_shouldThrow() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.TEN);

            // When & Then
            assertThatThrownBy(() -> wallet.deposit(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("withdraw() — снятие средств")
    class WithdrawTests {

        @Test
        @DisplayName("Успешное снятие уменьшает баланс")
        void withdraw_validAmount_shouldDecreaseBalance() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.valueOf(1000))
                    .build();
            BigDecimal withdrawAmount = BigDecimal.valueOf(300);

            // When
            wallet.withdraw(withdrawAmount);

            // Then
            assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(700));
        }

        @Test
        @DisplayName("Снятие всей суммы — баланс становится нулевым")
        void withdraw_fullBalance_shouldResultInZero() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(500));

            // When
            wallet.withdraw(BigDecimal.valueOf(500));

            // Then
            assertThat(wallet.getBalance()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Снятие при недостатке средств — выбрасывает InsufficientFundsException")
        void withdraw_insufficientFunds_shouldThrowException() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(100));
            BigDecimal withdrawAmount = BigDecimal.valueOf(150);

            // When & Then
            assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Balance: 100")
                    .hasMessageContaining("Requested: 150");
        }

        @Test
        @DisplayName("Снятие ровно до нуля — успешно")
        void withdraw_exactBalance_shouldSucceed() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(42.50));

            // When
            wallet.withdraw(BigDecimal.valueOf(42.50));

            // Then
            assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-50", "-0.01"})
        @DisplayName("Отрицательная сумма снятия — выбрасывает исключение")
        void withdraw_negativeAmount_shouldThrow(String amountStr) {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.TEN);

            // When & Then
            assertThatThrownBy(() -> wallet.withdraw(new BigDecimal(amountStr)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Null сумма снятия — выбрасывает исключение")
        void withdraw_nullAmount_shouldThrow() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.TEN);

            // When & Then
            assertThatThrownBy(() -> wallet.withdraw(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("hasSufficientFunds() — проверка достаточности средств")
    class SufficientFundsTests {

        @Test
        @DisplayName("Баланс больше суммы — возвращает true")
        void hasSufficientFunds_balanceGreaterThanAmount_shouldReturnTrue() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(100));

            // When & Then
            assertThat(wallet.hasSufficientFunds(BigDecimal.valueOf(50))).isTrue();
        }

        @Test
        @DisplayName("Баланс равен сумме — возвращает true")
        void hasSufficientFunds_balanceEqualsAmount_shouldReturnTrue() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(100));

            // When & Then
            assertThat(wallet.hasSufficientFunds(BigDecimal.valueOf(100))).isTrue();
        }

        @Test
        @DisplayName("Баланс меньше суммы — возвращает false")
        void hasSufficientFunds_balanceLessThanAmount_shouldReturnFalse() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.valueOf(100));

            // When & Then
            assertThat(wallet.hasSufficientFunds(BigDecimal.valueOf(150))).isFalse();
        }

        @Test
        @DisplayName("Null сумма — возвращает false")
        void hasSufficientFunds_nullAmount_shouldReturnFalse() {
            // Given
            Wallet wallet = createWalletWithBalance(BigDecimal.TEN);

            // When & Then
            assertThat(wallet.hasSufficientFunds(null)).isFalse();
        }
    }

    // === Вспомогательные методы ===

    @Nested
    @DisplayName("Жизненный цикл сущности")
    class LifecycleTests {

        @Test
        @DisplayName("@PrePersist устанавливает createdAt и updatedAt")
        void onCreate_shouldSetTimestamps() {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.TEN)
                    .build();

            // When
            wallet.onCreate();

            // Then
            assertThat(wallet.getCreatedAt()).isNotNull();
            assertThat(wallet.getUpdatedAt()).isNotNull();
            assertThat(wallet.getCreatedAt()).isEqualTo(wallet.getUpdatedAt());
        }

        @Test
        @DisplayName("@PreUpdate обновляет только updatedAt")
        void onUpdate_shouldUpdateOnlyUpdatedAt() throws InterruptedException {
            // Given
            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .balance(BigDecimal.TEN)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build();
            LocalDateTime originalCreatedAt = wallet.getCreatedAt();

            // Небольшая пауза для различия во времени
            Thread.sleep(10);

            // When
            wallet.onUpdate();

            // Then
            assertThat(wallet.getCreatedAt()).isEqualTo(originalCreatedAt); // не изменился
            assertThat(wallet.getUpdatedAt()).isAfter(originalCreatedAt);   // обновился
        }
    }

}