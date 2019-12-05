package com.clickntap.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
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
import com.clickntap.utils.AsciiUtils;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

import freemarker.template.utility.StringUtil;

public class ApiController implements Controller {

	private Set<String> localAddresses = new HashSet<String>();

	private App app;
	private Resource rootDir;
	private Api api;

	private String apiKey;
	private Boolean apiSecure;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Boolean getApiSecure() {
		return apiSecure;
	}

	public void setApiSecure(Boolean apiSecure) {
		this.apiSecure = apiSecure;
	}

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

	public ApiController() throws Exception {
		localAddresses.add(InetAddress.getLocalHost().getHostAddress());
		for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
			localAddresses.add(inetAddress.getHostAddress());
		}
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String uri = request.getRequestURI();
			if (api != null) {
				if (api.handleRequest(request, response)) {
					return null;
				}
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
				SmartContext context;
				try {
					context = new SmartContext(request, response);
				} catch (Exception e1) {
					return null;
				}
				Cookie[] cookies = request.getCookies();
				String token = null;
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals("cookie")) {
							token = cookie.getValue();
						}
					}
				}
				if (groupName.equals("token")) {
					JSONObject json = new JSONObject();
					if (token == null) {
						json.put("token", addTokenCookie(response));
					} else {
						json.put("token", getTokenCookie(response, token));
					}
					out(response, json);
					return null;
				}
				if (getApiKey() != null) {
					if (token == null) {
						return null;
					} else {
						String headerToken = request.getHeader("token");
						if (!getTokenCookie(response, token).equals(headerToken)) {
							return null;
						}
					}
				}
				if (groupName != null && entityName != null && action != null) {
					BOFilter filter = (BOFilter) BeanUtils.getValue(app, groupName);
					if (action.equals("search")) {
						Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), fixS("search" + entityName + "s"), String.class);
						List<BO> list = (List<BO>) method.invoke(filter, URLDecoder.decode(names[3], ConstUtils.UTF_8));
						list = api.onSearch(filter.getClass(), list);
						JSONObject result = new JSONObject();
						List<JSONObject> items = new ArrayList<JSONObject>();
						for (BO bo : list) {
							items.add(bo.json(false));
						}
						result.put("size", items.size());
						result.put("items", items);
						out(response, result);
					} else if (action.equals("add")) {
						SmartBindingResult bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, null);
						BO bo = (BO) context.get(entityName);
						if (!context.param("username").isEmpty()) {
							String username = BeanUtils.getValue(bo, "username").toString();
							username = AsciiUtils.webize(AsciiUtils.utf7ToText(username));
							BeanUtils.setValue(bo, "username", username);
						}
						Map<String, Object> conf = new HashMap<String, Object>();
						conf.put("appId", context.param("appId"));
						api.onPreAdd(bo, conf);
						ValidationUtils.invokeValidator(app.getValidator(bo, "create"), bo, bindingResult.getBindingResult());
						if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
							bo.create();
							api.onAdd(bo, conf);
							out(response, bo);
						} else {
							outErrors(response, context, bindingResult);
						}
					} else if (action.equals("edit")) {
						String[] fields = new String[1];
						fields[0] = "id";
						SmartBindingResult bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
						BO bo = (BO) context.get(entityName);
						bo.read();
						bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, "request");
						if (!context.param("username").isEmpty()) {
							String username = BeanUtils.getValue(bo, "username").toString();
							username = AsciiUtils.webize(AsciiUtils.utf7ToText(username));
							BeanUtils.setValue(bo, "username", username);
						}
						ValidationUtils.invokeValidator(app.getValidator(bo, "update"), bo, bindingResult.getBindingResult());
						if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
							bo.update();
							Map<String, Object> conf = new HashMap<String, Object>();
							conf.put("appId", context.param("appId"));
							api.onEdit(bo, conf);
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
						api.onRead(bo, json);
						out(response, json);
					} else if (action.equals("auth")) {
						BO bo = getBo(request, response, groupName, entityName);
						bo.read("auth");
						if (bo.getId() != null) {
							bo.read();
							Map<String, Object> conf = new HashMap<String, Object>();
							conf.put("appId", context.param("appId"));
							api.onAuth(bo, conf);
						}
						out(response, bo);
					} else if (action.equals("changePassword")) {
						String[] fields = new String[4];
						fields[0] = "id";
						fields[1] = "oldPassword";
						fields[2] = "newPassword";
						fields[3] = "confirmNewPassword";
						SmartBindingResult bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
						BO bo = (BO) context.get(entityName);
						bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, "request");
						ValidationUtils.invokeValidator(app.getValidator(bo, "execute-password"), bo, bindingResult.getBindingResult());
						if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
							bo.execute("password");
							out(response, bo);
						} else {
							outErrors(response, context, bindingResult);
						}
					} else if (action.equals("forgotPassword") || action.equals("forgot")) {
						//          String[] fields = new String[4];
						//          fields[0] = "email";
						//          SmartBindingResult bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
						//          BO bo = (BO) context.get(entityName);
						//          bindingResult = context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", null, null, "request");
						//          ValidationUtils.invokeValidator(app.getValidator(bo, "create"), bo, bindingResult.getBindingResult());
						//          bo.read("email");
						//          if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
						//            out(response, bo);
						//          } else {
						//            outErrors(response, context, bindingResult);
						//          }
						//         if (bo.getId() != null) {
						//            bo.read();
						//            JSONObject json = new JSONObject();
						//            json.put(action, true);
						//            out(response, json);
						//          } else {
						//            JSONObject json = new JSONObject();
						//            json.put(action, false);
						//            out(response, json);
						//          }
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
								api.onRead(bo, json);
								out(response, json);
							} else {
								String className = this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName;
								out(response, api.onNull(Class.forName(className), context));
							}
						} else {
							if (action != null && !action.trim().isEmpty()) {
								String className = this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName;
								BO bo = (BO) api.get((Class<? extends BO>) Class.forName(className), action);
								if (bo != null) {
									JSONObject json = bo.json();
									api.onRead(bo, json);
									out(response, json);
								} else {
									out(response, api.onNull(Class.forName(className), context));
								}
							} else {
								out(response, api.api(uri, context));
							}
						}
					}
				} else {
					out(response, api.api(uri, context));
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
		} catch (Exception e) {
		}
		return null;
	}

	private String getTokenCookie(HttpServletResponse response, String token) throws Exception {
		return api.getTokenCookie(app, response, token, getApiSecure());
	}

	private String addTokenCookie(HttpServletResponse response) throws Exception {
		return api.addTokenCookie(app, response, getApiSecure());
	}

	private BO getBo(HttpServletRequest request, HttpServletResponse response, String groupName, String entityName) throws Exception {
		SmartContext context = new SmartContext(request, response);
		String[] fields = new String[4];
		fields[0] = "id";
		fields[1] = "username";
		fields[2] = "password";
		fields[3] = "email";
		context.bind(entityName, this.getClass().getPackage().getName() + ".bo." + groupName + "." + entityName, "app", fields, null, "request");
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