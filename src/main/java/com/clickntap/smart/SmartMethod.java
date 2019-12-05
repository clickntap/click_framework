package com.clickntap.smart;

import org.dom4j.Element;

public class SmartMethod extends SmartAction {

	private String name;

	public SmartMethod(String name, Element element) {
		super(element);
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
