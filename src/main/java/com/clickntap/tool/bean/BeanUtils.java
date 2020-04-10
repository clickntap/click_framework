package com.clickntap.tool.bean;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class BeanUtils extends org.springframework.beans.BeanUtils {

	public static String beanClassToResourceName(Class beanClass, String extension) {
		String simpleName = beanClassToSimpleName(beanClass);
		return simpleName + extension;
	}

	public static String beanClassToSimpleName(Class beanClass) {
		String simpleName = beanClass.getSimpleName();
		return simpleName;
	}

	public static Document beanToResource(Class beanClass) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = null;
		InputStream in = null;
		try {
			doc = reader.read(beanClass.getResourceAsStream(BeanUtils.beanClassToResourceName(beanClass, ".xml")));
		} catch (Exception e1) {
			try {
				in = beanClass.getResourceAsStream(BeanUtils.beanClassToResourceName(beanClass, ".ftl"));
				if (in != null)
					doc = reader.read(in);
				else
					return null;
			} catch (Exception e2) {
				return null;
			}
		}
		return doc;
	}

	public static BeanInfo beanTobeanInfo(Class beanClass) throws Exception {
		BeanInfo beanInfo = null;
		List<Class> classHierarchy = new ArrayList<Class>();
		Class superClass = beanClass;
		while (!superClass.toString().equals(Bean.class.toString())) {
			classHierarchy.add(0, superClass);
			superClass = superClass.getSuperclass();
		}
		for (Class clazz : classHierarchy) {
			Document document = BeanUtils.beanToResource(clazz);
			if (document != null)
				beanInfo = new BeanInfo(document, BeanUtils.beanClassToSimpleName(clazz), beanInfo);
		}
		return beanInfo;
	}

	public static Object getValue(Object target, String propertyName) {
		int x = 0;
		if ((x = propertyName.indexOf(".")) >= 0) {
			Object newTarget = getValue(target, propertyName.substring(0, x));
			return getValue(newTarget, propertyName.substring(x + 1));
		} else if ((x = propertyName.indexOf("[")) >= 0) {
			int x1 = propertyName.indexOf("]");
			int index = Integer.parseInt(propertyName.substring(x + 1, x1));
			List list = (List) getValue(target, propertyName.substring(0, x));
			return list.get(index);
		}
		Object value = null;
		PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(target.getClass(), propertyName);
		try {
			value = propertyDescriptor.getReadMethod().invoke(target, (Object[]) null);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return value;
	}

	public static void setValue(Object target, String propertyName, Object value) {
		PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(target.getClass(), propertyName);
		try {
			propertyDescriptor.getWriteMethod().invoke(target, new Object[] { value });
		} catch (Exception e) {
		}
	}
}
