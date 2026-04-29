package com.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.model.dto.WalletRequest;
import com.wallet.model.dto.WalletResponse;
import com.wallet.model.enums.OperationType;
import com.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@DisplayName("WalletController — юнит-тесты")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @Nested
    @DisplayName("POST /api/v1/wallet — выполнение операции")
    class ProcessOperationTests {

        @Test
        @DisplayName("DEPOSIT — успешное пополнение")
        void processOperation_deposit_shouldReturnUpdatedBalance() throws Exception {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(1000.50);
            WalletRequest request = new WalletRequest(walletId, OperationType.DEPOSIT, amount);
            WalletResponse expectedResponse = new WalletResponse(walletId, BigDecimal.valueOf(2000.50));

            given(walletService.processOperation(eq(walletId), eq(OperationType.DEPOSIT), eq(amount)))
                    .willReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                    .andExpect(jsonPath("$.balance").value(2000.50));

            verify(walletService).processOperation(eq(walletId), eq(OperationType.DEPOSIT), eq(amount));
        }

        @Test
        @DisplayName("WITHDRAW — успешное снятие")
        void processOperation_withdraw_shouldReturnUpdatedBalance() throws Exception {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);
            WalletRequest request = new WalletRequest(walletId, OperationType.WITHDRAW, amount);
            WalletResponse expectedResponse = new WalletResponse(walletId, BigDecimal.valueOf(500));

            given(walletService.processOperation(eq(walletId), eq(OperationType.WITHDRAW), eq(amount)))
                    .willReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(500));
        }

        @Test
        @DisplayName("Невалидный JSON — возвращает 400")
        void processOperation_invalidJson_shouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"not valid json\""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Отсутствует обязательное поле — возвращает 400")
        void processOperation_missingField_shouldReturn400() throws Exception {
            // Given — request без amount
            String invalidJson = """
                    {
                      "walletId": "550e8400-e29b-41d4-a716-446655440000",
                      "operationType": "DEPOSIT"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("ValidationError"));
        }

        @Test
        @DisplayName("Невалидный UUID — возвращает 400")
        void processOperation_invalidUuid_shouldReturn400() throws Exception {
            // Given
            String invalidJson = """
                    {
                      "walletId": "not-a-uuid",
                      "operationType": "DEPOSIT",
                      "amount": 100
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Отрицательная сумма — возвращает 400")
        void processOperation_negativeAmount_shouldReturn400() throws Exception {
            // Given
            WalletRequest request = new WalletRequest(
                    UUID.randomUUID(),
                    OperationType.DEPOSIT,
                    BigDecimal.valueOf(-100)
            );

            // When & Then
            mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/wallets/{id} — получение баланса")
    class GetBalanceTests {

        @Test
        @DisplayName("Успешный запрос — возвращает баланс")
        void getWalletBalance_success_shouldReturnBalance() throws Exception {
            // Given
            UUID walletId = UUID.randomUUID();
            WalletResponse expectedResponse = new WalletResponse(walletId, BigDecimal.valueOf(1500.75));

            given(walletService.getWalletBalance(walletId)).willReturn(expectedResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                    .andExpect(jsonPath("$.balance").value(1500.75));

            verify(walletService).getWalletBalance(walletId);
        }

        @Test
        @DisplayName("Невалидный UUID в path — возвращает 400")
        void getWalletBalance_invalidUuid_shouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/wallets/{walletId}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

}