package com.wallet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация валидации для проверки значений типа {@link java.util.UUID}.
 *
 * @see UuidValidator
 * @see jakarta.validation.Validator
 * @see java.util.UUID
 */
@Constraint(validatedBy = UuidValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUUID {

    /**
     * Сообщение об ошибке, возвращаемое при провале валидации.
     *
     * @return шаблон сообщения об ошибке
     */
    String message() default "Invalid UUID format";

    /**
     * Группы валидации, к которым применяется данная аннотация.
     *
     * @return массив классов-групп
     * @see jakarta.validation.groups.Default
     */
    Class<?>[] groups() default {};

    /**
     * Метаданные для клиентов валидации (например, для генерации документации).
     * <br>
     * Может использоваться фреймворками для передачи дополнительной
     * информации о правиле валидации.
     *
     * @return массив классов-пейлоадов
     * @see Payload
     */
    Class<? extends Payload>[] payload() default {};

}
