package com.wallet.service;

import com.wallet.model.dto.WalletResponse;
import com.wallet.model.enums.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сервис для управления операциями с кошельками.
 * <br>
 * Предоставляет методы для:
 * <ul>
 *   <li>Выполнения операций пополнения и снятия средств</li>
 *   <li>Получения текущего баланса кошелька</li>
 * </ul>
 *
 * @see com.wallet.controller.WalletController
 * @see com.wallet.repository.WalletRepository
 */
public interface WalletService {

    /**
     * Выполняет финансовую операцию (пополнение или снятие) с кошельком.
     *
     * @param walletId      UUID кошелька
     * @param operationType тип операции
     * @param amount        сумма операции
     * @return {@link WalletResponse} с обновлённым балансом
     */
    WalletResponse processOperation(UUID walletId, OperationType operationType, BigDecimal amount);

    /**
     * Возвращает текущий баланс кошелька по его идентификатору.
     * <br>
     * Метод не модифицирует данные, поэтому может выполняться параллельно
     * без риска конфликтов.
     *
     * @param walletId UUID кошелька
     * @return {@link WalletResponse} с текущим балансом
     */
    WalletResponse getWalletBalance(UUID walletId);

}