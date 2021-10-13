package org.sguesdon.api.jsonvalidator.models.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.sguesdon.api.jsonvalidator.models.exception.InvalidSchemaException;
import org.sguesdon.api.jsonvalidator.models.exception.NotFoundException;
import org.sguesdon.api.jsonvalidator.openapi.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Error> handleNotFoundException(Exception exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                Error.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(exception.getMessage())
                    .trace("")
                    .build()
            );
    }

    @ExceptionHandler(value = {InvalidSchemaException.class})
    public ResponseEntity<Error> handleInvalidSchemaException(Exception exception) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                    Error.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .trace("")
                        .build()
            );
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleOtherException(Exception exception) {
        log.error(exception.toString());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                    Error.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .trace(ExceptionUtils.getStackTrace(exception))
                        .build()
            );
    }

}
