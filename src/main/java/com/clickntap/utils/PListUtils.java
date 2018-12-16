package com.clickntap.utils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanUtils;

public class PListUtils {

	public static List toArray(Element element) {
		List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		for (Element child : (List<Element>) element.elements()) {
			if (child.getName().equals("array")) {
				bindArray(array, child);
			}
		}
		return array;
	}

	public static Map toDictionary(Element element) {
		Map<String, Object> dict = new HashMap<String, Object>();
		for (Element child : (List<Element>) element.elements()) {
			if (child.getName().equals("dict")) {
				bindDictionary(dict, child);
			}
		}
		return dict;
	}

	private static void bindArray(List<Map<String, Object>> array, Element element) {
		for (Element child : (List<Element>) element.elements()) {
			if (child.getName().equals("dict")) {
				Map<String, Object> dict = new HashMap<String, Object>();
				bindDictionary(dict, child);
				array.add(dict);
			}
		}
	}

	private static void bindDictionary(Map<String, Object> dict, Element element) {
		String key = null;
		for (Element child : (List<Element>) element.elements()) {
			if (key != null) {
				if (child.getName().equals("string") || child.getName().equals("integer")) {
					dict.put(key, child.getTextTrim());
				}
				if (child.getName().equals("dict")) {
					Map<String, Object> dict2 = new HashMap<String, Object>();
					bindDictionary(dict2, child);
					dict.put(key, dict2);
				}
				if (child.getName().equals("array")) {
					List<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
					bindArray(array, child);
					dict.put(key, array);
				}
				key = null;
			}
			if (child.getName().equals("key")) {
				key = child.getTextTrim();
			}
		}
	}

	public static Element toPList(Map map) {
		Element root = createPListDocument();
		addValue(root, map);
		return root;
	}

	public static Element toPList(Object object) {
		Element root = createPListDocument();
		addValue(root, toMap(object));
		return root;
	}

	public static Element toPList(List list) {
		Element root = createPListDocument();
		addValue(root, list);
		return root;
	}

	private static Map toMap(Object anObject) {
		if (anObject instanceof Bean)
			return ((Bean) anObject).toMap();
		Map map = new HashMap();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(anObject.getClass());
		String name = null;
		for (PropertyDescriptor pd : pds)
			try {
				name = pd.getName();
				if (!"class".equals(name) && !"beanManager".equals(name) && !"app".equals(name)) {
					Object value = BeanUtils.getValue(anObject, name);
					if (value != null) {
						map.put(name, value);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return map;
	}

	public static Map toMap(Element dict) {
		Map map = new HashMap();
		String key = null;
		for (Element element : (List<Element>) dict.elements()) {
			if (element.getName().equals("key"))
				key = element.getText();
			else
				map.put(key, toValue(element));
		}
		return map;
	}

	private static Object toValue(Element element) {
		Object value;
		if (element.getName().equals("dict"))
			value = toMap(element);
		else if (element.getName().equals("array"))
			value = toList(element);
		else if (element.getName().equals("real"))
			value = Float.parseFloat(element.getText());
		else if (element.getName().equals("integer"))
			value = Long.parseLong(element.getText());
		else {
			String s = element.asXML();
			if (s.startsWith("<string>") && s.endsWith("</string>")) {
				value = s.substring(8, s.length() - 9);
			} else {
				value = element.getText();
			}
		}
		return value;
	}

	public static List toList(Element array) {
		List list = new ArrayList();
		for (Element element : (List<Element>) array.elements())
			list.add(toValue(element));
		return list;
	}

	private static void addProperty(Element parent, String key, Object value) {
		if (!key.equals("app")) {
			parent.addElement("key").setText(key);
			addValue(parent, value);
		}
	}

	private static void addValue(Element parent, Object value) {
		if (value instanceof List) {
			Element array = parent.addElement("array");
			for (Object o : (List<Object>) value)
				addValue(array, o);
		} else if (value instanceof Map) {
			Map map = (Map) value;
			Element dictionary = parent.addElement("dict");
			for (Iterator it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next().toString();
				addProperty(dictionary, key, map.get(key));
			}
		} else if (value instanceof Bean)
			addValue(parent, ((Bean) value).toMap());
		else if (value instanceof Long || value instanceof Integer)
			addValue(parent, value, "integer");
		else if (value instanceof Double)
			addValue(parent, value, "real");
		else
			addValue(parent, value, "string");
	}

	private static void addValue(Element parent, Object value, String type) {
		String text = value != null ? value.toString() : ConstUtils.EMPTY;
		if (text.contains("\n")) {
			CDATA cdata = DocumentHelper.createCDATA(text);
			parent.addElement(type).add(cdata);
		} else {
			parent.addElement(type).setText(text);
		}
	}

	public static Element createPListDocument() {
		Element root = DocumentHelper.createDocument().addElement("plist");
		root.addAttribute("version", "1.0");
		return root;
	}
}
