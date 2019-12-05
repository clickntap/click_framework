package com.clickntap.smart;

import org.springframework.validation.BindingResult;

public class SmartBindingResult {
	private BindingResult bindingResult;
	private boolean isNew;

	public SmartBindingResult(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
		this.isNew = false;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}

	public void setBindingResult(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
}
