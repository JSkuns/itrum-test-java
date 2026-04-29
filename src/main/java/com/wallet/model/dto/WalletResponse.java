package com.wallet.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wallet.model.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа с информацией о кошельке.
 * <br>
 * Возвращается клиенту в эндпоинтах:
 * <ul>
 *   <li>{@code POST /api/v1/wallet} — после выполнения операции</li>
 *   <li>{@code GET /api/v1/wallets/{id}} — при получении баланса</li>
 * </ul>
 *
 * @see com.wallet.controller.WalletController
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletResponse {

    /**
     * Уникальный идентификатор кошелька (UUID v4).
     * <p>
     * Формат: {@code 8-4-4-4-12} шестнадцатеричных цифр.
     * Пример: {@code 550e8400-e29b-41d4-a716-446655440000}
     */
    private UUID walletId;

    /**
     * Текущий баланс кошелька в основной валюте системы.
     */
    private BigDecimal balance;

    /**
     * Создаёт DTO из JPA-сущности {@link Wallet}.
     * <br>
     * Используется в сервисном слое для преобразования результата
     * операции с базой данных в формат, пригодный для отправки клиенту.
     *
     * @param wallet сущность кошелька из базы данных (не может быть {@code null})
     * @return DTO с идентификатором и балансом кошелька
     */
    public static WalletResponse fromEntity(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .build();
    }

}