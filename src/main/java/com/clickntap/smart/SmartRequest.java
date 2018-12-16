package com.clickntap.smart;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class SmartRequest {
	private Map<String, Object> parameters = null;
	private String ref;

	public SmartRequest(String ref, HttpServletRequest request) {
		this.ref = ref;
		parameters = new HashMap<String, Object>();
		parameters.putAll(request.getParameterMap());
	}

	public String getRef() {
		return ref;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}
}
