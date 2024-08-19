package com.tourism.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SortDirectionValidator.class)
@Documented
public @interface ValidSortDirection {
    String message() default "Sort type must be either ASC or DESC";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

