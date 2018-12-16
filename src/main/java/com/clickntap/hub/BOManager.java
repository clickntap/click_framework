package com.clickntap.hub;

import java.util.ArrayList;
import java.util.List;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.ProxyBeanManager;

public class BOManager extends ProxyBeanManager {

	public BOManager() {
		System.setProperty("java.awt.headless", "true");
	}

	public <T> T getBO(Class<T> clazz, Number id) throws Exception {
		return (T) read(id, clazz);
	}

	public <T> T getBO(Class<T> clazz, BO bo, String filter) throws Exception {
		return (T) readByFilter(bo, filter, clazz);
	}

	public <T> T getBO(Class<T> clazz, String filter) throws Exception {
		return (T) readByFilter(null, filter, clazz);
	}

	public <T> List<T> getBOList(BO bo, String filter) throws Exception {
		List<T> items = new ArrayList<T>();
		for (Number id : readList(bo, filter))
			items.add((T) read(id, bo.getClass()));
		return items;
	}

	public <T> List<T> getBOListByClass(Class<T> clazz, String filter) throws Exception {
		List<T> items = new ArrayList<T>();
		for (Number id : readListByClass(clazz, filter))
			items.add(getBO(clazz, id));
		return items;
	}

	public <T> List<T> getBOListByFilter(Class<T> clazz, BO bo, String filter) throws Exception {
		List<T> items = new ArrayList<T>();
		for (Number id : readListByFilter(bo.getClass(), bo, filter))
			items.add(getBO(clazz, id));
		return items;
	}

	public <T> List<T> exportBOList(BO bo, String filter) throws Exception {
		List<Bean> beans = exportList(bo.getClass(), bo, filter);
		List<T> items = new ArrayList<T>();
		for (Bean bean : beans)
			items.add((T) bean);
		return items;
	}

	public <T> List<T> exportBOListByClass(Class<T> clazz, String filter) throws Exception {
		List<Bean> beans = exportList(clazz, (BO) clazz.newInstance(), filter);
		List<T> items = new ArrayList<T>();
		for (Bean bean : beans)
			items.add((T) bean);
		return items;
	}

	public <T> List<T> exportBOListByFilter(Class<T> clazz, BO bo, String filter) throws Exception {
		List<Bean> beans = exportList(clazz, bo, filter);
		List<T> items = new ArrayList<T>();
		for (Bean bean : beans)
			items.add((T) bean);
		return items;
	}
}
