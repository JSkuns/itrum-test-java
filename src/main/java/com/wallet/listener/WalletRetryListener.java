package com.wallet.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Слушатель событий механизма повторных попыток (retry) для операций с кошельками.
 * <br>
 * Регистрируется в {@link com.wallet.config.RetryConfig} и вызывает
 * методы логирования на каждом этапе жизненного цикла retry:
 * <ul>
 *   <li>{@link #open} — перед первой попыткой выполнения операции</li>
 *   <li>{@link #onError} — после каждой неудачной попытки</li>
 *   <li>{@link #close} — после завершения (успех или исчерпание попыток)</li>
 * </ul>
 *
 * @see com.wallet.config.RetryConfig
 */
@Component
@Slf4j
public class WalletRetryListener implements RetryListener {

    /**
     * Вызывается перед первой попыткой выполнения операции.
     * <br>
     * Логирует начало retry-цикла на уровне {@code DEBUG}.
     * <br>
     * Возвращает {@code true} всегда, чтобы не блокировать retry-механизм.
     *
     * @param context  контекст retry с информацией о попытках
     * @param callback колбэк, содержащий логику операции
     * @return {@code true} для продолжения выполнения; {@code false} для прерывания
     */
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.debug("Retry started for operation: {}", callback.getClass().getSimpleName());
        return true;
    }

    /**
     * Вызывается после завершения всех попыток (успех или провал).
     * <br>
     * <b>Если {@code throwable == null}</b> — операция завершилась успешно,
     * возможно после нескольких повторов (логируем на {@code INFO}).
     * <br>
     * <b>Если {@code throwable != null}</b> — все попытки исчерпаны,
     * операция провалена (логируем на {@code WARN}).
     *
     * @param context   контекст retry с итоговой статистикой
     * @param callback  колбэк с логикой операции
     * @param throwable {@code null} при успехе или финальное исключение при провале
     */
    @Override
    public <T, E extends Throwable> void close(RetryContext context,
                                               RetryCallback<T, E> callback,
                                               Throwable throwable) {
        if (throwable != null) {
            log.warn("Retry failed after {} attempts: {} - {}",
                    context.getRetryCount(),
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage());
        } else if (context.getRetryCount() > 0) {
            log.info("Retry succeeded after {} attempts", context.getRetryCount());
        }
    }

    /**
     * Вызывается после каждой неудачной попытки выполнения операции.
     * <br>
     * Логирует детали ошибки на уровне {@code WARN} для мониторинга
     * конкурентных конфликтов и временных сбоев.
     * <br>
     * Не выбрасывает исключения, чтобы не нарушать работу retry-механизма.
     *
     * @param context   контекст retry с номером текущей попытки
     * @param callback  колбэк с логикой операции
     * @param throwable исключение, вызвавшее неудачу
     */
    @Override
    public <T, E extends Throwable> void onError(RetryContext context,
                                                 RetryCallback<T, E> callback,
                                                 Throwable throwable) {
        log.warn("Retry attempt {} failed: {} - {}",
                context.getRetryCount(),
                throwable.getClass().getSimpleName(),
                throwable.getMessage());
    }

}