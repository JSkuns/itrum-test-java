package com.wallet.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WalletNotFoundException — тесты")
class WalletNotFoundExceptionTest {

    @Test
    @DisplayName("Конструктор устанавливает сообщение и идентификатор")
    void constructor_shouldSetMessageAndWalletId() {
        // Given
        UUID walletId = UUID.randomUUID();

        // When
        WalletNotFoundException ex = new WalletNotFoundException(walletId);

        // Then
        assertThat(ex.getWalletId()).isEqualTo(walletId);
        assertThat(ex.getMessage()).isEqualTo("Wallet with id " + walletId + " not found");
    }

    @Test
    @DisplayName("Геттер возвращает корректный UUID для фиксированного значения")
    void getWalletId_shouldReturnExactId() {
        // Given
        UUID expectedId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // When
        WalletNotFoundException ex = new WalletNotFoundException(expectedId);

        // Then
        assertThat(ex.getWalletId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Исключение является наследником RuntimeException")
    void shouldBeRuntimeException() {
        assertThat(new WalletNotFoundException(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class);
    }

}