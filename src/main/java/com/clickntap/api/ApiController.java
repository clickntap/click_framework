package com.clickntap.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

import freemarker.template.utility.StringUtil;

public class ApiController implements Controller {

  private App app;
  private Resource rootDir;
  private Api api;

  public Resource getRootDir() {
    return rootDir;
  }

  public void setRootDir(Resource rootDir) {
    this.rootDir = rootDir;
  }

  public void setApp(App app) {
    this.app = app;
  }

  public Api getApi() {
    return api;
  }

  public void setApi(Api api) {
    this.api = api;
  }

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String uri = request.getRequestURI();
    if (api != null) {
      api.handleRequest(request, response);
    }
    if (uri.indexOf("/api/") > 0) {
      uri = uri.substring(uri.indexOf("/api/"));
    }
    if (uri.indexOf("/api/") == 0) {
      String groupName = null;
      String entityName = null;
      String action = null;
      int n = 0;
      String[] names = StringUtil.split(uri.substring(5), '/');
      for (String name : names) {
        if (!name.isEmpty()) {
          if (n == 0) {
            groupName = name;
          }
          if (n == 1) {
            entityName = name;
          }
          if (n == 2) {
            action = name;
          }
          n++;
        }
      }
      SmartContext context = new SmartContext(request, response);
      if (groupName != null && entityName != null && action != null) {
        BOFilter filter = (BOFilter) BeanUtils.getValue(app, groupName);
        if (action.equals("search")) {
          Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), fixS("search" + entityName + "s"), String.class);
          List<BO> list = (List<BO>) method.invoke(filter, URLDecoder.decode(names[3], ConstUtils.UTF_8));
          JSONObject result = new JSONObject();
          List<JSONObject> items = new ArrayList<JSONObject>();
          for (BO bo : list) {
            items.add(bo.json(false));
          }
          result.put("size", items.size());
          result.put("items", items);
          if (api != null) {
            api.onSearch(filter.getClass(), result);
          }
          out(response, result);
        } else if (action.equals("add")) {
          SmartBindingResult bindingResult = context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, null);
          BO bo = (BO) context.get(entityName);
          ValidationUtils.invokeValidator(app.getValidator(bo, "create"), bo, bindingResult.getBindingResult());
          if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
            bo.create();
            Map<String, Object> conf = new HashMap<String, Object>();
            conf.put("appId", context.param("appId"));
            if (api != null) {
              api.onAdd(bo, conf);
            }
            out(response, bo);
          } else {
            outErrors(response, context, bindingResult);
          }
        } else if (action.equals("edit")) {
          String[] fields = new String[1];
          fields[0] = "id";
          SmartBindingResult bindingResult = context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
          BO bo = (BO) context.get(entityName);
          bo.read();
          bindingResult = context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, "request");
          ValidationUtils.invokeValidator(app.getValidator(bo, "update"), bo, bindingResult.getBindingResult());
          if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
            bo.update();
            Map<String, Object> conf = new HashMap<String, Object>();
            conf.put("appId", context.param("appId"));
            if (api != null) {
              api.onEdit(bo, conf);
            }
            out(response, bo);
          } else {
            outErrors(response, context, bindingResult);
          }
        } else if (action.equals("weight")) {
          BO bo = getBo(request, response, groupName, entityName);
          bo.read();
          JSONObject json = new JSONObject();
          json.put(action, bo.deleteWeight());
          out(response, json);
        } else if (action.equals("delete")) {
          BO bo = getBo(request, response, groupName, entityName);
          JSONObject json = new JSONObject();
          json.put("n", bo.delete());
          out(response, json);
        } else if (action.equals("read")) {
          BO bo = getBo(request, response, groupName, entityName);
          bo.read();
          JSONObject json = bo.json();
          if (api != null) {
            api.onRead(bo, json);
          }
          out(response, json);
        } else if (action.equals("auth")) {
          BO bo = getBo(request, response, groupName, entityName);
          bo.read("auth");
          if (bo.getId() != null) {
            bo.read();
            Map<String, Object> conf = new HashMap<String, Object>();
            conf.put("appId", context.param("appId"));
            if (api != null) {
              api.onAuth(bo, conf);
            }
          }
          out(response, bo);
        } else if (action.equals("changePassword")) {
          String[] fields = new String[4];
          fields[0] = "id";
          fields[1] = "oldPassword";
          fields[2] = "newPassword";
          fields[3] = "confirmNewPassword";
          SmartBindingResult bindingResult = context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
          BO bo = (BO) context.get(entityName);
          bindingResult = context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, "request");
          ValidationUtils.invokeValidator(app.getValidator(bo, "execute-password"), bo, bindingResult.getBindingResult());
          if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
            bo.execute("password");
            out(response, bo);
          } else {
            outErrors(response, context, bindingResult);
          }
        } else if (action.equals("forgotPassword")) {
          BO bo = getBo(request, response, groupName, entityName);
          bo.read("email");
          if (bo.getId() != null) {
            bo.read();
            JSONObject json = bo.json();
            json.put("forgotPassword", true);
            if (api != null) {
              api.onRead(bo, json);
            }
          }
          out(response, bo);
        } else {
          Long actionId = 0L;
          try {
            actionId = Long.parseLong(action);
          } catch (Exception e) {
            actionId = 0L;
          }
          if (actionId != 0L) {
            Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), "get" + entityName, Number.class);
            BO bo = (BO) method.invoke(filter, actionId);
            if (bo != null) {
              JSONObject json = bo.json();
              if (api != null) {
                api.onRead(bo, json);
              }
              out(response, json);
            } else {
              if (api != null) {
                String className = api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName;
                out(response, api.onNull(Class.forName(className), context));
              }
            }
          } else {
            if (api != null) {
              out(response, api.api(uri, context));
            }
          }
        }
      } else {
        if (api != null) {
          out(response, api.api(uri, context));
        }
      }
    }
    if (uri.indexOf("/click/") > 0) {
      uri = uri.substring(uri.indexOf("/click/"));
    }
    if (uri.indexOf("/click/") == 0) {
      if (uri.startsWith("/click/ui/")) {
        File f = new File(getRootDir().getFile().getCanonicalPath() + "/" + uri.substring(7));
        if (f.getName().endsWith(".css")) {
          response.setContentType("text/css; charset=UTF-8");
        }
        if (f.getName().endsWith(".svg")) {
          response.setContentType("image/svg+xml; charset=UTF-8");
        }
        if (f.getName().endsWith(".png")) {
          response.setContentType("image/png");
        }
        if (f.getName().endsWith(".js")) {
          response.setContentType("text/javascript; charset=UTF-8");
        }
        if (f.getName().endsWith(".pdf")) {
          response.setContentType("application/pdf; charset=UTF-8");
        }
        if (f.exists()) {
          FileInputStream in = new FileInputStream(f);
          IOUtils.copy(in, response.getOutputStream());
          in.close();
        }
        return null;
      }
    }
    return null;
  }

  private BO getBo(HttpServletRequest request, HttpServletResponse response, String groupName, String entityName) throws Exception {
    SmartContext context = new SmartContext(request, response);
    String[] fields = new String[4];
    fields[0] = "id";
    fields[1] = "username";
    fields[2] = "password";
    fields[3] = "email";
    context.bind(entityName, api.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
    BO bo = (BO) context.get(entityName);
    return bo;
  }

  private void outErrors(HttpServletResponse response, SmartContext context, SmartBindingResult bindingResult) throws IOException, UnsupportedEncodingException {
    JSONObject json = new JSONObject();
    List<JSONObject> errors = new ArrayList<JSONObject>();
    for (FieldError fieldError : bindingResult.getBindingResult().getFieldErrors()) {
      JSONObject item = new JSONObject();
      String error = context.tr("error." + fieldError.getCode() + "." + fieldError.getObjectName().toLowerCase() + "." + fieldError.getField());
      item.put("name", fieldError.getField());
      item.put("error", error);
      errors.add(item);
    }
    json.put("errors", errors);
    out(response, json);
  }

  private void out(HttpServletResponse response, BO bo) throws IOException, UnsupportedEncodingException {
    out(response, bo.json());
  }

  private void out(HttpServletResponse response, JSONObject json) throws IOException, UnsupportedEncodingException {
    response.setContentType("application/json; charset=UTF-8");
    response.getOutputStream().write(json.toString(2).getBytes(ConstUtils.UTF_8));
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

}
