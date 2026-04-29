package com.wallet.repository;

import com.wallet.model.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link Wallet}.
 * <br>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository}
 * и специализированные методы для обработки конкурентных запросов.
 * <br>
 * <b>Особенности:</b>
 * <ul>
 *   <li>Использует {@code UUID} в качестве первичного ключа</li>
 *   <li>Поддерживает пессимистическую блокировку для операций с высокой конкуренцией</li>
 *   <li>Интегрируется с механизмом оптимистической блокировки через поле {@code @Version}</li>
 * </ul>
 *
 * @see Wallet
 * @see com.wallet.service.WalletService
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Находит кошелёк по идентификатору с пессимистической блокировкой на уровне БД.
     * <br>
     * <b>Когда использовать:</b>
     * <ul>
     *   <li>Операции изменения баланса ({@code DEPOSIT}/{@code WITHDRAW})</li>
     *   <li>Высокая конкуренция за один кошелёк (до 1000 RPS)</li>
     *   <li>Критичные финансовые операции, где важна консистентность</li>
     * </ul>
     * <br>
     * <b>Механизм работы:</b>
     * <ol>
     *   <li>Выполняет {@code SELECT ... FOR UPDATE} в PostgreSQL</li>
     *   <li>Блокирует строку до завершения транзакции</li>
     *   <li>Другие транзакции ждут освобождения блокировки (или получают таймаут)</li>
     * </ol>
     *
     * @param id UUID кошелька
     * @return {@link Optional} с найденным кошельком или пустой, если не найден
     * @see LockModeType#PESSIMISTIC_WRITE
     * @see com.wallet.config.RetryConfig
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithPessimisticLock(@Param("id") UUID id);

}
