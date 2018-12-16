package com.clickntap.smart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanManager;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;
import com.clickntap.utils.WebUtils;

import freemarker.template.utility.StringUtil;

public class SmartAuthenticator implements Authenticator {

	private static final String SMART_ACTION = "action";
	private static final String SMART_LOGIN_PARAM = "smartLogin";
	private static final String SMART_LOGOUT_PARAM = "smartLogout";
	private static final String SMART_USERNAME_PARAM = "username";
	private static final String SMART_PASSWORD_PARAM = "password";
	private static final String BEAN_LOGIN_FILTER = "login";
	private static final String BEAN_LOGOUT_FILTER = "logout";
	private String className;
	private BeanManager beanManager;
	private boolean md5;
	private boolean sha1;

	public SmartAuthenticator() {
		md5 = false;
		sha1 = false;
	}

	public boolean isMd5() {
		return md5;
	}

	public void setMd5(boolean md5) {
		this.md5 = md5;
	}

	public boolean isSha1() {
		return sha1;
	}

	public void setSha1(boolean sha1) {
		this.sha1 = sha1;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) throws ClassNotFoundException {
		this.className = className;
		Class.forName(className);
	}

	public boolean isLoginRequest(HttpServletRequest request) throws Exception {
		return SMART_LOGIN_PARAM.equals(request.getParameter(SMART_ACTION));
	}

	public boolean isLogoutRequest(HttpServletRequest request) {
		return SMART_LOGOUT_PARAM.equals(request.getParameter(SMART_ACTION));
	}

	public AuthenticatedUser login(HttpServletRequest request, HttpServletResponse response, String username, String password) throws Exception {
		AuthenticatedUser user = (AuthenticatedUser) Class.forName(className).newInstance();
		user.setUsername(username);
		if (username == null || username.trim().length() == 0) {
			throw new UserNotEnabledException();
		}
		if (isMd5())
			user.setPassword(SecurityUtils.md5(password));
		else if (isSha1())
			user.setPassword(SecurityUtils.sha1(password));
		else
			user.setPassword(password);
		Bean bean = beanManager.readByFilter(user, BEAN_LOGIN_FILTER, Class.forName(className));
		if (bean == null)
			throw new UnknownUsernameException();
		if (bean != null && user.getPassword() != null && !user.getPassword().equals(BeanUtils.getValue(bean, "password")))
			throw new UnknownPasswordException();
		Boolean enabled = Boolean.valueOf(BeanUtils.getValue(bean, "enabled").toString());
		if (!enabled)
			throw new UserNotEnabledException();
		user = (AuthenticatedUser) beanManager.read(bean.getId(), Class.forName(className));
		if (request.getParameter("smartRememberMe") != null) {
			WebUtils.setClientData(response, SmartContext.SMART_USER_ID, StringUtil.leftPad(user.getId().toString(), 16, '0'));
		}
		return user;
	}

	public void logout(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception {
		WebUtils.setClientData(response, SmartContext.SMART_USER_ID, null);
		if (user != null)
			beanManager.execute(user, BEAN_LOGOUT_FILTER);
	}

	public String getUsername(HttpServletRequest request) {
		return request.getParameter(SMART_USERNAME_PARAM);
	}

	public String getPassword(HttpServletRequest request) {
		return request.getParameter(SMART_PASSWORD_PARAM);
	}

	public BeanManager getBeanManager() {
		return beanManager;
	}

	public void setBeanManager(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public AuthenticatedUser tryAutoLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String id = WebUtils.getClientData(request, SmartContext.SMART_USER_ID);
			if (id != null && !id.equals(ConstUtils.EMPTY))
				return (AuthenticatedUser) getBeanManager().read(Long.parseLong(id), Class.forName(className));
			else {
				try {
					id = WebUtils.decryptClientData(request.getParameter(SmartContext.SMART_USER_ID));
					return (AuthenticatedUser) getBeanManager().read(Long.parseLong(id), Class.forName(className));
				} catch (Exception e) {
				}
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return request.getSession().getAttribute(SmartContext.SMART_USER_ID) != null;
	}

	public void authorize(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception {
		request.getSession().setAttribute(SmartContext.SMART_USER_ID, user.getId());
		if (request.getParameter("smartRememberMe") != null) {
			WebUtils.setClientData(response, SmartContext.SMART_USER_ID, StringUtil.leftPad(user.getId().toString(), 16, '0'));
		}
	}

	public void deauthorize(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().removeAttribute(SmartContext.SMART_USER_ID);
	}

	public class SmartUser {
		private String smartUsername;
		private String smartPassword;

		public String getSmartUsername() {
			return smartUsername;
		}

		public void setSmartUsername(String smartUsername) {
			this.smartUsername = smartUsername;
		}

		public String getSmartPassword() {
			return smartPassword;
		}

		public void setSmartPassword(String smartPassword) {
			this.smartPassword = smartPassword;
		}
	}

}
