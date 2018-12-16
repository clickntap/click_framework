package com.clickntap.tool.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;

import com.clickntap.smart.SmartContext;
import com.clickntap.utils.XMLUtils;

public class UiCalendarManager {

	private Map<String, UICalendarConf> confMap;

	public void setConf(Resource conf) throws Exception {
		Document doc = XMLUtils.copyFrom(conf.getInputStream());
		confMap = new HashMap<String, UICalendarConf>();
		for (Element element : (List<Element>) doc.getRootElement().elements("uicalendar")) {
			confMap.put(element.attributeValue("name"), new UICalendarConf(element));
		}
	}

	public UICalendarConf getConf(String channel, SmartContext ctx) {
		UICalendarConf conf = confMap.get(channel);
		return conf;
	}

}
