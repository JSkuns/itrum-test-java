package com.wallet.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для REST API.
 * <br>
 * Перехватывает исключения, выброшенные на уровне контроллеров, и возвращает
 * стандартизированный {@link ErrorResponse} с соответствующим HTTP-статусом.
 * Это позволяет скрыть детали внутренней реализации (Stack Trace) от клиента.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ситуацию, когда запрошенный кошелёк не найден.
     *
     * @param ex исключение с информацией о missing walletId
     * @return {@link ResponseEntity} со статусом 404 Not Found
     */
    @ExceptionHandler(WalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex) {
        log.warn("Wallet not found: {}", ex.getWalletId());
        return buildResponse(HttpStatus.NOT_FOUND, "WalletNotFound", "Wallet with id " + ex.getWalletId() + " not found");
    }

    /**
     * Обрабатывает ошибку недостатка средств на кошельке.
     *
     * @param ex исключение с деталями операции
     * @return {@link ResponseEntity} со статусом 400 Bad Request
     */
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "InsufficientFunds", ex.getMessage());
    }

    /**
     * Обрабатывает ошибки валидации входных данных (аннотации @Valid).
     *
     * @param ex исключение со списком ошибок валидации
     * @return {@link ResponseEntity} со статусом 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "ValidationError", message);
    }

    /**
     * Обрабатывает ошибки парсинга JSON (например, неверный формат или битый синтаксис).
     *
     * @param ex исключение с описанием ошибки
     * @return {@link ResponseEntity} со статусом 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.error("Invalid JSON: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "InvalidJson", "Request body is not valid JSON");
    }

    /**
     * Обрабатывает ошибки несоответствия типов параметров (например, невалидный UUID в Path Variable).
     *
     * @param ex исключение с деталями mismatch
     * @return {@link ResponseEntity} со статусом 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch error: {}", ex.getMessage());
        String message = String.format("Invalid value for parameter '%s': %s",
                ex.getName(), ex.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, "ValidationError", message);
    }

    /**
     * Обрабатывает ошибки конкурентного доступа (конфликт версий), если Retry-механизм исчерпан.
     *
     * @param ex исключение блокировки
     * @return {@link ResponseEntity} со статусом 409 Conflict
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Optimistic lock failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "ConcurrentModification", "Please retry your request");
    }

    /**
     * Общий обработчик для всех остальных непредвиденных исключений.
     * Возвращает 500 Internal Server Error.
     *
     * @param ex исключение
     * @return {@link ResponseEntity} со статусом 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "InternalError", "An unexpected error occurred");
    }

    /**
     * Вспомогательный метод для сборки ответа об ошибке.
     *
     * @param status  HTTP статус
     * @param error   тип ошибки
     * @param message сообщение для клиента
     * @return готовый ResponseEntity
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

}