package com.wallet.service;

import com.wallet.config.RetryConfig;
import com.wallet.exception.WalletNotFoundException;
import com.wallet.listener.WalletRetryListener;
import com.wallet.model.dto.WalletResponse;
import com.wallet.model.entity.Wallet;
import com.wallet.model.enums.OperationType;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = {
        RetryConfig.class,
        WalletServiceImpl.class,
        WalletRetryListener.class
})
@DisplayName("WalletService — тесты retry-механизма")
class WalletServiceRetryIntegrationTest {

    @Autowired
    private WalletService walletService;

    @MockBean
    private WalletRepository walletRepository;

    @Test
    @DisplayName("processOperation — повтор при OptimisticLockingFailureException")
    void processOperation_shouldRetryOnOptimisticLockingFailure() {
        // Given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setVersion(1L);

        // Первый вызов — ошибка, второй — успех
        when(walletRepository.findByIdWithPessimisticLock(walletId))
                .thenThrow(new OptimisticLockingFailureException("Lock failed"))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        WalletResponse response = walletService.processOperation(
                walletId, OperationType.DEPOSIT, BigDecimal.valueOf(100));

        // Then
        assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(1100));
        verify(walletRepository, times(2)).findByIdWithPessimisticLock(walletId);
    }

    @Test
    @DisplayName("processOperation — бизнес-ошибка НЕ ретраится")
    void processOperation_shouldNotRetryOnBusinessException() {
        // Given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findByIdWithPessimisticLock(walletId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                walletService.processOperation(walletId, OperationType.DEPOSIT, BigDecimal.TEN))
                .isInstanceOf(WalletNotFoundException.class);

        // Проверяем, что был только 1 вызов (без повторов)
        verify(walletRepository, times(1)).findByIdWithPessimisticLock(walletId);
    }

}