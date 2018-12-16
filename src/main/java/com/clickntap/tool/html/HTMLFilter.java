package com.clickntap.tool.html;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class HTMLFilter {
	private static final String TAG_KEY = "tag";
	private static final String NAME_KEY = "name";
	private Map tagMap;
	private String name;

	public HTMLFilter() throws Exception {
	}

	public HTMLFilter(String confFile) throws Exception {
		setConfFile(confFile);
	}

	public HTMLFilter(InputStream in) throws Exception {
		setConfFile(in);
	}

	public HTMLTag getTag(String tag) throws Exception {
		return tagMap.containsKey(tag) ? (HTMLTag) tagMap.get(tag) : new HTMLTag(tag);
	}

	public void setConfFile(String confFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(confFile);
		init(doc);
	}

	public void setConfFile(InputStream confFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(confFile);
		init(doc);
	}

	public void init(Document doc) throws Exception {
		tagMap = new HashMap();
		List elements = doc.getRootElement().elements(TAG_KEY);
		name = doc.getRootElement().attributeValue(NAME_KEY);
		Element element;
		for (int i = 0; i < elements.size(); i++) {
			element = (Element) elements.get(i);
			tagMap.put(element.attributeValue(NAME_KEY), new HTMLTag(element, element.attributeValue(NAME_KEY)));
		}
	}

	public String getName() {
		return name;
	}
}