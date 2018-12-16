package com.clickntap.tool.types;

import java.beans.PropertyEditorSupport;

import com.clickntap.utils.ConstUtils;

public class DatetimePropertyEditor extends PropertyEditorSupport {

	public String getAsText() {
		return getValue() == null ? ConstUtils.EMPTY : getValue().toString();
	}

	public void setAsText(String text) throws IllegalArgumentException {
		try {
			if (text != null && text.length() > 3) {
				if (text.charAt(2) == ':') {
					setValue(new Datetime(new Datetime().format("yyyy-MM-dd") + " " + text));
				} else if (!Character.isDigit(text.charAt(2))) {
					setValue(new Datetime(text, false));
				} else {
					setValue(new Datetime(text));
				}
			} else {
				setValue(null);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
