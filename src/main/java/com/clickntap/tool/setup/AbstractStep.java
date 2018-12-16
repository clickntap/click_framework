package com.clickntap.tool.setup;

import org.springframework.jdbc.core.JdbcTemplate;

import com.clickntap.hub.BOManager;

abstract class AbstractStep implements SetupStep {

	private JdbcTemplate db;
	private BOManager app;
	private String schema;

	public void init(JdbcTemplate db, BOManager app, String schema) throws Exception {
		this.db = db;
		this.app = app;
		this.schema = schema;
	}

	public JdbcTemplate getDb() {
		return db;
	}

	public BOManager getApp() {
		return app;
	}

	public String getSchema() {
		return schema;
	}

}
