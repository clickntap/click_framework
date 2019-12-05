package com.clickntap.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public interface SecureApi {

	boolean handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

	boolean handleRequest(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest) throws Exception;

	BO signin(HttpServletRequest request, SecureRequest secureRequest, BO token) throws Exception;

	void search(JSONObject result, SecureRequest secureRequest, BO token) throws Exception;

}
