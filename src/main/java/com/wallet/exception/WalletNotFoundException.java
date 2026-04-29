package com.wallet.exception;

import java.util.UUID;

/**
 * Исключение, указывающее на то, что запрошенный кошелёк не найден в базе данных.
 * <br>
 * Выбрасывается сервисным слоем ({@code WalletService}) или репозиторием при попытке
 * выполнить операцию с несуществующим идентификатором.
 * Перехватывается {@link GlobalExceptionHandler} и возвращается клиенту
 *
 * @see com.wallet.service.WalletService
 * @see com.wallet.exception.GlobalExceptionHandler
 */
public class WalletNotFoundException extends RuntimeException {

    /**
     * Идентификатор кошелька, который не был найден
     */
    private final UUID walletId;

    /**
     * Создаёт исключение для указанного идентификатора кошелька.
     *
     * @param walletId UUID кошелька, который не был найден
     */
    public WalletNotFoundException(UUID walletId) {
        super("Wallet with id " + walletId + " not found");
        this.walletId = walletId;
    }

    /**
     * Возвращает идентификатор кошелька, вызвавший ошибку.
     * Может использоваться в обработчиках исключений для логирования
     * или формирования детализированного ответа клиенту.
     *
     * @return UUID кошелька
     */
    public UUID getWalletId() {
        return walletId;
    }

}