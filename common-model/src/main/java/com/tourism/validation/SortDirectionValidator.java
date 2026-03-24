package com.tourism.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.data.domain.Sort;

public class SortDirectionValidator implements ConstraintValidator<ValidSortDirection, Sort.Direction> {
    @Override
    public boolean isValid(Sort.Direction value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value == Sort.Direction.ASC || value == Sort.Direction.DESC;
    }
}
