package com.clickntap.tool.bean;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public class Bean extends BeanId {
	transient private BeanManager beanManager;

	public Bean() {
	}

	public Bean(BeanId id) {
		setId(id.getId());
	}

	public Bean(Number id) {
		setId(id);
	}

	public BeanManager getBeanManager() throws Exception {
		return beanManager;
	}

	public void setBeanManager(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public void copyTo(OutputStream out) throws Exception {
		getBeanManager().copyTo(this, out);
	}

	public void copyFrom(MultipartFile in) throws Exception {
		getBeanManager().copyFrom(this, in);
	}

	public InputStream stream() throws Exception {
		return getBeanManager().stream(this);
	}

	synchronized public void read() throws Exception {
		Object source = null;
		if (getId() != null && getBeanManager() != null)
			source = getBeanManager().read(this.getId(), this.getClass());
		if (source == this)
			return;
		clear();
		if (source != null)
			BeanUtils.copyProperties(source, this);
		setupAfterRead();
	}

	public void setupAfterRead() throws Exception {

	}

	public void setupBeforeUpdate() throws Exception {

	}

	synchronized public void read(String filter) throws Exception {
		Object source = null;
		if (filter != null && getBeanManager() != null)
			source = getBeanManager().readByFilter(this, filter, this.getClass());
		if (source == this)
			return;
		clear();
		if (source != null)
			BeanUtils.copyProperties(source, this);
	}

	synchronized public Bean clone() {
		Bean bean = null;
		try {
			bean = this.getClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		BeanUtils.copyProperties(this, bean);
		return bean;
	}

	public Number create() throws Exception {
		setId(getBeanManager().create(this));
		return getId();
	}

	public void createOrUpdate() throws Exception {
		Bean bean = getBeanManager().readByFilter(this, "exists", this.getClass());
		if (bean == null) {
			setId(create());
		} else {
			setId(bean.getId());
			update();
		}
	}

	public Number update() throws Exception {
		return getBeanManager().update(this);
	}

	public Number delete() throws Exception {
		return getBeanManager().delete(this);
	}

	public Number execute(String what) throws Exception {
		return getBeanManager().execute(this, what);
	}

	synchronized public void clear() throws Exception {
		BeanManager beanManager = getBeanManager();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(this.getClass());
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null)
				BeanUtils.setValue(this, pd.getName(), null);
		}
		setBeanManager(beanManager);
	}

	public Object get(String propertyName) {
		return BeanUtils.getValue(this, propertyName);
	}

	public Map toMap(String[] allowedFields, String[] disallowedFields) {
		Map map = new HashMap();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(this.getClass());
		for (PropertyDescriptor pd : pds)
			try {
				String name = pd.getName();
				if (isAllowed(name, allowedFields) && !isDisallowed(name, disallowedFields)) {
					Object value = BeanUtils.getValue(this, name);
					if (value != null)
						map.put(name, value);
				}
			} catch (Exception e) {
			}
		return map;
	}

	private boolean isAllowed(String name, String[] allowedFields) {
		if (allowedFields == null)
			return true;
		for (String field : allowedFields) {
			if (field.equals(name))
				return true;
		}
		return false;
	}

	private boolean isDisallowed(String name, String[] disallowedFields) {
		return isAllowed(name, disallowedFields);
	}

	public Map toMap() {
		return toMap(null, new String[] { "class", "beanManager" });
	}

}
