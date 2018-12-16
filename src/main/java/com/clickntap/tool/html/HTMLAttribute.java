package com.clickntap.tool.html;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.clickntap.utils.ConstUtils;

public class HTMLAttribute {
	private static final String STYLE_KEY = "style";
	private String name;
	private boolean enabled;
	private Map attributeMap;

	public HTMLAttribute(Element element, String name) {
		this(name);
		enabled = true;
		List elements = element.elements(HTMLTag.ATTRIBUTE_KEY);
		Element attribute;
		for (int i = 0; i < elements.size(); i++) {
			attribute = (Element) elements.get(i);
			attributeMap.put(attribute.attributeValue(HTMLTag.NAME_KEY), new HTMLAttribute(attribute, attribute.attributeValue(HTMLTag.NAME_KEY)));
		}
	}

	public HTMLAttribute(String name) {
		attributeMap = new HashMap();
		this.name = name;
		enabled = false;
	}

	public boolean isEnabled() throws Exception {
		return enabled;
	}

	public boolean isStyle() throws Exception {
		return name.equals(STYLE_KEY);
	}

	public boolean isColor() throws Exception {
		return false;
	}

	public String parseStyle(String style) throws Exception {
		int x = style.indexOf(ConstUtils.COLON);
		if (x > 0) {
			String name = style.substring(0, x).trim();
			if (attributeMap.containsKey(name))
				return style + ConstUtils.SEMICOLON;
		}
		return ConstUtils.EMPTY;
	}

	public String parseColor(String color) throws Exception {
		return null;
	}

	public String getName() throws Exception {
		return name;
	}
}