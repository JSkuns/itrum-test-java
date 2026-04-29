package com.wallet.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Унифицированная модель ответа об ошибке для REST API.
 * <br>
 * Используется в {@link GlobalExceptionHandler} для формирования
 * стандартизированного JSON-ответа при возникновении исключений.
 * <br>
 * <b>Пример JSON-ответа:</b>
 * <pre>{@code
 * {
 *   "status": 404,
 *   "error": "WalletNotFound",
 *   "message": "Wallet with id 123e4567-e89b-12d3-a456-426614174000 not found",
 *   "timestamp": "2024-01-01T12:00:00",
 * }
 * }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP-статус ответа (400, 404, 500 и т.д.)
     */
    private int status;

    /**
     * Тип ошибки или код исключения (например: "WalletNotFound", "ValidationError")
     */
    private String error;

    /**
     * Подробное сообщение об ошибке
     */
    private String message;

    /**
     * Время возникновения ошибки в ISO-8601 формате
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

}