package com.clickntap.smart;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.springframework.util.StringUtils;

public class SmartAction {

	public String target;

	private List<BindingElement> loads;
	private List<BindingElement> binds;
	private List<String> inits;
	private List<String> execs;
	private List<String> rules;
	private List<String> elses;

	public SmartAction(Element actionElement) {
		target = actionElement.attributeValue("target");
		List<Element> initElements = actionElement.elements("init");
		inits = new ArrayList<String>(initElements.size());
		for (Element initElement : initElements)
			inits.add(initElement.getTextTrim());
		List<Element> bindElements = actionElement.elements("bind");
		binds = new ArrayList<BindingElement>(bindElements.size());
		for (Element bindElement : bindElements)
			binds.add(new BindingElement(bindElement));
		List<Element> loadElements = actionElement.elements("load");
		loads = new ArrayList<BindingElement>(loadElements.size());
		for (Element loadElement : loadElements)
			loads.add(new BindingElement(loadElement));
		List<Element> execElements = actionElement.elements("exec");
		execs = new ArrayList<String>(execElements.size());
		for (Element execElement : execElements)
			execs.add(execElement.getTextTrim());
		List<Element> ruleElements = actionElement.elements("rule");
		rules = new ArrayList<String>(ruleElements.size());
		for (Element ruleElement : ruleElements)
			rules.add(ruleElement.getTextTrim());
		List<Element> elseElements = actionElement.elements("else");
		elses = new ArrayList<String>(elseElements.size());
		for (Element elseElement : elseElements)
			elses.add(elseElement.getTextTrim());
	}

	public String getTarget() {
		return target;
	}

	public List<String> getInits() {
		return inits;
	}

	public List<String> getExecs() {
		return execs;
	}

	public List<String> getRules() {
		return rules;
	}

	public List<BindingElement> getBinds() {
		return binds;
	}

	public List<BindingElement> getLoads() {
		return loads;
	}

	public List<String> getElses() {
		return elses;
	}

	public class BindingElement {
		private String objectClass;
		private String objectName;
		private String[] allowedFields;
		private String[] disallowedFields;
		private String scope;
		private String channel;
		private String validationGroup;
		private String script;

		public BindingElement(Element element) {
			this.objectClass = element.attributeValue("class");
			this.objectName = element.attributeValue("name");
			this.allowedFields = StringUtils.commaDelimitedListToStringArray(element.attributeValue("allowedFields"));
			this.disallowedFields = StringUtils.commaDelimitedListToStringArray(element.attributeValue("disallowedFields"));
			this.scope = element.attributeValue("scope");
			this.channel = element.attributeValue("channel");
			this.validationGroup = element.attributeValue("validation-group");
			this.script = element.getTextTrim();
		}

		public String getScript() {
			return script;
		}

		public String getValidationGroup() {
			return validationGroup;
		}

		public String getObjectClass() {
			return objectClass;
		}

		public String getObjectName() {
			return objectName;
		}

		public String[] getAllowedFields() {
			return allowedFields;
		}

		public String[] getDisallowedFields() {
			return disallowedFields;
		}

		public String getChannel(String context) {
			return channel + context;
		}

		public String getScope() {
			return scope;
		}
	}
}
