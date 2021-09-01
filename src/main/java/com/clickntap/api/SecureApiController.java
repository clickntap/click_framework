package com.clickntap.api;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.clickntap.hub.App;
import com.clickntap.smart.SmartBindingResult;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.f.RhinoScriptEngineFactory;
import com.clickntap.tool.f.Util;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

import freemarker.template.utility.StringUtil;

public class SecureApiController implements Controller {

  private ECPublicKey publicKey;
  private ECPrivateKey privateKey;
  private App app;
  private SecureApi api;
  private String boPackage;
  private String dbPackage;
  private Resource sqlFolder;
  private AdvancedSearch search;
  private FreemarkerScriptEngine engine;
  private CryptoUtils crypto;

  public CryptoUtils getCrypto() {
    return crypto;
  }

  public void setCrypto(CryptoUtils crypto) {
    this.crypto = crypto;
  }

  public void setSqlFolder(Resource sqlFolder) {
    this.sqlFolder = sqlFolder;
  }

  public void setBoPackage(String boPackage) {
    this.boPackage = boPackage;
  }

  public void setDbPackage(String dbPackage) {
    this.dbPackage = dbPackage;
  }

  public void setSearch(AdvancedSearch search) {
    this.search = search;
  }

  public SecureApiController() {
    Security.addProvider(new BouncyCastleProvider());
    try {
      engine = new FreemarkerScriptEngine();
      engine.start();
    } catch (Exception e) {
      new RuntimeException(e);
    }
  }

