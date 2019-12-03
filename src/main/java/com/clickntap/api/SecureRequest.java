package com.clickntap.api;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;

import com.clickntap.utils.BindUtils;

import freemarker.template.utility.StringUtil;

public class SecureRequest {

	private List<String> path;
	private String k;
	private String d;
	private String c;
	private Long t;
	private BO token;

	public SecureRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		while (uri.indexOf("/api/") > 0) {
			uri = uri.substring(uri.indexOf("/api/"));
		}
		if (uri.indexOf("/api/") == 0) {
			this.path = Arrays.asList(StringUtil.split(uri.substring(5), '/'));
		}
		ServletRequestDataBinder binder = new ServletRequestDataBinder(this, this.getClass().getName());
		BindUtils.registerCustomEditor(binder);
		binder.bind(request);
	}

	public ECPublicKey getPublicKey() throws Exception {
		return SecureUtils.importClientPublicKey(getK());
	}

	public SecretKeySpec getSecretKey(ECPrivateKey privateKey) throws Exception {
		return SecureUtils.generateSecret(getPublicKey(), privateKey);
	}

	public String path(int index) {
		return getPath().get(index);
	}

	public List<String> getPath() {
		return path;
	}

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public void setDeviceToken(BO token) {
		this.token = token;
	}

	public BO getToken() {
		return token;
	}

	public void setToken(BO token) {
		this.token = token;
	}

}
