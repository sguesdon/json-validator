package org.sguesdon.api.jsonvalidator.controller;

import org.sguesdon.api.jsonvalidator.exception.NotFoundException;

public interface BaseController {
    default NotFoundException notFoundException(String id) {
        return new NotFoundException(String.format("model with id %s doesn't exist", id));
    }
}
