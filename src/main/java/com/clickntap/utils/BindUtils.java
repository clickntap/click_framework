package com.clickntap.utils;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;

import com.clickntap.tool.types.Datetime;
import com.clickntap.tool.types.DatetimePropertyEditor;
import com.clickntap.tool.types.MultipartFilePropertyEditor;
import com.clickntap.tool.types.NumberPropertyEditor;

public class BindUtils {

	public static void registerCustomEditor(DataBinder binder) {
		binder.registerCustomEditor(Number.class, new NumberPropertyEditor());
		binder.registerCustomEditor(Datetime.class, new DatetimePropertyEditor());
		binder.registerCustomEditor(MultipartFile.class, new MultipartFilePropertyEditor());
	}

	public static void bind(Object object, MutablePropertyValues pvs) {
		DataBinder binder = new DataBinder(object);
		BindUtils.registerCustomEditor(binder);
		bind(binder, pvs);
	}

	public static void bind(DataBinder dataBinder, MutablePropertyValues pvs) {
		dataBinder.bind(pvs);
	}

}
