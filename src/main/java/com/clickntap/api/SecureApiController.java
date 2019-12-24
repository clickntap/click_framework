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
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

import freemarker.template.utility.StringUtil;

public class SecureApiController implements Controller {

	private ECPublicKey publicKey;
	private ECPrivateKey privateKey;
	private App app;
	private SecureApi api;
	private String boPackage;
	private Resource sqlFolder;
	private AdvancedSearch search;
	private FreemarkerScriptEngine engine;

	public void setSqlFolder(Resource sqlFolder) {
		this.sqlFolder = sqlFolder;
	}

	public void setBoPackage(String boPackage) {
		this.boPackage = boPackage;
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

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
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
					BO token = signed(request, privateKey);
					M.invoke(secureRequest, "setDeviceToken", token);
					if (token != null) {
						if (api != null) {
							if (api.handleRequest(request, response, secureRequest)) {
								return null;
							}
						}
						List<BO> users;
						try {
							users = (List) M.invoke(token, "getAuthUsers");
						} catch (Exception e) {
							users = new ArrayList<BO>();
						}
						if (secureRequest.path(0).equalsIgnoreCase("f")) {
							Number authId = null;
							if (users.size() != 0) {
								authId = (Number) M.invoke(users.get(0), "getUserId");
							}
							json = fapi(sqlFolder, request, engine, search, secureRequest.getPath(), authId);
							out(response, json);
							return null;
						}
						if (users.size() != 0 && "me".equalsIgnoreCase(secureRequest.path(0))) {
							BO bo = (BO) M.invoke(users.get(0), "getUser");
							JSONObject data = bo.json(true);
							out(response, data);
							return null;
						}
						if (secureRequest.getPath().size() > 2) {
							if ("add".equalsIgnoreCase(secureRequest.path(2))) {
								return add(request, response, json, secureRequest);
							}
							if ("edit".equalsIgnoreCase(secureRequest.path(2))) {
								return edit(request, response, json, secureRequest);
							}
							if ("delete".equalsIgnoreCase(secureRequest.path(2))) {
								return delete(request, response, json, secureRequest);
							}
							if ("signin".equalsIgnoreCase(secureRequest.path(2))) {
								return signin(request, response, json, secureRequest, token);
							}
							if ("search".equalsIgnoreCase(secureRequest.path(2))) {
								return search(request, response, json, secureRequest, token);
							}
							Long id;
							if ((id = num(secureRequest.path(2))) != 0) {
								return get(response, secureRequest, id);
							}
						}
						out(response, json);
					} else {
						err(response);
					}
				}
			} else {
				err(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			err(response);
		}
		return null;
	}

	public static JSONObject fapi(File sqlFolder, HttpServletRequest request, ScriptEngine engine, AdvancedSearch search, List<String> path, Number authId) throws Exception {
		JSONObject json = new JSONObject();
		String smartQuery = path.get(1);
		SmartContext ctx = new SmartContext(request, null);
		ctx.put("path", path);
		if (authId != null) {
			ctx.put("authId", authId);
		}
		JSONObject sqlJson = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(sqlFile(sqlFolder, smartQuery), ConstUtils.UTF_8)));
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
			JSONObject item = search.run(sqlJson, ctx, json, false).get(0);
			json.put("item", item);
			if (sqlJson.has("includes")) {
				ctx.put("item", item);
				JSONArray includes = sqlJson.getJSONArray("includes");
				for (int i = 0; i < includes.length(); i++) {
					JSONObject include = includes.getJSONObject(i);
					sqlJson = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(sqlFile(sqlFolder, include.getString("file")), ConstUtils.UTF_8)));
					List<JSONObject> otherItems = search.run(sqlJson, ctx, json, i == 0);
					json.put(include.getString("name"), otherItems);
				}
			}
		} else {
			List<JSONObject> items = search.run(sqlJson, ctx, json, true);
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
			json.put("count", search.count(sqlJson, ctx, json));
			json.put("items", items);
			json.put("size", items.size());
		}
		File js = jsFile(sqlFolder, smartQuery);
		if (js.exists()) {
			ScriptEngineManager manager = new ScriptEngineManager();
			javax.script.ScriptEngine javascriptEngine = manager.getEngineByName("nashorn");
			javascriptEngine.put("sql", sqlJson);
			javascriptEngine.put("json", json);
			javascriptEngine.put("request", request);
			json = new JSONObject((javascriptEngine.eval(FileUtils.readFileToString(js))).toString());
		}
		return json;
	}

	public static JSONObject fapi(Resource sqlFolder, HttpServletRequest request, ScriptEngine engine, AdvancedSearch search, List<String> path, Number authId) throws Exception {
		return fapi(sqlFolder.getFile(), request, engine, search, path, authId);
	}

	public static File sqlFile(File sqlFolderFile, String smartQuery) throws Exception {
		return new File(sqlFolderFile.getCanonicalPath() + "/" + smartQuery + ".json");
	}

	public static File jsFile(File sqlFolderFile, String smartQuery) throws Exception {
		return new File(sqlFolderFile.getCanonicalPath() + "/" + smartQuery + ".js");
	}

	private ModelAndView get(HttpServletResponse response, SecureRequest secureRequest, Number id) throws Exception {
		BOFilter filter = (BOFilter) BeanUtils.getValue(app, secureRequest.path(0));
		Method method = org.springframework.beans.BeanUtils.findDeclaredMethod(filter.getClass(), "get" + secureRequest.path(1), Number.class);
		BO bo = (BO) method.invoke(filter, id);
		out(response, bo.json());
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
		BO authUser = (BO) Class.forName(boPackage + ".security.AuthUser").newInstance();
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

	private ModelAndView add(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
		SmartContext context = new SmartContext(request, response);
		SmartBindingResult bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, null);
		BO bo = (BO) context.get(secureRequest.path(1));
		ValidationUtils.invokeValidator(app.getValidator(bo, "create"), bo, bindingResult.getBindingResult());
		if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
			bo.create();
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
		bindingResult = context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", null, null, "request");
		ValidationUtils.invokeValidator(app.getValidator(bo, "update"), bo, bindingResult.getBindingResult());
		if (bindingResult.getBindingResult().getAllErrors().size() == 0) {
			bo.update();
			Map<String, Object> conf = new HashMap<String, Object>();
			conf.put("appId", context.param("appId"));
			out(response, bo.json());
		} else {
			out(response, json, bindingResult);
		}
		return null;
	}

	private ModelAndView delete(HttpServletRequest request, HttpServletResponse response, JSONObject json, SecureRequest secureRequest) throws Exception {
		SmartContext context = new SmartContext(request, response);
		String[] fields = new String[1];
		fields[0] = "id";
		context.bind(secureRequest.path(1), boPackage + "." + secureRequest.path(0) + "." + secureRequest.path(1), "app", fields, null, "request");
		BO bo = (BO) context.get(secureRequest.path(1));
		json.put("n", bo.delete());
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

	public BO signed(HttpServletRequest request, ECPrivateKey privateKey) throws Exception {
		BO token = (BO) Class.forName(boPackage + ".security.DeviceToken").newInstance();
		token.setApp(app);
		M.invoke(token, "setToken", request.getHeader("token"));
		token.read("token");
		if (token.getId() != null) {
			token.read();
			ECPublicKey publicKey = SecureUtils.importPublicKey(M.invoke(token, "getPublicKey").toString());
			byte[] encoded = SecureUtils.base64dec(request.getHeader("sign"));
			JSONObject sign = new JSONObject(SecureUtils.decrypt(SecureUtils.generateSecret(publicKey, privateKey), encoded));
			if (sign.getLong("t") > Long.parseLong(M.invoke(token, "getLastRequestTime").toString())) {
				if (M.invoke(token, "getToken").toString().equals(sign.get("token"))) {
					M.invoke(token, "setLastRequestTime", sign.getLong("t"));
					token.update();
					return token;
				}
			}
		}
		return null;
	}

	private void handshake(JSONObject json, SecureRequest secureRequest) throws Exception {
		long now = System.currentTimeMillis();
		BO token = (BO) Class.forName(boPackage + ".security.DeviceToken").newInstance();
		token.setApp(app);
		M.invoke(token, "setToken", secureRequest.getD());
		token.read("token");
		if (token.getId() == null) {
			M.invoke(token, "setPublicKey", SecurityUtils.base64enc(secureRequest.getPublicKey().getEncoded()));
			M.invoke(token, "setToken", SecurityUtils.base64enc(SecurityUtils.generateKey(512).getEncoded()));
			M.invoke(token, "setChannel", secureRequest.getC());
			M.invoke(token, "setLastRequestTime", secureRequest.getT());
			token.create();
		} else {
			token.read();
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

	private static void out(HttpServletResponse response, JSONObject json) throws Exception {
		out(response, json, null);
	}

	private void err(HttpServletResponse response) throws Exception {
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

}
