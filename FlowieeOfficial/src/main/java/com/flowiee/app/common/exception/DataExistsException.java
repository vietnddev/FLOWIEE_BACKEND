package com.flowiee.app.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DataExistsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
}