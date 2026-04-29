package com.wallet.config;

import com.wallet.listener.WalletRetryListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryConfig — проверка создания конфигурации")
class RetryConfigTest {

    private final RetryConfig config = new RetryConfig();
    @Mock
    private WalletRetryListener retryListener;

    @Test
    @DisplayName("walletRetryTemplate создаёт RetryTemplate bean")
    void walletRetryTemplate_shouldCreateBean() {
        // When
        RetryTemplate template = config.walletRetryTemplate(retryListener);

        // Then
        assertThat(template).isNotNull();
    }

}