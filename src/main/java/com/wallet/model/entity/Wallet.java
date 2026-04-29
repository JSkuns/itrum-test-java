package com.wallet.model.entity;

import com.wallet.exception.InsufficientFundsException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность кошелька для хранения баланса и истории операций.
 * <br>
 * Отображается на таблицу {@code wallets} в базе данных PostgreSQL.
 * Использует оптимистическую блокировку через поле {@code @Version}
 * для безопасной обработки конкурентных запросов (до 1000 RPS на кошелёк).
 * <br><br>
 * <b>Бизнес-правила:</b>
 * <ul>
 *   <li>Баланс не может быть отрицательным</li>
 *   <li>Операции снятия средств проверяют достаточность баланса</li>
 *   <li>Временные метки создаются/обновляются автоматически</li>
 * </ul>
 *
 * @see com.wallet.repository.WalletRepository
 * @see com.wallet.service.WalletService
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    /**
     * Уникальный идентификатор кошелька (первичный ключ).
     * <br>
     * Генерируется клиентом или сервисом до сохранения в БД.
     * Формат: UUID v4 ({@code 8-4-4-4-12}).
     */
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * Текущий баланс кошелька.
     * <p>
     * <b>Характеристики:</b>
     * <ul>
     *   <li>Тип: {@link BigDecimal} для точности до 2 знаков после запятой</li>
     *   <li>Ограничение БД: {@code NOT NULL}, {@code precision=19, scale=2}</li>
     *   <li>Бизнес-правило: всегда {@code >= 0}</li>
     * </ul>
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Версия сущности для оптимистической блокировки.
     * <br>
     * Автоматически увеличивается при каждом обновлении записи.
     * Позволяет обнаруживать конфликтующие изменения при высокой нагрузке.
     */
    @Version
    private Long version;

    /**
     * Время создания записи. Заполняется автоматически при первом сохранении.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Время последнего обновления записи. Обновляется при каждом изменении.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Автоматически устанавливает временные метки при создании сущности.
     * Вызывается JPA-провайдером перед {@code INSERT}.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет временную метку при изменении сущности.
     * Вызывается JPA-провайдером перед {@code UPDATE}.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Выполняет операцию пополнения баланса кошелька.
     * <br>
     * Метод не сохраняет изменения в БД — это делает репозиторий/сервис
     * <br>
     * <b>Побочные эффекты:</b>
     * <ul>
     *   <li>Увеличивает поле {@code balance} на {@code amount}</li>
     *   <li>Не проверяет валидность суммы (валидация на уровне DTO/сервиса)</li>
     * </ul>
     *
     * @param amount сумма пополнения (должна быть положительной)
     * @throws IllegalArgumentException если {@code amount} равен {@code null} или отрицательный
     */
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Выполняет операцию снятия средств с кошелька.
     * <br>
     * Метод не сохраняет изменения в БД — это делает репозиторий/сервис
     * <br><br>
     * <b>Бизнес-правило:</b> снятие возможно только при достаточном балансе.
     * <br><br>
     * <b>Побочные эффекты:</b>
     * <ul>
     *   <li>Уменьшает поле {@code balance} на {@code amount}</li>
     *   <li>Выбрасывает {@link InsufficientFundsException} при недостатке средств</li>
     * </ul>
     *
     * @param amount сумма снятия (должна быть положительной)
     * @throws InsufficientFundsException если баланс меньше запрошенной суммы
     * @throws IllegalArgumentException   если {@code amount} равен {@code null} или отрицательный
     */
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Balance: " + this.balance + ", Requested: " + amount);
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Проверяет, достаточно ли средств на кошельке для заданной суммы.
     * <br>
     * Удобный метод для предварительной проверки перед вызовом {@link #withdraw(BigDecimal)}.
     *
     * @param amount запрошенная сумма
     * @return {@code true} если баланс >= amount, иначе {@code false}
     */
    public boolean hasSufficientFunds(BigDecimal amount) {
        return amount != null && this.balance.compareTo(amount) >= 0;
    }

}
