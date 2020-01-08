package com.clickntap.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clickntap.smart.SmartContext;

public interface SecureApi {

	boolean handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

	boolean handleRequest(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest) throws Exception;

	BO signin(HttpServletRequest request, SecureRequest secureRequest, BO token) throws Exception;

	void search(JSONObject result, SecureRequest secureRequest, BO token) throws Exception;

	void onPreCreate(SmartContext context, BO bo, SecureRequest secureRequest);

	void onCreate(SmartContext context, BO bo, SecureRequest secureRequest);

	void onPreEdit(SmartContext context, BO bo, SecureRequest secureRequest);

	void onEdit(SmartContext context, BO bo, SecureRequest secureRequest);

	void onPreDelete(SmartContext context, BO bo, SecureRequest secureRequest);

	void onDelete(SmartContext context, BO bo, SecureRequest secureRequest);

}
