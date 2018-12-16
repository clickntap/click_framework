package com.clickntap.smart;

import com.clickntap.tool.bean.Bean;
import com.clickntap.utils.ConstUtils;

import freemarker.template.utility.StringUtil;

public class AuthenticatedUser extends Bean {

	private String name;
	private String surname;
	private String username;
	private String password;
	private String email;
	private String passes;

	public String getSecondName() {
		return surname;
	}

	public void setSecondName(String surname) {
		this.surname = surname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return name;
	}

	public void setFirstName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasses() {
		return passes;
	}

	public void setPasses(String passes) {
		this.passes = passes;
	}

	public boolean hasPass(String pass) {
		if (this.passes == null)
			return false;
		String[] passes = StringUtil.split(this.passes, ConstUtils.COMMA_CHAR);
		for (String aPass : passes) {
			if (aPass.compareToIgnoreCase(pass) == 0)
				return true;
		}
		return false;
	}

	public boolean isEnabled() {
		return true;
	}

}
