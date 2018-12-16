package com.clickntap.tool.types;

import java.beans.PropertyEditorSupport;

import org.springframework.web.multipart.MultipartFile;

public class MultipartFilePropertyEditor extends PropertyEditorSupport {

	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			try {
				super.setValue(value);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
}
