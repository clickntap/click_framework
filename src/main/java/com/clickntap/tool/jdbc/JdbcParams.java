package com.clickntap.tool.jdbc;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.beans.BeanUtils;

import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

public class JdbcParams {

	private List<Object> params = new ArrayList<Object>();

	private Object object;
	private Timestamp timestamp;

	public JdbcParams(Object object) {
		this.object = object;
		this.timestamp = new Timestamp(System.currentTimeMillis());
	}

	public String get(String propertyName) throws Exception {
		Method readMethod;
		try {
			readMethod = BeanUtils.getPropertyDescriptor(object.getClass(), propertyName).getReadMethod();
		} catch (Exception e) {
			throw new Exception("the property '" + propertyName + "' does not exists in " + object.getClass());
		}
		try {
			Object value = readMethod.invoke(object, (Object[]) null);
			// if (value != null && value instanceof String) {
			// value = AsciiUtils.textToUtf7(value.toString());
			// }
			params.add(value);
			return ConstUtils.QUESTION_MARK;
		} catch (Exception e) {
			return ConstUtils.NULL;
		}
	}

	public String md5(String value) throws Exception {
		try {
			params.add(SecurityUtils.md5(value));
			return ConstUtils.QUESTION_MARK;
		} catch (Exception e) {
			return ConstUtils.NULL;
		}
	}

	public String toString(String param) {
		params.add(param);
		return ConstUtils.QUESTION_MARK;
	}

	public String toInputStream(String param) {
		params.add(new StringInputStream(param));
		return ConstUtils.QUESTION_MARK;
	}

	public String toTime(Datetime param) {
		params.add(new Timestamp(param.getTimeInMillis()));
		return ConstUtils.QUESTION_MARK;
	}

	public List<Object> getParams() {
		return params;
	}

	public Object[] toArray() {
		return params.toArray();
	}

	public void close() {
	}

	public Object getObject() {
		return object;
	}

	public Object getTarget() {
		return object;
	}

	public Object getBean() {
		return object;
	}

	public String now() {
		params.add(timestamp);
		return ConstUtils.QUESTION_MARK;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public Datetime getDate() {
		return new Datetime(timestamp);
	}

}
