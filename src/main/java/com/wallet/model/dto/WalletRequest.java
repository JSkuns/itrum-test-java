package com.wallet.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wallet.model.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для запроса операции с кошельком (пополнение или снятие средств).
 * <br>
 * Используется в эндпоинте {@code POST /api/v1/wallet} контроллера
 * {@link com.wallet.controller.WalletController}.
 * Все поля валидируются через Bean Validation перед обработкой в сервисе.
 *
 * @see com.wallet.controller.WalletController#processOperation(WalletRequest)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRequest {

    /**
     * Уникальный идентификатор кошелька (UUID v4).
     * <br>
     * Должен соответствовать формату {@code 8-4-4-4-12} шестнадцатеричных цифр.
     * Пример: {@code 550e8400-e29b-41d4-a716-446655440000}
     */
    @NotNull(message = "Wallet ID cannot be null")
    private UUID walletId;

    /**
     * Тип финансовой операции.
     */
    @NotNull(message = "Operation type cannot be null")
    private OperationType operationType;

    /**
     * Сумма операции в основной валюте системы.
     */
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

}