package com.wallet.exception;

/**
 * Исключение, выбрасываемое при попытке снять с кошелька сумму,
 * превышающую доступный баланс.
 * <br>
 * Используется в бизнес-логике сервиса {@code WalletService} для обработки
 * операций снятия средств ({@code WITHDRAW}). Возвращает клиенту понятное
 * сообщение с указанием текущего баланса и запрошенной суммы.
 *
 * @see com.wallet.service.WalletService
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

}