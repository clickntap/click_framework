package com.clickntap.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import com.clickntap.smart.SmartContext;

public interface Api {

  void handleRequest(HttpServletRequest request, HttpServletResponse response);

  void onAdd(BO bo, Map<String, Object> conf) throws Exception;

  void onEdit(BO bo, Map<String, Object> conf) throws Exception;

  void onAuth(BO bo, Map<String, Object> conf) throws Exception;

  void onSearch(Class<? extends BOFilter> c, JSONObject result) throws Exception;

  void onRead(BO bo, JSONObject json) throws Exception;

  JSONObject onNull(Class<?> clazz, SmartContext context) throws Exception;

  JSONObject api(String uri, SmartContext ctx) throws Exception;

}
