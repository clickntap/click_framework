package com.clickntap.smart;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.validation.DataBinder;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.clickntap.api.BO;
import com.clickntap.api.BOApp;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.html.HTMLParser;
import com.clickntap.tool.mail.Mail;
import com.clickntap.tool.mail.Mailer;
import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.AsciiUtils;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;
import com.clickntap.utils.Pager;
import com.clickntap.utils.StringUtils;
import com.clickntap.utils.WebUtils;

import freemarker.template.utility.StringUtil;

public class SmartContext extends HashMap<String, Object> implements Serializable {
	public static final String SMART_USER = "smartUser";
	public static final String SMART_USER_ID = "smartUserId";
	private static final String SMART_AUTHENTICATOR_BEAN = "smartAuthenticator";
	private static final String SMART_APP_BEAN = "smartApp";
	private static final String SMART_STORED_REQUEST = "smartStoredRequest";
	private static final String SMART_SESSION_KEY = "smartSessionKey";
	private static final String SMART_SESSIONS_KEY = "smartSessionsKey";
	private static final String APP_SESSIONS_KEY = "appSessionsKey";
	private static Log log = LogFactory.getLog(SmartContext.class);
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map session;
	private String activeSessionKey;
	private ApplicationContext applicationContext;

	private String ref;
	private String extension;
	private Authenticator authenticator;
	private SmartApp smartApp;

	private boolean redirected;
	private boolean isBreak;
	private RequestContext requestContext;
	private SmartRequest smartRequest;
	private Exception exception;

	public SmartContext() {
		this(null, null);
	}

	public SmartContext(HttpServletRequest request, HttpServletResponse response) {
		if (request != null) {
			String uri = request.getRequestURI().substring(request.getContextPath().length() + 1);
			int x = uri.indexOf(ConstUtils.DOT);
			this.redirected = false;
			if (x > 0) {
				this.ref = uri.substring(0, x);
				this.extension = uri.substring(x + 1);
			}
			this.request = request;
			this.response = response;
			try {
				this.applicationContext = RequestContextUtils.findWebApplicationContext(request);
				this.authenticator = (Authenticator) getBean(SMART_AUTHENTICATOR_BEAN);
				this.smartApp = (SmartApp) getBean(SMART_APP_BEAN);
				this.requestContext = new RequestContext(request, this);
			} catch (Exception e) {
				this.applicationContext = null;
				this.authenticator = null;
				this.smartApp = null;
				this.requestContext = null;
			}
		}
		put(ConstUtils.THIS, this);
	}

	public List<String> getActiveSessions() {
		List<String> activeSessions = (List<String>) request.getSession().getAttribute(SMART_SESSIONS_KEY);
		if (activeSessions == null) {
			activeSessions = new ArrayList<String>();
			request.getSession().setAttribute(SMART_SESSIONS_KEY, activeSessions);
		}
		return activeSessions;
	}

	public String getActiveSessionKey() {
		return activeSessionKey;
	}

	public Datetime toSessionDate(String ietfDate) throws Exception {
		return Datetime.parseIETFDate(ietfDate);
	}

	public Map getActiveSession() {
		Map map = (Map) request.getSession().getAttribute(activeSessionKey);
		if (map == null) {
			map = new HashMap();
			request.getSession().setAttribute(activeSessionKey, map);
		}
		return map;
	}

	public JSONArray toJsonArray(String array) {
		return new JSONArray(array);
	}

	public JSONObject toJsonObject(String object) {
		return new JSONObject(object);
	}

	public boolean isBreak() {
		return isBreak;
	}

