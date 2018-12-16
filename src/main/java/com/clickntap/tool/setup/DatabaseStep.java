package com.clickntap.tool.setup;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class DatabaseStep extends AbstractStep {

	private String description;
	private String sqlSkip;
	private String preAction;
	private String postAction;
	private List<String> sqlSetup;

	public DatabaseStep(Element step) {
		this.description = step.attributeValue("name");
		this.preAction = step.attributeValue("pre-action");
		this.postAction = step.attributeValue("post-action");
		try {
			this.sqlSkip = step.element("sql-skip").getTextTrim();
		} catch (Exception e) {
			this.sqlSkip = null;
		}
		this.sqlSetup = new ArrayList<String>();
		for (Element element : (List<Element>) step.elements("sql")) {
			sqlSetup.add(element.getTextTrim());
		}
	}

	public String getPostAction() {
		return postAction;
	}

	public void setPostAction(String postAction) {
		this.postAction = postAction;
	}

	public String getPreAction() {
		return preAction;
	}

	public void setPreAction(String preAction) {
		this.preAction = preAction;
	}

	public String getDescription() {
		return description;
	}

	public boolean isSkipable() throws Exception {
		if (sqlSkip == null || sqlSkip.isEmpty())
			return false;
		long count = getDb().queryForObject(sqlSkip.replaceAll("SCHEMA", getSchema()), Number.class).intValue();
		return count > 0;
	}

	public void setup() throws Exception {
		if (sqlSetup.size() > 0) {
			getDb().batchUpdate(sqlSetup.toArray(new String[sqlSetup.size()]));
		}
	}
}
