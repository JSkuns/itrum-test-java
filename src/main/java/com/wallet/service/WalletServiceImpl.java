package com.wallet.service;

import com.wallet.exception.InsufficientFundsException;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.model.dto.WalletResponse;
import com.wallet.model.entity.Wallet;
import com.wallet.model.enums.OperationType;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Реализация {@link WalletService} с поддержкой высокой конкуренции.
 * <br>
 * <b>Ключевые механизмы:</b>
 * <ul>
 *   <li><b>RetryTemplate</b> — автоматические повторные попытки при временных ошибках БД</li>
 *   <li><b>Pessimistic locking</b> — блокировка строки на уровне СУБД для предотвращения потерь обновлений</li>
 *   <li><b>Transactional</b> — атомарность операций и откат при ошибках</li>
 * </ul>
 *
 * @see WalletService
 * @see com.wallet.config.RetryConfig
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final RetryTemplate retryTemplate;

    @Override
    @Transactional
    public WalletResponse processOperation(UUID walletId, OperationType operationType, BigDecimal amount) {
        return retryTemplate.execute(ctx -> doProcessOperation(walletId, operationType, amount));
    }

    /**
     * Внутренний метод выполнения операции (без retry-обёртки).
     * <br>
     * Вызывается через {@link RetryTemplate#execute(org.springframework.retry.RetryCallback)},
     * поэтому может выполняться несколько раз при временных сбоях.
     *
     * @param walletId      UUID кошелька
     * @param operationType тип операции
     * @param amount        сумма
     * @return DTO с результатом
     * @throws WalletNotFoundException    если кошелёк не найден
     * @throws InsufficientFundsException если недостаточно средств
     */
    private WalletResponse doProcessOperation(UUID walletId, OperationType operationType, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdWithPessimisticLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        switch (operationType) {
            case DEPOSIT -> wallet.deposit(amount);
            case WITHDRAW -> wallet.withdraw(amount);
        }

        Wallet saved = walletRepository.save(wallet);
        log.info("Operation {} completed for wallet {}: new balance {}",
                operationType, walletId, saved.getBalance());

        return WalletResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletBalance(UUID walletId) {
        // Для чтения не нужна блокировка — используем стандартный findById
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return WalletResponse.fromEntity(wallet);
    }

}