package com.clickntap.tool.setup;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.clickntap.hub.BOManager;
import com.clickntap.utils.XMLUtils;

public class Setup {

	private BOManager app;
	private JdbcTemplate db;
	private String schema;
	private List<SetupStep> steps;

	public Setup() {
		steps = new ArrayList<SetupStep>();
	}

	public void setResource(Resource resource) throws Exception {
		Document doc = XMLUtils.copyFrom(resource.getInputStream());
		for (Element element : (List<Element>) doc.getRootElement().elements("db")) {
			steps.add(new DatabaseStep(element));
		}
	}

	public void setApp(BOManager app) {
		this.app = app;
	}

	public void setDb(JdbcTemplate db) {
		this.db = db;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		int x = schema.lastIndexOf('/');
		if (x > 0) {
			schema = schema.substring(x + 1);
		}
		this.schema = schema;
	}

	public void setup() throws Exception {
		db.execute("select LAST_INSERT_ID() as id");
		for (SetupStep step : steps) {
			step.init(db, app, schema);
			if (!step.isSkipable()) {
				System.out.println(step.getDescription());
				step.setup();
			}
		}
	}
}
