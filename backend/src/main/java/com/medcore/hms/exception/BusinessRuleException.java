package com.medcore.hms.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super("BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY.value(), message);
    }

    public BusinessRuleException(String message, Map<String, Object> details) {
        super("BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY.value(), message, details);
    }
}