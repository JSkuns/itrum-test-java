package com.wallet.service;

import com.wallet.exception.WalletNotFoundException;
import com.wallet.model.dto.WalletResponse;
import com.wallet.model.entity.Wallet;
import com.wallet.model.enums.OperationType;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletServiceImpl — юнит-тесты")
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RetryTemplate retryTemplate;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet createWallet(UUID id, BigDecimal balance) {
        return Wallet.builder()
                .id(id)
                .balance(balance)
                .version(0L)
                .build();
    }

    @Nested
    @DisplayName("processOperation — выполнение операций")
    class ProcessOperationTests {

        @Test
        @DisplayName("DEPOSIT — успешное пополнение")
        void processOperation_deposit_shouldUpdateBalance() {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal initialBalance = BigDecimal.valueOf(1000);
            BigDecimal depositAmount = BigDecimal.valueOf(500);
            Wallet wallet = createWallet(walletId, initialBalance);

            given(walletRepository.findByIdWithPessimisticLock(walletId))
                    .willReturn(Optional.of(wallet));
            given(walletRepository.save(any(Wallet.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            doAnswer(invocation -> {
                RetryCallback<WalletResponse, Exception> callback = invocation.getArgument(0);
                return callback.doWithRetry(mock(RetryContext.class));
            }).when(retryTemplate).execute(any());

            // When
            WalletResponse response = walletService.processOperation(
                    walletId, OperationType.DEPOSIT, depositAmount);

            // Then
            assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
            verify(walletRepository).findByIdWithPessimisticLock(walletId);
            verify(walletRepository).save(wallet);
        }

        @Test
        @DisplayName("WITHDRAW — успешное снятие")
        void processOperation_withdraw_shouldUpdateBalance() {
            // Given
            UUID walletId = UUID.randomUUID();
            BigDecimal initialBalance = BigDecimal.valueOf(1000);
            BigDecimal withdrawAmount = BigDecimal.valueOf(300);
            Wallet wallet = createWallet(walletId, initialBalance);

            given(walletRepository.findByIdWithPessimisticLock(walletId))
                    .willReturn(Optional.of(wallet));
            given(walletRepository.save(any(Wallet.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            doAnswer(invocation -> {
                RetryCallback<WalletResponse, Exception> callback = invocation.getArgument(0);
                return callback.doWithRetry(mock(RetryContext.class));
            }).when(retryTemplate).execute(any());

            // When
            WalletResponse response = walletService.processOperation(
                    walletId, OperationType.WITHDRAW, withdrawAmount);

            // Then
            assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(700));
        }

        @Test
        @DisplayName("Кошелёк не найден — выбрасывает WalletNotFoundException")
        void processOperation_walletNotFound_shouldThrowException() {
            // Given
            UUID walletId = UUID.randomUUID();
            given(walletRepository.findByIdWithPessimisticLock(walletId))
                    .willReturn(Optional.empty());

            doAnswer(invocation -> {
                RetryCallback<WalletResponse, Exception> callback = invocation.getArgument(0);
                return callback.doWithRetry(mock(RetryContext.class));
            }).when(retryTemplate).execute(any());

            // When & Then
            assertThatThrownBy(() ->
                    walletService.processOperation(walletId, OperationType.DEPOSIT, BigDecimal.TEN))
                    .isInstanceOf(WalletNotFoundException.class)
                    .extracting("walletId")
                    .isEqualTo(walletId);
        }

        @Test
        @DisplayName("Недостаточно средств — выбрасывает InsufficientFundsException")
        void processOperation_insufficientFunds_shouldThrowException() {
            // Given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = createWallet(walletId, BigDecimal.valueOf(100));

            given(walletRepository.findByIdWithPessimisticLock(walletId))
                    .willReturn(Optional.of(wallet));

            doAnswer(invocation -> {
                RetryCallback<WalletResponse, Exception> callback = invocation.getArgument(0);
                return callback.doWithRetry(mock(RetryContext.class));
            }).when(retryTemplate).execute(any());

            // When & Then
            assertThatThrownBy(() ->
                    walletService.processOperation(walletId, OperationType.WITHDRAW, BigDecimal.valueOf(150)))
                    .isInstanceOf(com.wallet.exception.InsufficientFundsException.class);
        }

        @Test
        @DisplayName("RetryTemplate применяется к операции")
        void processOperation_shouldUseRetryTemplate() {
            // Given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = createWallet(walletId, BigDecimal.TEN);

            given(walletRepository.findByIdWithPessimisticLock(walletId))
                    .willReturn(Optional.of(wallet));
            given(walletRepository.save(any(Wallet.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            doAnswer(invocation -> {
                RetryCallback<WalletResponse, Exception> callback = invocation.getArgument(0);
                return callback.doWithRetry(mock(RetryContext.class));
            }).when(retryTemplate).execute(any());

            // When
            WalletResponse response = walletService.processOperation(
                    walletId, OperationType.DEPOSIT, BigDecimal.TEN);

            // Then
            assertThat(response).isNotNull();
            verify(retryTemplate).execute(any());
            verify(walletRepository).findByIdWithPessimisticLock(walletId);
            verify(walletRepository).save(wallet);
        }
    }

    // === Вспомогательные методы ===

    @Nested
    @DisplayName("getWalletBalance — получение баланса")
    class GetBalanceTests {

        @Test
        @DisplayName("Успешный запрос — возвращает баланс")
        void getWalletBalance_success_shouldReturnBalance() {
            // Given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = createWallet(walletId, BigDecimal.valueOf(1234.56));

            given(walletRepository.findById(walletId))
                    .willReturn(Optional.of(wallet));

            // When
            WalletResponse response = walletService.getWalletBalance(walletId);

            // Then
            assertThat(response.getWalletId()).isEqualTo(walletId);
            assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(1234.56));
            verify(walletRepository).findById(walletId);
            verifyNoMoreInteractions(walletRepository);
        }

        @Test
        @DisplayName("Кошелёк не найден — выбрасывает WalletNotFoundException")
        void getWalletBalance_notFound_shouldThrowException() {
            // Given
            UUID walletId = UUID.randomUUID();
            given(walletRepository.findById(walletId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletService.getWalletBalance(walletId))
                    .isInstanceOf(WalletNotFoundException.class);
        }

        @Test
        @DisplayName("Метод помечен readOnly — не вызывает save()")
        void getWalletBalance_shouldNotModifyData() {
            // Given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = createWallet(walletId, BigDecimal.TEN);
            given(walletRepository.findById(walletId)).willReturn(Optional.of(wallet));

            // When
            walletService.getWalletBalance(walletId);

            // Then
            verify(walletRepository).findById(walletId);
            verify(walletRepository, never()).save(any());
            verify(walletRepository, never()).delete(any());
        }
    }

}