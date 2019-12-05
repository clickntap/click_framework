package com.clickntap.tool.html;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.springframework.util.StringUtils;

public class HTMLTag {
	public static final String NAME_KEY = "name";
	public static final String ATTRIBUTE_KEY = "attribute";
	private boolean eolBeforeStartTag;
	private boolean eolAfterStartTag;
	private boolean eolBeforeEndTag;
	private boolean eolAfterEndTag;
	private boolean enabled;
	private String name;
	private Map attributeMap;

	public HTMLTag(Element element, String name) {
		this(name);
		enabled = true;
		String value = element.attributeValue("eol");
		eolBeforeStartTag = false;
		eolAfterStartTag = false;
		eolBeforeEndTag = false;
		eolAfterEndTag = false;

		if (value != null) {
			String[] eol = StringUtils.commaDelimitedListToStringArray(value);
			if (eol.length == 4) {
				eolBeforeStartTag = eol[0].equals("1");
				eolAfterStartTag = eol[1].equals("1");
				eolBeforeEndTag = eol[2].equals("1");
				eolAfterEndTag = eol[3].equals("1");
			}
		}
		List elements = element.elements(ATTRIBUTE_KEY);
		Element attribute;
		for (int i = 0; i < elements.size(); i++) {
			attribute = (Element) elements.get(i);
			attributeMap.put(attribute.attributeValue(NAME_KEY), new HTMLAttribute(attribute, attribute.attributeValue(NAME_KEY)));
		}
	}

	public HTMLTag(String name) {
		this.name = name;
		enabled = false;
		eolBeforeStartTag = false;
		eolAfterStartTag = false;
		eolBeforeEndTag = false;
		eolAfterEndTag = false;
		attributeMap = new HashMap();
	}

	public boolean isEolBeforeStartTag() throws Exception {
		return eolBeforeStartTag;
	}

	public boolean isEolAfterStartTag() throws Exception {
		return eolAfterStartTag;
	}

	public boolean isEolBeforeEndTag() throws Exception {
		return eolBeforeEndTag;
	}

	public boolean isEolAfterEndTag() throws Exception {
		return eolAfterEndTag;
	}

	public boolean isEnabled() throws Exception {
		return enabled;
	}

	public HTMLAttribute getAttribute(Object name) throws Exception {
		return attributeMap.containsKey(name.toString()) ? (HTMLAttribute) attributeMap.get(name.toString()) : new HTMLAttribute(name.toString());
	}

	public String getName() throws Exception {
		return name;
	}
}