package com.clickntap.click;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.api.BO;
import com.clickntap.api.M;
import com.clickntap.smart.AuthenticatedUser;
import com.clickntap.smart.Authenticator;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.bean.BeanManager;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.WebUtils;

public class SmartAuthenticator implements Authenticator {

  private static final String SMART_ACTION = "action";
  private static final String SMART_LOGIN_PARAM = "smartLogin";
  private static final String SMART_LOGOUT_PARAM = "smartLogout";
  private static final String SMART_USERNAME_PARAM = "username";
  private static final String SMART_PASSWORD_PARAM = "password";
  private static final String BEAN_EMAIL_FILTER = "email";
  private static final String BEAN_LOGIN_FILTER = "login";
  private static final String BEAN_LOGOUT_FILTER = "logout";
  private String className;
  private String boPackage;
  private BeanManager beanManager;

  public SmartAuthenticator() {
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) throws ClassNotFoundException {
    this.className = className;
    Class.forName(className);
  }

  public String getBoPackage() {
    return boPackage;
  }

  public void setBoPackage(String boPackage) {
    this.boPackage = boPackage;
  }

  public Number getNumAttempts() {
    return 5;
  }

  public Number getNumTryAgain() {
    return 20;
  }

  public boolean isLoginRequest(HttpServletRequest request) throws Exception {
    return SMART_LOGIN_PARAM.equals(request.getParameter(SMART_ACTION));
  }

  public boolean isLogoutRequest(HttpServletRequest request) {
    return SMART_LOGOUT_PARAM.equals(request.getParameter(SMART_ACTION));
  }

  public BO getDeviceToken(HttpServletRequest request) {
    return getDeviceToken(request.getHeader(SmartContext.DEVICE_TOKEN));
  }

  private BO getDeviceToken(String d) {
    try {
      if (d == null || d.equalsIgnoreCase(ConstUtils.EMPTY)) {
        return null;
      }
      BO token = (BO) Class.forName(getBoPackage() + ".security.DeviceToken").getDeclaredConstructor().newInstance();
      token.setBeanManager(getBeanManager());
      M.invoke(token, "setToken", d);
      token.read("token");
      if (token.getId() != null) {
        token.read();
        return token;
      }
    } catch (Exception e) {
    }
    return null;
  }

  public AuthenticatedUser tryAutoLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
    try {
      BO token = getDeviceToken(WebUtils.getClientData(request, SmartContext.DEVICE_TOKEN));
      if (token != null) {
        for (BO user : (List<BO>) M.invoke(token, "getAuthUsers")) {
          return (AuthenticatedUser) getBeanManager().read((Number) M.invoke(user, "getUserId"), Class.forName(className));
        }
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public AuthenticatedUser login(HttpServletRequest request, HttpServletResponse response, String email, String password) throws Exception {
    BO token = getDeviceToken(request);
    if (token == null) {
      return null;
    }

    AuthenticatedUser user = (AuthenticatedUser) Class.forName(getClassName()).getDeclaredConstructor().newInstance();
    user.setBeanManager(getBeanManager());
    M.invoke(user, "setEmail", email); // email or username
    M.invoke(user, "setPassword", password);
    user.read(BEAN_LOGIN_FILTER);

    if (user.getId() != null) {
      user.read();
      for (BO item : (List<BO>) M.invoke(token, "getAuthUsers")) {
        item.delete();
      }

      BO authUser = (BO) Class.forName(boPackage + ".security.AuthUser").getDeclaredConstructor().newInstance();
      authUser.setBeanManager(getBeanManager());
      M.invoke(authUser, "setUserId", user.getId());
      M.invoke(authUser, "setDeviceId", token.getId());
      authUser.create();

      WebUtils.setClientData(response, SmartContext.DEVICE_TOKEN, (String) M.invoke(token, "getToken"));
      return user;
    }

    return null;
  }

  public AuthenticatedUser login(String username) throws Exception {
    AuthenticatedUser user = (AuthenticatedUser) Class.forName(getClassName()).getDeclaredConstructor().newInstance();
    user.setBeanManager(getBeanManager());
    M.invoke(user, "setEmail", username);
    user.read(BEAN_EMAIL_FILTER);
    return user;
  }

  public void logout(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception {
    BO token = getDeviceToken(request);
    if (token != null) {
      for (BO item : (List<BO>) M.invoke(token, "getAuthUsers")) {
        item.delete();
      }
    }
    WebUtils.setClientData(response, SmartContext.DEVICE_TOKEN, null);
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

  public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
    try {
      Number userId = (Number) request.getSession().getAttribute(SmartContext.SMART_USER_ID);
      if (userId != null) {
        BO token = getDeviceToken(request);
        if (token != null && token.getId() != null) {
          for (BO item : (List<BO>) M.invoke(token, "getAuthUsers")) {
            return ((Number) M.invoke(item, "getUserId")).longValue() == userId.intValue();
          }
        }
      }
    } catch (Exception e) {
    }
    return false;
  }

  public void authorize(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception {
    request.getSession().setAttribute(SmartContext.SMART_USER_ID, user.getId());
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

  public void failedAttempts(HttpServletRequest request, HttpServletResponse response) throws Exception {

  }

}