	public void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}

	public void stop() {
		setBreak(true);
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Datetime toDate(String s) {
		return new Datetime(s);
	}

	public String getReferer() {
		return request.getHeader("referer");
	}

	public SmartApp getSmartApp() {
		return smartApp;
	}

	public void setSmartApp(SmartApp smartApp) {
		this.smartApp = smartApp;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setRef(String name, String extension) {
		this.ref = name;
		this.extension = extension;
	}

	public SmartRequest getSmartRequest() {
		return smartRequest;
	}

	public Map getSession() {
		if (session == null) {
			if ("selectSmartSession".equals(param("action"))) {
				List<String> activeSessions = getActiveSessions();
				if (activeSessions.contains(param("smartSession"))) {
					request.getSession().setAttribute(SMART_SESSION_KEY, param("smartSession"));
				}
			}
			try {
				activeSessionKey = (String) request.getSession().getAttribute(SMART_SESSION_KEY);
			} catch (Exception e1) {
				activeSessionKey = null;
			}
			if ("removeSmartSession".equals(param("action"))) {
				List<String> activeSessions = getActiveSessions();
				request.getSession().removeAttribute(SMART_SESSION_KEY);
				activeSessions.remove(activeSessionKey);
				if (activeSessions.size() > 0) {
					activeSessionKey = activeSessions.get(activeSessions.size() - 1);
					request.getSession().setAttribute(SMART_SESSION_KEY, activeSessionKey);
				} else {
					activeSessionKey = null;
				}
			}
			if ("newSmartSession".equals(param("action"))) {
				activeSessionKey = null;
			}
			if (activeSessionKey == null) {
				List<String> activeSessions = getActiveSessions();
				try {
					activeSessionKey = new Datetime().toIETFDate();
					if (!activeSessions.contains(activeSessionKey)) {
						activeSessions.add(activeSessionKey);
					}
				} catch (ParseException e) {
				}
				request.getSession().setAttribute(SMART_SESSION_KEY, activeSessionKey);
			}
			if (!param("appSessionKey").isEmpty()) {
				HashMap<String, Map> appSessionMap = (HashMap<String, Map>) request.getSession().getAttribute(APP_SESSIONS_KEY);
				if (appSessionMap == null) {
					appSessionMap = new HashMap<String, Map>();
					request.getSession().setAttribute(APP_SESSIONS_KEY, appSessionMap);
				}
				if (!appSessionMap.containsKey(param("appSessionKey"))) {
					appSessionMap.put(param("appSessionKey"), new HashMap());
				}
				this.session = appSessionMap.get(param("appSessionKey"));
			} else {
				this.session = getActiveSession();
			}
		}
		return session;
	}

	public void storeRequest() {
		session.put(SMART_STORED_REQUEST, new SmartRequest(ref, request));
	}

	public SmartRequest getStoredRequest() {
		return (SmartRequest) session.get(SMART_STORED_REQUEST);
	}

	public boolean isStoredRequest() {
		if (session != null) {
			return session.get(SMART_STORED_REQUEST) != null;
		}
		return false;
	}

	public void loadRequest() {
		if (isStoredRequest()) {
			smartRequest = getStoredRequest();
			session.remove(SMART_STORED_REQUEST);
		}
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public String tr(String key) {
		return getRequestContext().getMessage(key, new Object[] { this });
	}

	public String plainText(String html) {
		try {
			return HTMLParser.getText(html);
		} catch (Exception e) {
			return html;
		}
	}

	public String tr(String key, Object target) {
		return getRequestContext().getMessage(key, new Object[] { target });
	}

	public List<SmartController> level(int i) throws SmartControllerNotFoundException {
		List<SmartController> path = getController().getPath();
		i--;
		if (i >= 0 && i < path.size())
			return path.get(i).getChildren();
		else
			return new ArrayList<SmartController>();
	}

	public boolean isHilited(String ref) {
		return getRef().startsWith(ref);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getURI() throws SmartControllerNotFoundException {
		if (getReferer() != null && getController().isAjax())
			return getReferer();
		return getURI(getRef());
	}

	public String getURI(String ref) {
		return request.getContextPath() + ConstUtils.SLASH + ref + ConstUtils.DOT + extension;
	}

	public Object getBean(String ref) {
		try {
			return applicationContext.getBean(ref);
		} catch (Exception e) {
			return null;
		}
	}

	public Resource getResource(String path) {
		return applicationContext.getResource(path);
	}

	public String param(String key) {
		String value;
		try {
			value = StringUtils.toString(request.getParameter(key));
		} catch (Throwable e) {
			value = ConstUtils.EMPTY;
		}
		return value;
	}

	public int intParam(String key) {
		try {
			return Integer.parseInt(param(key));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public boolean isAuthenticated() throws Exception {
		return authenticator == null ? false : authenticator.isAuthenticated(request, response);
	}

	public boolean canTryAutoLogin() throws Exception {
		return authenticator == null ? false : authenticator.isAuthenticated(request, response);
	}

	public Number getUserId() {
		return (Number) request.getSession().getAttribute(SMART_USER_ID);
	}

	public boolean tryLogin(boolean always) throws Exception {
		if (isLoginRequest() || always) {
			SmartUser smartUser = new SmartUser();
			smartUser.setUsername(authenticator.getUsername(request));
			smartUser.setPassword(authenticator.getPassword(request));
			DataBinder binder = new DataBinder(smartUser, SMART_USER);
			AuthenticatedUser user = null;
			try {
				user = authenticator.login(request, response, smartUser.getUsername(), smartUser.getPassword());
			} catch (UnknownUsernameException ue) {
				binder.getBindingResult().rejectValue("username", "unknown");
			} catch (UnknownPasswordException pe) {
				binder.getBindingResult().rejectValue("password", "unknown");
			} catch (UserNotEnabledException pe) {
				binder.getBindingResult().rejectValue("username", "pending");
			}
			if (user != null) {
				authorize(user);
				return true;
			} else {
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			}
			putAll(binder.getBindingResult().getModel());
		}
		return false;
	}

	public void authorize(AuthenticatedUser user) throws Exception {
		authenticator.authorize(request, response, user);
	}

	public void deauthorize(AuthenticatedUser user) throws Exception {
		authenticator.deauthorize(request, response);
	}

	public boolean tryLogout() throws Exception {
		if (isLogoutRequest()) {
			if (isAuthenticated()) {
				logout();
			}
			return true;
		}
		return false;
	}

	public void logout() throws Exception {
		Number id = getUserId();
		authenticator.logout(request, response, (AuthenticatedUser) authenticator.getBeanManager().read(id, Class.forName(authenticator.getClassName())));
		authenticator.deauthorize(request, response);
	}

	public boolean isLoginRequest() throws Exception {
		if (authenticator != null)
			if (authenticator.isLoginRequest(request))
				return true;
		return false;
	}

	public boolean isLogoutRequest() throws Exception {
		if (authenticator != null)
			if (authenticator.isLogoutRequest(request))
				return true;
		return false;
	}

	public void redirect(String ref) throws IOException {
		if (ref != getRef())
			sendRedirect(getURI(ref));
	}

	public void sendRedirect(String uri) throws IOException {
		if (redirected)
			return;
		redirected = true;
		getResponse().sendRedirect(uri);
	}

	public boolean isRedirected() {
		return redirected;
	}

	public SmartController getController() throws SmartControllerNotFoundException {
		SmartController controller = null;
		try {
			controller = smartApp.getController(this);
		} catch (Exception e) {
			if (controller == null)
				controller = smartApp.getMapper();
		}
		if (controller == null)
			controller = smartApp.getMapper();
		if (controller == null)
			throw new SmartControllerNotFoundException(this.getRef());
		return controller;
	}

	public String eval(Map map, String script) throws Exception {
		return smartApp.eval(map, script);
	}

	public String eval(String script) throws Exception {
		return smartApp.eval(this, script);
	}

	public boolean evalRule(Map map, String rule) throws Exception {
		return smartApp.evalRule(map, rule);
	}

	public boolean evalRule(String rule) throws Exception {
		return smartApp.evalRule(this, rule);
	}

	public SmartBindingResult bind(String objectName, String objectClass, String channel, String[] allowedFields, String[] disallowedFields, String scope) throws Exception {
		return smartApp.bind(this, objectName, objectClass, channel, allowedFields, disallowedFields, scope);
	}

	public void load(String objectName, String objectClass, String channel, String scope) throws Exception {
		Object object = smartApp.load(this, objectName, objectClass, channel, scope);
		if (log.isDebugEnabled()) {
			log.debug("load '" + objectName + "' with scope '" + scope + "' on channel '" + channel + "'");
		}
		put(objectName, object);
	}

	public SmartBindingResult bind(Object object, String objectName, String[] allowedFields, String[] disallowedFields) throws Exception {
		return SmartApp.bind(this, object, objectName, allowedFields, disallowedFields);
	}

	public SmartBindingResult bind(Object object, String objectName) throws Exception {
		return SmartApp.bind(this, object, objectName, null, null);
	}

	public SmartBindingResult bind(Object object) throws Exception {
		return SmartApp.bind(this, object, object.getClass().getName(), null, null);
	}

	public String utf7(String text) {
		return AsciiUtils.textToUtf7(text);
	}

	public int rand() throws Exception {
		return rand(Integer.MAX_VALUE);
	}

	public int rand(int num) {
		return (int) (Math.random() * num) + 1;
	}

	public BindStatus getBindStatus(String path) {
		return getRequestContext().getBindStatus(path);
	}

	public BindStatus getBindStatus(String path, String propertyName) {
		int x = path.lastIndexOf('.');
		if (x > 0) {
			path = path.substring(0, x) + '.' + propertyName;
			return getRequestContext().getBindStatus(path);
		}
		return null;
	}

	public String now() {
		return Long.toString(System.currentTimeMillis());
	}

	public String code(String key) {
		return StringUtils.code(key);
	}

	public Datetime date() {
		return new Datetime();
	}

	public Datetime date(String date) {
		return new Datetime(date);
	}

	public Datetime parseDate(String date) {
		try {
			return new Datetime(date);
		} catch (Exception e) {
			return null;
		}
	}

	public void reverse(List list) {
		Collections.reverse(list);
	}

	public void shuffle(List list) {
		Collections.shuffle(list);
	}

	public String limit(String text, int limit) {
		return AsciiUtils.limit(text, limit);
	}

	public void tryAutoLogin() throws Exception {
		if (authenticator != null) {
			AuthenticatedUser user = authenticator.tryAutoLogin(request, response);
			if (user != null)
				authenticator.authorize(request, response, user);
		}
	}

	public void sendmail(String email, String name, boolean starttl) throws Exception {
		Mailer mailer = (Mailer) getBean("mailer");
		Mail mail = mailer.newMail(name, starttl);
		mail.addTo(email);
		mailer.setup(mail, this);
		mailer.sendmail(mail);
	}

	public String conf(String key) {
		return smartApp.conf(key);
	}

	public void setClientData(String key, String value) throws Exception {
		WebUtils.setClientData(response, key, value);
	}

	public String getClientData(String key) throws Exception {
		return WebUtils.getClientData(request, key);
	}

	public String getBrowser() {
		String ua = request.getHeader("User-Agent");
		if (ua != null) {
			if (ua.indexOf("iPhone") >= 0)
				return "iphone";
			else if (ua.indexOf("Android") >= 0)
				return ua.indexOf("Mobile") >= 0 ? "android-phone" : "android-tablet";
			else if (ua.indexOf("iPad") >= 0)
				return "ipad";
			else if (ua.indexOf("Firefox") >= 0)
				return "firefox";
			else if (ua.indexOf("MSIE 8") >= 0)
				return "msie8";
			else if (ua.indexOf("MSIE 7") >= 0)
				return "msie7";
			else if (ua.indexOf("MSIE 6") >= 0)
				return "msie6";
			else if (ua.indexOf("MSIE 5") >= 0)
				return "msie5";
			else if (ua.indexOf("Opera") >= 0)
				return "opera";
			else if (ua.indexOf("Chrome") >= 0)
				return "chrome";
			else if (ua.indexOf("Safari") >= 0)
				return "safari";
			if (getUserAgent().getChannel().equals("mobile"))
				return "mobile";
		}
		return ua;
	}

	public boolean isDevice() {
		String browser = getBrowser();
		if (browser == null)
			return false;
		else
			return browser.equals("iphone") || browser.equals("ipad") || browser.equals("mobile") || browser.equals("android-phone") || browser.equals("android-tablet");
	}

	public SmartUserAgent getUserAgent() {
		return new SmartUserAgent(this);
	}

	public void sendError(String errorMessageKey, String errorMessage) throws Exception {
		response.addHeader(errorMessageKey, errorMessage);
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	public Long toLong(String s) {
		Long retVal;
		try {
			retVal = Long.parseLong(s);
		} catch (Exception e) {
			retVal = null;
		}
		return retVal;
	}

	public boolean dateEquals(Datetime t1, Datetime t2, String format) {
		return t1.format(format).equals(t2.format(format));
	}

	public String css(String less) throws Exception {
		return LessUtils.eval(less);
	}

	public Map newMap() {
		return new LinkedHashMap<Object, Object>();
	}

	public List newList() {
		return new ArrayList<Object>();
	}

	public Pager newPager(List list, int pageNumber) throws Exception {
		return newPager(list, pageNumber, 5, 20);
	}

	public Pager newPager(List list, int pageNumber, int linksNum, int itemsForPageNum) throws Exception {
		Pager pager = new Pager(linksNum, itemsForPageNum);
		pager.selectPage(list, pageNumber);
		return pager;
	}

	public static List asList(JSONArray array) {
		List items = new ArrayList();
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				items.add(array.get(i));
			}
		}
		return items;
	}

	public JSONArray boValues(String className, Number id, String fields) throws Exception {
		JSONArray values = new JSONArray();
		BO bo = (BO) Class.forName(className).getDeclaredConstructor().newInstance();
		bo.setId(id);
		bo.setApp((BOApp) getBean("app"));
		bo.read();
		String[] names = StringUtil.split(fields, ',');
		for (String name : names) {
			try {
				Object o = BeanUtils.getValue(bo, name);
				JSONObject value = new JSONObject();
				value.put(name, (o instanceof BO) ? ((BO) o).json(false) : o);
				values.put(value);
			} catch (Exception e) {
			}
		}
		return values;
	}

}
