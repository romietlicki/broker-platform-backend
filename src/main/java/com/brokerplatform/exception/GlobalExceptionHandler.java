package com.brokerplatform.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("BusinessException: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setTitle("Erro de Negócio");
        pd.setType(URI.create("https://brokerplatform.com/errors/business"));
        return pd;
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        pd.setTitle("Autenticação Falhou");
        pd.setType(URI.create("https://brokerplatform.com/errors/auth"));
        return pd;
    }

    @ExceptionHandler(LockedException.class)
    ProblemDetail handleLocked(LockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.LOCKED, "Conta bloqueada por excesso de tentativas");
        pd.setTitle("Conta Bloqueada");
        pd.setType(URI.create("https://brokerplatform.com/errors/locked"));
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acesso negado");
        pd.setTitle("Sem Permissão");
        pd.setType(URI.create("https://brokerplatform.com/errors/forbidden"));
        return pd;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, detail);
        pd.setTitle("Dados Inválidos");
        pd.setType(URI.create("https://brokerplatform.com/errors/validation"));

        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> pd.setProperty(fe.getField(), fe.getDefaultMessage()));

        return ResponseEntity.unprocessableEntity().body(pd);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno. Tente novamente.");
        pd.setTitle("Erro Interno");
        pd.setType(URI.create("https://brokerplatform.com/errors/internal"));
        return pd;
    }
}
