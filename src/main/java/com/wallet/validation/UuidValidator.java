package com.wallet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

/**
 * Валидатор для аннотации {@link ValidUUID}.
 * <br>
 * <b>Важно:</b> При использовании с {@code @PathVariable} в Spring MVC
 * этот валидатор срабатывает только после успешной конвертации строки в UUID.
 * Если строка не соответствует формату UUID, Spring выбрасывает
 * {@link org.springframework.web.method.annotation.MethodArgumentTypeMismatchException}
 * до вызова данного валидатора.
 * <br><br>
 * <b>Когда использовать:</b>
 * <ul>
 *   <li>Для явной проверки на {@code null} в бизнес-логике</li>
 *   <li>В сочетании с {@code @NotNull} для более понятных сообщений об ошибках</li>
 *   <li>Для валидации UUID в {@code @RequestBody} или {@code @RequestParam},
 *       где конвертация может не произойти автоматически</li>
 * </ul>
 *
 * @see ValidUUID
 * @see ConstraintValidator
 */
public class UuidValidator implements ConstraintValidator<ValidUUID, UUID> {

    /**
     * Проверяет валидность UUID.
     * <br>
     * Метод не проверяет формат UUID — это делает конвертер Spring.
     * Здесь проверяется только наличие значения.
     *
     * @param value   проверяемое значение UUID
     * @param context контекст валидации (не используется в данной реализации)
     * @return {@code true} если UUID не {@code null}, иначе {@code false}
     */
    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return value != null;
    }

}
