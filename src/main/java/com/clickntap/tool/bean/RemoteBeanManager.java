package com.clickntap.tool.bean;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.SerializerFactory;

public class RemoteBeanManager implements BeanManager {

	private String serviceUrl;
	private BeanManager remoteBeanManager;
	private SerializerFactory serializerFactory;

	public SerializerFactory getSerializerFactory() {
		return serializerFactory;
	}

	public void setSerializerFactory(SerializerFactory serializerFactory) {
		this.serializerFactory = serializerFactory;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public void init() throws Exception {
		HessianProxyFactory factory = new HessianProxyFactory();
		if (serializerFactory != null)
			factory.setSerializerFactory(serializerFactory);
		remoteBeanManager = (BeanManager) factory.create(BeanManager.class, serviceUrl);
	}

	public Bean read(Number id, Class beanClass) throws Exception {
		Bean bean = remoteBeanManager.read(id, beanClass);
		if (bean != null)
			bean.setBeanManager(this);
		return bean;
	}

	public Bean readByFilter(Bean filter, String filterName, Class beanClass) throws Exception {
		Bean bean = remoteBeanManager.readByFilter(filter, filterName, beanClass);
		if (bean != null)
			bean.setBeanManager(this);
		return bean;
	}

	public int update(Bean bean) throws Exception {
		return remoteBeanManager.update(bean);
	}

	public int execute(Bean bean, String what) throws Exception {
		return remoteBeanManager.execute(bean, what);
	}

	public Number create(Bean bean) throws Exception {
		return remoteBeanManager.create(bean);
	}

	public int delete(Bean bean) throws Exception {
		return remoteBeanManager.delete(bean);
	}

	public void copyFrom(Bean bean, MultipartFile in) throws Exception {
		remoteBeanManager.copyFrom(bean, in);
	}

	public List<Number> readList(Bean bean, String fieldName) throws Exception {
		return remoteBeanManager.readList(bean, fieldName);
	}

	public List<Number> readListByClass(Class beanClass, String fieldName) throws Exception {
		return remoteBeanManager.readListByClass(beanClass, fieldName);
	}

	public List<Number> readListByFilter(Class beanClass, Bean bean, String fieldName) throws Exception {
		return remoteBeanManager.readListByFilter(beanClass, bean, fieldName);
	}

	public List<Bean> exportList(Class beanClass, Bean bean, String fieldName) throws Exception {
		List<Bean> items = remoteBeanManager.exportList(beanClass, bean, fieldName);
		for (Bean item : items)
			item.setBeanManager(this);
		return items;
	}

	public void copyTo(Bean bean, OutputStream out) throws Exception {
		remoteBeanManager.copyTo(bean, out);
	}

	public InputStream stream(Bean bean) throws Exception {
		return remoteBeanManager.stream(bean);
	}

	public Validator getValidator(Bean target, String validationGroup) throws Exception {
		return remoteBeanManager.getValidator(target, validationGroup);
	}

	public void batchUpdate(String[] sql) throws Exception {
		remoteBeanManager.batchUpdate(sql);
	}

	public Object executeTransaction(TransactionCallback transactionCallback) {
		return remoteBeanManager.executeTransaction(transactionCallback);
	}

	public PlatformTransactionManager getTransactionManager() {
		return remoteBeanManager.getTransactionManager();
	}

}
