package com.wallet.config;

import com.wallet.listener.WalletRetryListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

import java.net.SocketTimeoutException;

/**
 * Конфигурация механизма повторных попыток (retry) для обработки конкурентных операций с кошельками.
 * <br>
 * Предназначена для обеспечения отказоустойчивости при высокой нагрузке (до 1000 RPS на один кошелёк)
 * и предотвращения ошибок 5xx за счёт автоматического повторения запросов при временных сбоях БД.
 * <br><br>
 * <b>Стратегия повторных попыток:</b>
 * <ul>
 *   <li>Максимум 3 попытки выполнения операции</li>
 *   <li>Экспоненциальная задержка: 50мс → 100мс → 200мс (макс. 1000мс)</li>
 *   <li>Повтор только при временных ошибках БД и сетевых таймаутах</li>
 *   <li>Бизнес-ошибки (недостаточно средств, кошелёк не найден) не ретраятся</li>
 * </ul>
 */
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * <b>Параметры политики повторных попыток:</b>
     * <ul>
     *   <li><b>maxAttempts = 3</b> — общее количество попыток (первая + 2 повтора)</li>
     *   <li><b>initialInterval = 50ms</b> — задержка перед первой повторной попыткой</li>
     *   <li><b>multiplier = 2.0</b> — коэффициент увеличения задержки (экспоненциальный рост)</li>
     *   <li><b>maxInterval = 1000ms</b> — максимальная задержка между попытками</li>
     * </ul>
     * <br>
     * <b>Исключения, при которых выполняется повтор:</b>
     * <ul>
     *   <li>{@link OptimisticLockingFailureException} — конфликт версий при оптимистической блокировке</li>
     *   <li>{@link CannotAcquireLockException} — не удалось получить пессимистическую блокировку (таймаут)</li>
     *   <li>{@link TransientDataAccessException} — временная ошибка доступа к данным</li>
     *   <li>{@link SocketTimeoutException} — таймаут сетевого соединения с БД</li>
     * </ul>
     * <br>
     * <b>Важно:</b> Исключения, не указанные в {@code retryOn()}, по умолчанию не приводят к повтору.
     * Это гарантирует, что бизнес-ошибки (например, {@code InsufficientFundsException})
     * сразу возвращаются клиенту без лишних попыток.
     *
     * @param retryListener слушатель для логирования событий retry
     * @return настроенный экземпляр {@link RetryTemplate}
     */
    @Bean
    public RetryTemplate walletRetryTemplate(WalletRetryListener retryListener) {
        RetryTemplate retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(50, 2.0, 1000)

                .retryOn(OptimisticLockingFailureException.class)
                .retryOn(CannotAcquireLockException.class)
                .retryOn(TransientDataAccessException.class)
                .retryOn(SocketTimeoutException.class)

                .build();

        retryTemplate.registerListener(retryListener);

        return retryTemplate;
    }

}