  public Auth auth(SecureRequest request) {
    Auth auth = new Auth();
    JSONObject client = new JSONObject();
    try {
      BO token = signed(request.getRequest(), privateKey);
      M.invoke(request, "setDeviceToken", token);
      if (token != null) {
        auth.setToken(token);
        if ("api".equalsIgnoreCase(M.invoke(token, "getChannel").toString())) {
          try {
            JSONObject info = new JSONObject(crypto.decrypt(M.invoke(token, "getToken").toString()));
            client.put("authId", info.getNumber("id"));
            client.put("authUser", info);
          } catch (Exception e) {
            client.put("error", "expired_token");
          }
        } else {
          List<BO> users;
          try {
            users = (List) M.invoke(token, "getAuthUsers");
          } catch (Exception e) {
            users = new ArrayList<BO>();
          }
          auth.setUsers(users);
          Number authId = null;
          JSONObject authUser = null;
          if (users.size() != 0) {
            authId = (Number) M.invoke(users.get(0), "getUserId");
            authUser = ((BO) M.invoke(users.get(0), "getUser")).json(true);
            client.put("authId", authId);
            client.put("authUser", authUser);
          }
        }
      }
    } catch (Exception e) {
    }
    auth.setInfo(client);
    return auth;
  }

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      if (api != null) {
        if (api.handleRequest(request, response)) {
          return null;
        }
      }
      JSONObject json = new JSONObject();
      SecureRequest secureRequest = new SecureRequest(request);
      if (secureRequest.getPath().size() > 0) {
        if ("handshake".equalsIgnoreCase(secureRequest.path(0))) {
          handshake(json, secureRequest);
          out(response, json);
        } else if ("signout".equalsIgnoreCase(secureRequest.path(0))) {
          BO token = signed(request, privateKey);
          if (token != null) {
            return signout(request, response, json, secureRequest, token);
          }
        } else {
          Auth auth = auth(secureRequest);
          if (auth.getInfo() != null && auth.getInfo().has("error")) {
            out(response, auth.getInfo());
            return null;
          }
          if (auth.getToken() != null) {
            if (api != null) {
              if (api.handleRequest(request, response, secureRequest)) {
                return null;
              }
            }
            Number authId;
            JSONObject authUser;
            try {
              authId = auth.getInfo().getNumber("authId");
              authUser = auth.getInfo().getJSONObject("authUser");
            } catch (Exception e) {
              authId = null;
              authUser = null;
            }
            if (secureRequest.path(0).equalsIgnoreCase("f")) {
              json = fapi(sqlFolder, request, engine, search, secureRequest.getPath(), authUser);
              if (api != null) {
                api.preOut(json, request, response, secureRequest);
              }
              out(request, response, json);
              return null;
            }
            if (secureRequest.getPath().size() > 2) {
              if ("add".equalsIgnoreCase(secureRequest.path(2))) {
                return add(request, response, json, secureRequest);
              }
              if ("edit".equalsIgnoreCase(secureRequest.path(2))) {
                return edit(request, response, json, secureRequest);
              }
              if ("change-password".equalsIgnoreCase(secureRequest.path(2))) {
                return changePassword(request, response, json, secureRequest);
              }
              if ("forgot-password".equalsIgnoreCase(secureRequest.path(2))) {
                return forgotPassword(request, response, json, secureRequest);
              }
              if ("undo".equalsIgnoreCase(secureRequest.path(2))) {
                return undo(request, response, secureRequest, authId);
              }
              if ("confirm".equalsIgnoreCase(secureRequest.path(2))) {
                return confirm(request, response, secureRequest, authId);
              }
              if ("delete".equalsIgnoreCase(secureRequest.path(2))) {
                return delete(request, response, json, secureRequest);
              }
              if ("signin".equalsIgnoreCase(secureRequest.path(2))) {
                return signin(request, response, json, secureRequest, auth.getToken());
              }
              if ("search".equalsIgnoreCase(secureRequest.path(2))) {
                return search(request, response, json, secureRequest, auth.getToken());
              }
              Long id;
              if ((id = num(secureRequest.path(2))) != 0) {
                return get(request, response, secureRequest, id);
              }
            }
            out(request, response, json);
          } else {
            JSONObject error = new JSONObject();
            error.put("description", "no token");
            err(error, response);
          }
        }
      } else {
        JSONObject error = new JSONObject();
        error.put("description", "useless request");
        err(error, response);
      }
    } catch (Exception e) {
      JSONObject error = new JSONObject();
      error.put("description", "generic error");
      error.put("exception", e.toString());
      err(error, response);
    }
    return null;
  }

  public JSONObject fapi(String channel) throws Exception {
    return fapi(new SmartContext(), "/api/f/" + channel, "api");
  }

  public JSONObject fapi(SmartContext ctx, String channel) throws Exception {
    return fapi(ctx, "/api/f/" + channel, "api");
  }

  public JSONObject fapi(SmartContext ctx, String uri, String folder) throws Exception {
    return fapi(sqlFolder.getFile(), ctx, engine, search, ApiUtils.path(uri, folder), (Number) null);
  }

  public JSONObject fapi(SmartContext ctx, String uri, String folder, Number authId) throws Exception {
    return fapi(sqlFolder.getFile(), ctx, engine, search, ApiUtils.path(uri, folder), authId);
  }

  public JSONObject fapi(SmartContext ctx, String uri, String folder, JSONObject authUser) throws Exception {
    return fapi(sqlFolder.getFile(), ctx, engine, search, ApiUtils.path(uri, folder), authUser);
  }

  public JSONObject fapi(SmartContext ctx, JSONObject sqlJson, Number authId) throws Exception {
    if (authId != null) {
      JSONObject authUser = new JSONObject();
      authUser.put("id", authId);
      ctx.put("authUser", authUser);
      ctx.put("authId", authUser.get("id"));
    }
    return fapiRunner(sqlFolder.getFile(), ctx, engine, search, null, sqlJson);
  }

  public static JSONObject fapi(File sqlFolder, SmartContext ctx, ScriptEngine engine, AdvancedSearch search, List<String> path, JSONObject authUser) throws Exception {
    String smartQuery = path.get(1);
    ctx.put("path", path);
    if (authUser != null) {
      ctx.put("authUser", authUser);
      ctx.put("authId", authUser.get("id"));
    }
    JSONObject sqlJson = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(sqlFile(sqlFolder, smartQuery), ConstUtils.UTF_8)));
    return fapiRunner(sqlFolder, ctx, engine, search, path, sqlJson);
  }

  public static JSONObject fapi(File sqlFolder, SmartContext ctx, ScriptEngine engine, AdvancedSearch search, List<String> path, Number authId) throws Exception {
    if (authId != null) {
      JSONObject authUser = new JSONObject();
      authUser.put("id", authId);
      return fapi(sqlFolder, ctx, engine, search, path, authUser);
    } else {
      return fapi(sqlFolder, ctx, engine, search, path, (JSONObject) null);
    }
  }

  public static JSONObject fapiRunner(File sqlFolder, SmartContext ctx, ScriptEngine engine, AdvancedSearch search, List<String> path, JSONObject sqlJson) throws Exception {
    String smartQuery;
    try {
      smartQuery = path.get(1);
    } catch (Exception e) {
      smartQuery = null;
    }
    JSONObject json = new JSONObject();
    boolean isID = false;
    if (sqlJson.has("filters")) {
      JSONArray filters = sqlJson.getJSONArray("filters");
      for (int i = 0; i < filters.length(); i++) {
        JSONObject filter = filters.getJSONObject(i);
        if (filter.getString("name").equalsIgnoreCase("id")) {
          isID = true;
        }
      }
    }
    if (isID) {
      JSONObject item;
      try {
        item = search.run(sqlJson, ctx, json, false).get(0);
      } catch (Exception e) {
        return new JSONObject();
      }
      json.put("item", item);
      if (sqlJson.has("includes")) {
        ctx.put("item", item);
        JSONArray includes = sqlJson.getJSONArray("includes");
        for (int i = 0; i < includes.length(); i++) {
          JSONObject include = includes.getJSONObject(i);
          sqlJson = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(sqlFile(sqlFolder, include.getString("file")), ConstUtils.UTF_8)));
          List<JSONObject> otherItems = null;
          if (sqlJson.has("sql")) {
            otherItems = search.sql(sqlJson.getString("sql"), ctx, json);
          } else {
            otherItems = search.run(sqlJson, ctx, json, i == 0);
          }
          json.put(include.getString("name"), otherItems);
        }
      }
    } else {
      List<JSONObject> items = null;
      if (sqlJson.has("sql")) {
        items = search.sql(sqlJson.getString("sql"), ctx, json);
      } else {
        items = search.run(sqlJson, ctx, json, true);
      }
      for (JSONObject item : items) {
        if (sqlJson.has("includes")) {
          ctx.put("item", item);
          JSONArray includes = sqlJson.getJSONArray("includes");
          for (int i = 0; i < includes.length(); i++) {
            JSONObject include = includes.getJSONObject(i);
            JSONObject includeJson = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(sqlFile(sqlFolder, include.getString("file")), ConstUtils.UTF_8)));
            List<JSONObject> otherItems = search.run(includeJson, ctx, item, false);
            item.put(include.getString("name"), otherItems);
          }
        }
      }
      if (!sqlJson.has("sql")) {
        json.put("count", search.count(sqlJson, ctx, json));
      }
      json.put("items", items);
      json.put("size", items.size());
    }
    if (smartQuery != null) {
      File js = jsFile(sqlFolder, smartQuery);
      if (js.exists()) {
        ScriptEngineManager manager = new ScriptEngineManager();
        manager.registerEngineName("rhino", new RhinoScriptEngineFactory());
        javax.script.ScriptEngine javascriptEngine = manager.getEngineByName("rhino");
        javascriptEngine.put("app", ctx.getBean("app"));
        javascriptEngine.put("crypto", ctx.getBean("crypto"));
        javascriptEngine.put("util", new Util(null, (CryptoUtils) ctx.getBean("crypto"), null));
        javascriptEngine.put("extension", ctx.getBean("utilExtension"));
        javascriptEngine.put("sql", sqlJson);
        javascriptEngine.put("json", json);
        javascriptEngine.put("request", ctx.getRequest());
        try {
          json = new JSONObject((javascriptEngine.eval(FileUtils.readFileToString(js, ConstUtils.UTF_8))).toString());
        } catch (Exception e) {
          System.out.println(FileUtils.readFileToString(js, ConstUtils.UTF_8));
        }
      }
    }
    return json;
  }

  public static JSONObject fapi(Resource sqlFolder, HttpServletRequest request, ScriptEngine engine, AdvancedSearch search, List<String> path, Number authId) throws Exception {
    return fapi(sqlFolder.getFile(), new SmartContext(request, null), engine, search, path, authId);
  }

  public static JSONObject fapi(Resource sqlFolder, HttpServletRequest request, ScriptEngine engine, AdvancedSearch search, List<String> path, JSONObject authUser) throws Exception {
    return fapi(sqlFolder.getFile(), new SmartContext(request, null), engine, search, path, authUser);
  }

  public static File sqlFile(File sqlFolderFile, String smartQuery) throws Exception {
    return new File(sqlFolderFile.getCanonicalPath() + "/" + smartQuery + ".json");
  }

  public static File jsFile(File sqlFolderFile, String smartQuery) throws Exception {
    return new File(sqlFolderFile.getCanonicalPath() + "/" + smartQuery + ".js");
  }

  private ModelAndView get(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest, Number id) throws Exception {
    BOFilter filter = (BOFilter) BeanUtils.getValue(app, secureRequest.path(0));
    Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), "get" + secureRequest.path(1), Number.class);
    BO bo = (BO) method.invoke(filter, id);
    out(request, response, bo.json());
    return null;
  }

  private long num(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private ModelAndView signin(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest, BO token) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[4];
    fields[0] = "id";
    fields[1] = "username";
    fields[2] = "password";
    fields[3] = "email";
    BO operator = null;
    if (api != null && (operator = api.signin(request, secureRequest, token)) != null) {
      sign(response, json, token, operator);
    } else {
      context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", fields, null, "request");
      BO bo = (BO) context.get(secureRequest.path(1));
      bo.read("auth");
      if (bo.getId() != null) {
        bo.read();
        sign(response, json, token, bo);
      } else {
        out(response, new JSONObject());
      }
    }
    return null;
  }

  private void sign(HttpServletResponse response, JSONObject json, BO token, BO bo) throws Exception {
    for (BO user : (List<BO>) M.invoke(token, "getAuthUsers")) {
      user.delete();
    }
    BO authUser = (BO) Class.forName(boPackage + ".security.AuthUser").getDeclaredConstructor().newInstance();
    authUser.setApp(app);
    M.invoke(authUser, "setUserId", bo.getId());
    M.invoke(authUser, "setDeviceId", token.getId());
    authUser.create();
    json.put("signed", true);
    out(response, json);
  }

  private ModelAndView signout(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest, BO token) throws Exception {
    for (BO user : (List<BO>) M.invoke(token, "getAuthUsers")) {
      user.delete();
    }
    out(response, json);
    return null;
  }

  private ModelAndView search(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest, BO token) throws Exception {
    BOFilter filter = (BOFilter) BeanUtils.getValue(app, secureRequest.path(0));
    Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), fixS("search" + secureRequest.path(1) + "s"), String.class);
    List<BO> list = (List<BO>) method.invoke(filter, URLDecoder.decode(secureRequest.path(3), ConstUtils.UTF_8));
    JSONObject result = new JSONObject();
    List<JSONObject> items = new ArrayList<JSONObject>();
    for (BO bo : list) {
      items.add(bo.json(false));
    }
    result.put("size", items.size());
    result.put("items", items);
    if (api != null) {
      api.search(result, secureRequest, token);
    }
    out(response, result);
    return null;
  }

  private ModelAndView confirm(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest, Number authId) throws Exception {
    editEnd(request, response, secureRequest, authId, true);
    return null;
  }

  private ModelAndView undo(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest, Number authId) throws Exception {
    editEnd(request, response, secureRequest, authId, false);
    return null;
  }

  private ModelAndView editEnd(HttpServletRequest request, HttpServletResponse response, SecureRequest secureRequest, Number authId, boolean confirm) throws Exception {
    JSONObject sql = new JSONObject();
    String className = boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1);
    Class clazz = Class.forName(className);
    sql.put("class", className);
    sql.put("table", new StringBuffer().append(dbPackage).append("_").append(secureRequest.path(1).toLowerCase()).toString());
    List<JSONObject> filters = new ArrayList<JSONObject>();
    {
      JSONObject filter = new JSONObject();
      filter.put("name", "operator_id");
      filter.put("operator", "=");
      filter.put("value", authId);
      filters.add(filter);
    }
    {
      JSONObject filter = new JSONObject();
      filter.put("name", "status");
      filter.put("operator", "=");
      filter.put("value", "403");
      filters.add(filter);
    }
    sql.put("filters", filters);
    JSONObject json = fapi(new SmartContext(request, response), sql, authId);
    JSONArray items = json.getJSONArray("items");
    for (int i = 0; i < items.length(); i++) {
      Number id = items.getJSONObject(i).getNumber("id");
      if (confirm) {
        BO bo = ((BO) app.getBO(clazz, id));
        M.invoke(bo, "setStatus", 200);
        bo.update();
      } else {
        ((BO) app.getBO(clazz, id)).delete();
      }
    }
    JSONObject result = new JSONObject();
    out(response, result);
    return null;
  }

  private ModelAndView add(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
    SmartContext context = new SmartContext(request, response);
    SmartBindingResult bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, null);
    BO bo = (BO) context.get(secureRequest.path(1));
    if (api != null) {
      if (api.onPreCreate(context, bo, secureRequest)) {
        out(request, response, new JSONObject());
        return null;
      }
    }
    ValidationUtils.invokeValidator(app.getValidator(bo, "create"), bo, bindingResult.getBindingResult());
    if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
      bo.create();
      if (api != null) {
        api.onCreate(context, bo, secureRequest);
      }
      out(response, bo.json());
    } else {
      out(response, json, bindingResult);
    }
    return null;
  }

  private ModelAndView edit(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[1];
    fields[0] = "id";
    SmartBindingResult bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", fields, null, "request");
    BO bo = (BO) context.get(secureRequest.path(1));
    bo.read();
    if (api != null) {
      if (api.onPreEdit(context, bo, secureRequest)) {
        out(request, response, new JSONObject());
        return null;
      }
    }
    bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, "request");
    ValidationUtils.invokeValidator(app.getValidator(bo, "update"), bo, bindingResult.getBindingResult());
    if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
      bo.update();
      if (api != null) {
        api.onEdit(context, bo, secureRequest);
      }
      Map<String, Object> conf = new HashMap<String, Object>();
      conf.put("appId", context.param("appId"));
      out(request, response, bo.json());
    } else {
      out(request, response, json, bindingResult);
    }
    return null;
  }

  private ModelAndView changePassword(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[4];
    fields[0] = "id";
    fields[1] = "oldPassword";
    fields[2] = "newPassword";
    fields[3] = "confirmNewPassword";
    SmartBindingResult bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", fields, null, "request");
    BO bo = (BO) context.get(secureRequest.path(1));
    bo.read();
    if (api != null) {
      if (api.onPreChangePassword(context, bo, secureRequest)) {
        out(request, response, new JSONObject());
        return null;
      }
    }
    bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, "request");
    ValidationUtils.invokeValidator(app.getValidator(bo, "execute-password"), bo, bindingResult.getBindingResult());
    if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
      bo.execute("password");
      if (api != null) {
        api.onChangePassword(context, bo, secureRequest);
      }
      out(request, response, bo.json());
    } else {
      out(request, response, json, bindingResult);
    }
    return null;
  }

  private ModelAndView forgotPassword(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[1];
    fields[0] = "email";
    context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, "request");
    BO bo = (BO) context.get(secureRequest.path(1));
    bo.read("email");
    bo.read();
    if (api != null) {
      if (api.onPreForgotPassword(context, bo, secureRequest)) {
        out(request, response, new JSONObject());
        return null;
      }
    }
    if (bo.getId() != null) {
      StringBuffer newPassword = new StringBuffer();
      for (int i = 0; i < 3; i++) {
        char d;
        do {
          d = (char) ('A' + (Math.random() * ((int) 'Z' - (int) 'A')));
        } while (d == 'O' || d == 'I');
        int n = (int) (Math.random() * (8)) + 2;
        newPassword.append(d).append(n);
      }
      M.invoke(bo, "setNewPassword", newPassword.toString().toLowerCase());
      bo.execute("forgot-password");
      if (api != null) {
        api.onForgotPassword(context, bo, secureRequest);
      }
      out(request, response, bo.json());
    } else {
      out(request, response, new JSONObject());
    }
    return null;
  }

  private ModelAndView delete(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[1];
    fields[0] = "id";
    context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", fields, null, "request");
    BO bo = (BO) context.get(secureRequest.path(1));
    if (api != null) {
      if (api.onPreDelete(context, bo, secureRequest)) {
        out(request, response, new JSONObject());
        return null;
      }
    }
    json.put("n", bo.delete());
    if (api != null) {
      api.onDelete(context, bo, secureRequest);
    }
    out(response, json);
    return null;
  }

  public static void out(HttpServletResponse response, JSONObject json, SmartBindingResult bindingResult) throws Exception {
    if (bindingResult == null) {
      if (json.has("contentType") && json.has("out")) {
        response.setContentType(json.getString("contentType"));
        if (json.has("headers")) {
          JSONObject headers = json.getJSONObject("headers");
          for (String key : headers.keySet()) {
            response.setHeader(key, headers.getString(key));
          }
        }
        if (json.has("statusCode")) {
          response.setStatus(json.getInt("statusCode"));
        }
        response.getOutputStream().write(json.getString("out").getBytes(ConstUtils.UTF_8));
      } else {
        response.setContentType("application/json; charset=UTF-8");
        response.getOutputStream().write(json.toString().getBytes(ConstUtils.UTF_8));
      }
    } else {
      List<JSONObject> errors = new ArrayList<JSONObject>();
      for (FieldError fieldError : bindingResult.getBindingResult().getFieldErrors()) {
        JSONObject item = new JSONObject();
        String error = "error." + fieldError.getCode() + "." + fieldError.getObjectName().toLowerCase() + "." + fieldError.getField();
        item.put("name", fieldError.getField());
        item.put("error", error);
        errors.add(item);
      }
      json.put("errors", errors);
      out(response, json);
    }
  }

  public synchronized BO signed(HttpServletRequest request, ECPrivateKey privateKey) throws Exception {
    BO token = (BO) Class.forName(boPackage + ".security.DeviceToken").getDeclaredConstructor().newInstance();
    token.setApp(app);
    M.invoke(token, "setToken", request.getHeader("token"));
    token.read("token");
    if (token.getId() != null) {
      token.read();
      if ("api".equalsIgnoreCase(M.invoke(token, "getChannel").toString())) {
        Datetime creationDate = token.getCreationDate();
        Datetime now = new Datetime();
        int minutes = (int) ((now.getTimeInMillis() - creationDate.getTimeInMillis()) / (1000 * 60));
        if (minutes > 30) {
          M.invoke(token, "setToken", "");
        }
        return token;
      } else {
        ECPublicKey publicKey = SecureUtils.importPublicKey(M.invoke(token, "getPublicKey").toString());
        byte[] encoded = SecureUtils.base64dec(request.getHeader("sign"));
        JSONObject sign = new JSONObject(SecureUtils.decrypt(SecureUtils.generateSecret(publicKey, privateKey), encoded));
        // if (sign.getLong("t") > Long.parseLong(M.invoke(token,
        // "getLastRequestTime").toString())) {
        if (M.invoke(token, "getToken").toString().equals(sign.get("token"))) {
          M.invoke(token, "setLastRequestTime", sign.getLong("t"));
          token.update();
          return token;
        }
        // }
      }
    }
    return null;
  }

  private void handshake(JSONObject json, SecureRequest secureRequest) throws Exception {
    long now = System.currentTimeMillis();
    BO token = (BO) Class.forName(boPackage + ".security.DeviceToken").getDeclaredConstructor().newInstance();
    token.setApp(app);
    M.invoke(token, "setToken", secureRequest.getD());
    token.read("token");
    if (token.getId() != null) {
      token.read();
      String channel = M.invoke(token, "getChannel").toString();
      if (!channel.equalsIgnoreCase(secureRequest.getC())) {
        token.setId(null);
      }
    }
    if (token.getId() == null) {
      M.invoke(token, "setPublicKey", SecurityUtils.base64enc(secureRequest.getPublicKey().getEncoded()));
      M.invoke(token, "setToken", SecurityUtils.base64enc(SecurityUtils.generateKey(512).getEncoded()));
      M.invoke(token, "setChannel", secureRequest.getC());
      M.invoke(token, "setLastRequestTime", secureRequest.getT());
      token.create();
    } else {
      M.invoke(token, "setPublicKey", SecurityUtils.base64enc(secureRequest.getPublicKey().getEncoded()));
      M.invoke(token, "setLastRequestTime", secureRequest.getT());
      token.update();
    }
    if (((List) M.invoke(token, "getAuthUsers")).size() > 0) {
      json.put("u", true);
    }
    json.put("d", M.invoke(token, "getToken").toString());
    json.put("k", SecureUtils.base64enc(publicKey.getEncoded()));
    json.put("t", now);
  }

  public static void out(HttpServletResponse response, JSONObject json) throws Exception {
    out(response, json, null);
  }

  private void err(JSONObject error, HttpServletResponse response) throws Exception {
    response.setHeader("error", error.toString());
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

  public void setPublicKeyBase64(String base64) throws Exception {
    this.publicKey = SecureUtils.importPublicKey(base64);
  }

  public void setPrivateKeyBase64(String base64) throws Exception {
    this.privateKey = SecureUtils.importPrivateKey(base64);
  }

  public void setApp(App app) throws Exception {
    this.app = app;
  }

  public void setApi(SecureApi api) {
    this.api = api;
  }

  private String fixS(String name) {
    for (int i = 'a'; i <= 'z'; i++) {
      if (i == 'a' || i == 'e' || i == 'i' || i == 'o' || i == 'u') {
        continue;
      }
      name = StringUtil.replace(name, ((char) i) + "ys(", ((char) i) + "ies(");
      name = StringUtil.replace(name, ((char) i) + "ys\"", ((char) i) + "ies\"");
    }
    return name;
  }

  public void out(HttpServletRequest request, HttpServletResponse response, JSONObject json) throws Exception {
    if (api != null) {
      api.onResponse(request, response, json);
    }
    out(response, json, null);
  }

  public void out(HttpServletRequest request, HttpServletResponse response, JSONObject json, SmartBindingResult bindingResult) throws Exception {
    if (api != null) {
      bindingResult = api.onResponse(request, response, json, bindingResult);
    }
    out(response, json, bindingResult);
  }

}
