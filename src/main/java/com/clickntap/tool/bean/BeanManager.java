package com.clickntap.tool.bean;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

public interface BeanManager {

	public Bean read(Number id, Class beanClass) throws Exception;

	public Bean readByFilter(Bean filter, String filterName, Class beanClass) throws Exception;

	public Number create(Bean bean) throws Exception;

	public int update(Bean bean) throws Exception;

	public int delete(Bean bean) throws Exception;

	public void batchUpdate(String[] sql) throws Exception;

	public List<Number> readList(Bean bean, String fieldName) throws Exception;

	public List<Number> readListByClass(Class beanClass, String fieldName) throws Exception;

	public List<Number> readListByFilter(Class beanClass, Bean filter, String fieldName) throws Exception;

	public void copyTo(Bean bean, OutputStream out) throws Exception;

	public void copyFrom(Bean bean, MultipartFile in) throws Exception;

	public InputStream stream(Bean bean) throws Exception;

	public int execute(Bean bean, String what) throws Exception;

	public List<Bean> exportList(Class beanClass, Bean bean, String fieldName) throws Exception;

	public Validator getValidator(Bean target, String validationGroup) throws Exception;

	public Object executeTransaction(TransactionCallback transactionCallback);

	public PlatformTransactionManager getTransactionManager();

	public void init() throws Exception;

}