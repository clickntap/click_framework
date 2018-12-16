package com.clickntap.smart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.tool.bean.BeanManager;

public interface Authenticator {

	public boolean isLoginRequest(HttpServletRequest request) throws Exception;

	public boolean isLogoutRequest(HttpServletRequest request);

	public AuthenticatedUser login(HttpServletRequest request, HttpServletResponse response, String username, String password) throws Exception;

	public void logout(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception;

	public String getUsername(HttpServletRequest request);

	public String getPassword(HttpServletRequest request);

	public AuthenticatedUser tryAutoLogin(HttpServletRequest request, HttpServletResponse response) throws Exception;

	public BeanManager getBeanManager() throws Exception;

	public String getClassName();

	public boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception;

	public void authorize(HttpServletRequest request, HttpServletResponse response, AuthenticatedUser user) throws Exception;

	public void deauthorize(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
