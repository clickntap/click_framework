package com.clickntap.tool.bean;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.clickntap.tool.jdbc.JdbcManager;

public class BeanCreator implements TransactionCallback {
	private Object bean;

	private BeanInfo beanInfo;

	private JdbcManager jdbcManager;

	public BeanCreator(Object bean, BeanInfo beanInfo, JdbcManager jdbcManager) {
		this.bean = bean;
		this.beanInfo = beanInfo;
		this.jdbcManager = jdbcManager;
	}

	public Object doInTransaction(TransactionStatus status) {
		jdbcManager.updateScript(beanInfo.getCreateScript(), bean);
		String currValScript = beanInfo.getCurrValScript();
		if (currValScript != null)
			return jdbcManager.queryScriptForLong(currValScript, bean);
		else
			return null;
	}
}
