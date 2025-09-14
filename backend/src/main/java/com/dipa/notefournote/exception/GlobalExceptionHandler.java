package com.dipa.notefournote.exception;

import com.dipa.notefournote.common.dto.ErrorResponse;
import com.dipa.notefournote.common.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed for a user");
        return new ErrorResponse("Credenziali non valide");
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException ex) {
        log.warn("Authentication failed due to invalid token");
        return new ErrorResponse("Il token fornito non è valido o è scaduto");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        final Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toUnmodifiableMap(
                        FieldError::getField,
                        fieldError -> String.valueOf(fieldError.getDefaultMessage()),
                        (existingMessage, newMessage) -> existingMessage + "; " + newMessage
                ));
        log.warn("Validation failure: {}", errors);
        return new ValidationErrorResponse("Errore di validazione", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestBody(HttpMessageNotReadableException ex) {
        log.warn("Request body is missing or unreadable: {}", ex.getMessage());
        return new ErrorResponse("Il corpo della richiesta è mancante o formattato in modo non corretto");
    }

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidSearch(InvalidSearchCriteriaException ex) {
        log.warn("Query params (or form) are missing: {}", ex.getMessage());
        return new ErrorResponse("Nessun parametro di ricerca fornito");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ErrorResponse("Si è verificato un errore interno inaspettato");
    }

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoteNotFound(NoteNotFoundException ex) {
        log.warn("Note not found: {}", ex.getMessage());
        return new ErrorResponse("Nota non trovata");
    }

    @ExceptionHandler(NoteAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNoteAccessDenied(NoteAccessDeniedException ex) {
        log.warn("Note access denied: {}", ex.getMessage());
        return new ErrorResponse("Accesso non autorizzato");
    }

}