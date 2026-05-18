package com.brokerplatform.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s não encontrado com id: %s", resource, id), HttpStatus.NOT_FOUND);
    }
}
