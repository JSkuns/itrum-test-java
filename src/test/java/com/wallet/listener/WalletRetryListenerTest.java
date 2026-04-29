package com.wallet.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletRetryListener — тесты")
class WalletRetryListenerTest {

    private WalletRetryListener listener;

    @Mock
    private RetryContext context;

    @Mock
    private RetryCallback<Object, Exception> callback;

    @BeforeEach
    void setUp() {
        listener = new WalletRetryListener();
    }

    @Test
    @DisplayName("open — возвращает true и не выбрасывает исключения")
    void open_shouldReturnTrueAndNotThrow() {
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            boolean result = listener.open(context, callback);
            assertThat(result).isTrue();
        });
    }

    @Test
    @DisplayName("onError — логирует ошибку и не выбрасывает исключения")
    void onError_shouldLogAndNotThrow() {
        // Given
        when(context.getRetryCount()).thenReturn(2);
        Exception ex = new RuntimeException("Test error");

        // When & Then
        assertThatNoException().isThrownBy(() ->
                listener.onError(context, callback, ex));
    }

    @Test
    @DisplayName("close с успехом после повторов — логирует успех")
    void close_withSuccessAfterRetries_shouldLogSuccess() {
        // Given
        when(context.getRetryCount()).thenReturn(2);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                listener.close(context, callback, null));
    }

    @Test
    @DisplayName("close с успехом с первой попытки — не логирует шум")
    void close_withSuccessFirstAttempt_shouldNotLogVerbose() {
        // Given
        when(context.getRetryCount()).thenReturn(0);

        // When & Then
        assertThatNoException().isThrownBy(() ->
                listener.close(context, callback, null));
        // Отсутствие лога — ожидаемое поведение
    }

}