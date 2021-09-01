package com.clickntap.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clickntap.smart.SmartBindingResult;
import com.clickntap.smart.SmartContext;

public interface SecureApi {

  boolean handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

  boolean handleRequest(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest) throws Exception;

  BO signin(HttpServletRequest request, SecureRequest secureRequest, BO token) throws Exception;

  void search(JSONObject result, SecureRequest secureRequest, BO token) throws Exception;

  boolean onPreCreate(SmartContext context, BO bo, SecureRequest secureRequest);

  void onCreate(SmartContext context, BO bo, SecureRequest secureRequest);

  boolean onPreEdit(SmartContext context, BO bo, SecureRequest secureRequest);

  void onEdit(SmartContext context, BO bo, SecureRequest secureRequest);

  boolean onPreChangePassword(SmartContext context, BO bo, SecureRequest secureRequest);

  void onChangePassword(SmartContext context, BO bo, SecureRequest secureRequest);

  boolean onPreForgotPassword(SmartContext context, BO bo, SecureRequest secureRequest);

  void onForgotPassword(SmartContext context, BO bo, SecureRequest secureRequest);

  boolean onPreDelete(SmartContext context, BO bo, SecureRequest secureRequest);

  void onDelete(SmartContext context, BO bo, SecureRequest secureRequest);

  void onResponse(HttpServletRequest request, HttpServletResponse response, JSONObject json);

  SmartBindingResult onResponse(HttpServletRequest request, HttpServletResponse response, JSONObject json, SmartBindingResult bindingResult);

  void preOut(JSONObject json, HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest);

}
