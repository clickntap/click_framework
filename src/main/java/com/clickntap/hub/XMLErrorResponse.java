package com.clickntap.hub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

public class XMLErrorResponse extends BO {
	public Throwable exception;
	public String stackTrace;
	private List<String> errorCodes;

	public List<String> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(List<String> errorCodes) {
		this.errorCodes = errorCodes;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable throwable) {
		this.exception = throwable;
		if (throwable instanceof BindException) {
			BindException e = (BindException) throwable;
			List<String> errorCodes = new ArrayList<String>();
			for (FieldError error : (List<FieldError>) e.getFieldErrors()) {
				errorCodes.add(error.getField() + "." + error.getCode());
			}
			setErrorCodes(errorCodes);
		}
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

}
