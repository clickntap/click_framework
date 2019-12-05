package com.clickntap.tool.setup;

import org.springframework.jdbc.core.JdbcTemplate;

import com.clickntap.hub.BOManager;

public interface SetupStep {
	public String getDescription() throws Exception;

	public boolean isSkipable() throws Exception;

	public void init(JdbcTemplate db, BOManager app, String schema) throws Exception;

	public void setup() throws Exception;
}
