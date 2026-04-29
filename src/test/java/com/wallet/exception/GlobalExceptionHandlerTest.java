package com.wallet.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleWalletNotFound — возврат 404 с path")
    void handleWalletNotFound_returns404WithPath() {
        // Given
        UUID walletId = UUID.randomUUID();

        // When
        ResponseEntity<ErrorResponse> response = handler.handleWalletNotFound(
                new WalletNotFoundException(walletId));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("WalletNotFound");
        assertThat(response.getBody().getMessage()).contains(walletId.toString());
    }

    @Test
    @DisplayName("handleInsufficientFunds — возврат 400")
    void handleInsufficientFunds_returns400() {
        var response = handler.handleInsufficientFunds(
                new InsufficientFundsException(
                        String.format("Insufficient funds. Current balance: %.2f, requested: %.2f", 10d, 100d)
                ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("InsufficientFunds");
    }

    @Test
    @DisplayName("handleTypeMismatch — возврат 400 с валидным URI")
    void handleTypeMismatch_returns400WithPath() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid-uuid", UUID.class, "walletId", null, new IllegalArgumentException("Invalid UUID"));

        var response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("invalid-uuid");
    }

}