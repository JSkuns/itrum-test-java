package com.wallet.controller;

import com.wallet.model.dto.WalletRequest;
import com.wallet.model.dto.WalletResponse;
import com.wallet.service.WalletService;
import com.wallet.validation.ValidUUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контроллер для управления операциями с кошельками.
 * <br>
 * Предоставляет эндпоинты для:
 * <ul>
 *   <li>Выполнения операций пополнения и снятия средств ({@code POST /api/v1/wallet})</li>
 *   <li>Получения текущего баланса кошелька ({@code GET /api/v1/wallets/{id}})</li>
 * </ul>
 * <br>
 * <b>Безопасность:</b> все входные данные валидируются через Bean Validation.
 * UUID проверяется через кастомный валидатор {@link ValidUUID}.
 * <br>
 * <b>Обработка ошибок:</b> исключения перехватываются {@code GlobalExceptionHandler}
 * и возвращаются в стандартизированном формате {@code ErrorResponse}.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class WalletController {

    private final WalletService walletService;

    /**
     * Выполняет операцию пополнения или снятия средств с кошелька.
     * <br>
     * <b>Логика:</b>
     * <ol>
     *   <li>Валидирует входной запрос ({@code @Valid})</li>
     *   <li>Вызывает сервис для выполнения операции</li>
     *   <li>Возвращает обновлённый баланс кошелька</li>
     * </ol>
     * Метод не обрабатывает исключения — это делает {@code GlobalExceptionHandler}
     *
     * @param request запрос с параметрами операции:
     *                <ul>
     *                  <li>{@code walletId} — UUID кошелька (обязателен, валидный UUID)</li>
     *                  <li>{@code operationType} — тип операции: {@code DEPOSIT} или {@code WITHDRAW}</li>
     *                  <li>{@code amount} — сумма операции (обязательна, положительная)</li>
     *                </ul>
     * @return {@link ResponseEntity} с {@link WalletResponse}, содержащим актуальный баланс
     * @throws jakarta.validation.ConstraintViolationException если запрос не прошёл валидацию
     * @throws com.wallet.exception.WalletNotFoundException    если кошелёк не найден
     * @throws com.wallet.exception.InsufficientFundsException если недостаточно средств при снятии
     */
    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processOperation(
            @RequestBody @Valid WalletRequest request) {

        var response = walletService.processOperation(
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Возвращает текущий баланс кошелька по его идентификатору.
     *
     * @param walletId UUID кошелька (проверяется через {@link ValidUUID})
     * @return {@link ResponseEntity} с {@link WalletResponse}, содержащим баланс
     * @throws com.wallet.exception.WalletNotFoundException    если кошелёк не найден
     * @throws jakarta.validation.ConstraintViolationException если UUID невалиден
     */
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getWalletBalance(
            @PathVariable @ValidUUID UUID walletId) {

        var response = walletService.getWalletBalance(walletId);
        return ResponseEntity.ok(response);
    }

}