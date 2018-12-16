package com.clickntap.tool.bean;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

public class ProxyBeanManager implements BeanManager {

	private BeanManager beanManager;

	public void setBeanManager(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public Bean read(Number id, Class beanClass) throws Exception {
		Bean bean = beanManager.read(id, beanClass);
		if (bean != null)
			bean.setBeanManager(this);
		return bean;
	}

	public Bean readByFilter(Bean filter, String filterName, Class beanClass) throws Exception {
		Bean bean = beanManager.readByFilter(filter, filterName, beanClass);
		if (bean != null)
			bean.setBeanManager(this);
		return bean;
	}

	public int update(Bean bean) throws Exception {
		return beanManager.update(bean);
	}

	public int execute(Bean bean, String what) throws Exception {
		return beanManager.execute(bean, what);
	}

	public Number create(Bean bean) throws Exception {
		return beanManager.create(bean);
	}

	public int delete(Bean bean) throws Exception {
		return beanManager.delete(bean);
	}

	public void copyFrom(Bean bean, MultipartFile in) throws Exception {
		beanManager.copyFrom(bean, in);
	}

	public List<Number> readList(Bean bean, String fieldName) throws Exception {
		return beanManager.readList(bean, fieldName);
	}

	public List<Number> readListByClass(Class beanClass, String fieldName) throws Exception {
		return beanManager.readListByClass(beanClass, fieldName);
	}

	public List<Number> readListByFilter(Class beanClass, Bean bean, String fieldName) throws Exception {
		return beanManager.readListByFilter(beanClass, bean, fieldName);
	}

	public List<Bean> exportList(Class beanClass, Bean bean, String fieldName) throws Exception {
		List<Bean> items = beanManager.exportList(beanClass, bean, fieldName);
		for (Bean item : items)
			item.setBeanManager(this);
		return items;
	}

	public void copyTo(Bean bean, OutputStream out) throws Exception {
		beanManager.copyTo(bean, out);
	}

	public InputStream stream(Bean bean) throws Exception {
		return beanManager.stream(bean);
	}

	public Validator getValidator(Bean target, String validationGroup) throws Exception {
		return beanManager.getValidator(target, validationGroup);
	}

	public void batchUpdate(String[] sql) throws Exception {
		beanManager.batchUpdate(sql);
	}

	public Object executeTransaction(TransactionCallback transactionCallback) {
		return beanManager.executeTransaction(transactionCallback);
	}

	public PlatformTransactionManager getTransactionManager() {
		return beanManager.getTransactionManager();
	}

	public void init() throws Exception {
		beanManager.init();
	}

}
