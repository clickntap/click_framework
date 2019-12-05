package com.clickntap.tool.calendar;

import org.dom4j.Element;

import com.clickntap.smart.SmartContext;

public class UICalendarConf {

	private String loadScript;
	private String saveScript;
	private String defaultStartTime;
	private String defaultEndTime;
	private String startParam;
	private String endParam;
	private String type;

	public UICalendarConf(Element element) throws Exception {
		loadScript = element.elementText("load");
		saveScript = element.elementText("save");
		defaultStartTime = element.attributeValue("defaultTimeStart");
		defaultEndTime = element.attributeValue("defaultTimeEnd");
		startParam = element.attributeValue("paramStart");
		endParam = element.attributeValue("paramEnd");
		type = element.attributeValue("type");
	}

	public String getStartParam() {
		return startParam;
	}

	public String getEndParam() {
		return endParam;
	}

	public String getLoadScript() {
		return loadScript;
	}

	public String getSaveScript() {
		return saveScript;
	}

	public String getDefaultStartTime() {
		return defaultStartTime;
	}

	public String getDefaultEndTime() {
		return defaultEndTime;
	}

	public String getType() {
		return type;
	}

	public void load(SmartContext ctx) throws Exception {
		ctx.eval(getLoadScript());
	}

	public void save(SmartContext ctx) throws Exception {
		ctx.eval(getSaveScript());
	}

}
