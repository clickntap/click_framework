package com.clickntap.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.tool.types.Datetime;

public class WebUtils {

	public static final String KEY = "6oRfpzs/qLDRlYAwSmYpvg==";

	public static String getClientData(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		for (int i = 0; cookies != null && i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookie.getName().equals(key))
				return WebUtils.decryptClientData(cookie.getValue());
		}
		return null;
	}

	public static void setClientData(HttpServletResponse response, String key, String value) {
		value = WebUtils.encryptClientData(value);
		Cookie cookie = new Cookie(key, value);
		cookie.setPath(ConstUtils.SLASH);
		cookie.setMaxAge(Datetime.ONEYEARINSECONDS);
		response.addCookie(cookie);
	}

	public static String encryptClientData(String value) {
		try {
			// byte[] data =
			// SecurityUtils.encrypt(value.getBytes(ConstUtils.UTF_8),
			// Base64.decode(KEY.toCharArray()));
			// return new String(Base64.encode(data));
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static String decryptClientData(String value) {
		try {
			// byte[] data = Base64.decode(value.toCharArray());
			// data = SecurityUtils.decrypt(data,
			// Base64.decode(KEY.toCharArray()));
			// return new String(data, ConstUtils.UTF_8);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String args[]) {
		try {
			System.out.println(java.util.Base64.getEncoder().encode(SecurityUtils.generateKey(128).getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		String key = "--test--";
		String cryptedKey = WebUtils.encryptClientData(key);
		System.out.println("cryptedKey:" + cryptedKey);
		System.out.println("key:" + WebUtils.decryptClientData(cryptedKey));
	}
}
