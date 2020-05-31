package com.clickntap.api;

import java.util.List;

import org.json.JSONObject;

public class Auth {
	private BO token;
	private List<BO> users;
	private JSONObject info;

	public BO getToken() {
		return token;
	}

	public void setToken(BO token) {
		this.token = token;
	}

	public JSONObject getInfo() {
		return info;
	}

	public void setInfo(JSONObject info) {
		this.info = info;
	}

	public List<BO> getUsers() {
		return users;
	}

	public void setUsers(List<BO> users) {
		this.users = users;
	}

}
