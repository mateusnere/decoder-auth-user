package com.ead.authuser.validations.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.ead.authuser.validations.UsernameConstraint;

public class UsernameConstraintImpl implements ConstraintValidator<UsernameConstraint, String> {

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return username != null && !username.trim().isEmpty() && !username.contains(" ");
    }

}
