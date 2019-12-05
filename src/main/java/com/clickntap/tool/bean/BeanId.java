package com.clickntap.tool.bean;

import java.io.Serializable;

public class BeanId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Number id;

	public BeanId() {
	}

	public BeanId(Number id) {
		setId(id);
	}

	public Number getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = id;
	}

}
