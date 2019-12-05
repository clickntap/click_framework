package com.clickntap.smart;

public class SmartControllerNotFoundException extends Exception {

	public SmartControllerNotFoundException(String ref) {
		super("'" + ref + "' not found");
	}

}
