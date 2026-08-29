package school.grevcev.reservation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull обрабатывает отсутствие значения отдельно
        }

        try {
            // Приводим к сырому типу Class<Enum>, чтобы компилятор пропустил вызов
            Enum.valueOf((Class<Enum>) enumClass, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
