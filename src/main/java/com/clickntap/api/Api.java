package com.clickntap.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clickntap.hub.App;
import com.clickntap.smart.SmartContext;

public interface Api {

  boolean handleRequest(HttpServletRequest request, HttpServletResponse response);

  void onPreAdd(BO bo, Map<String, Object> conf) throws Exception;

  void onAdd(BO bo, Map<String, Object> conf) throws Exception;

  void onEdit(BO bo, Map<String, Object> conf) throws Exception;

  void onAuth(BO bo, Map<String, Object> conf) throws Exception;

  List<BO> onSearch(Class<? extends BOFilter> c, List<BO> list) throws Exception;

  void onRead(BO bo, JSONObject json) throws Exception;

  JSONObject onNull(Class<?> clazz, SmartContext context) throws Exception;

  JSONObject api(String uri, SmartContext ctx) throws Exception;

  public BO get(Class<? extends BO> clazz, String value);

  public String getTokenCookie(App app, HttpServletResponse response, String token, boolean secure) throws Exception;

  public String addTokenCookie(App app, HttpServletResponse response, boolean secure) throws Exception;

}
