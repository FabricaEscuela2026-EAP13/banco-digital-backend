package co.edu.udea.bancodigital.dtos.validation;

import co.edu.udea.bancodigital.dtos.requests.GenerarReporteRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.temporal.ChronoUnit;

public class ValidRangoFechasValidator implements ConstraintValidator<ValidRangoFechas, GenerarReporteRequest> {

    private static final long MAX_DIAS = 90;

    @Override
    public void initialize(ValidRangoFechas constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(GenerarReporteRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getFechaInicio() == null || value.getFechaFin() == null) {
            return true;
        }

        if (value.getFechaInicio().isAfter(value.getFechaFin())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("La fecha de inicio no puede ser posterior a la fecha de fin")
                    .addConstraintViolation();
            return false;
        }

        long dias = ChronoUnit.DAYS.between(value.getFechaInicio(), value.getFechaFin());
        return dias <= MAX_DIAS;
    }
}
