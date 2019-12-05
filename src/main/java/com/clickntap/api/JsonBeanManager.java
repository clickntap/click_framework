package com.clickntap.api;

import com.clickntap.tool.bean.BeanManager;

public interface JsonBeanManager extends BeanManager {

	public String readAsJson(Number id, Class clazz) throws Exception;

	public String readListAsJsonByFilter(Class clazz, String filter, String what) throws Exception;
}
