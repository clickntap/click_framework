package com.clickntap.autoarchive;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

public class RowMover implements RowMapper {
	private JdbcTemplate jdbc;
	private String table;
	private boolean archive;

	public RowMover(JdbcTemplate jdbc, String table, boolean archive) {
		this.jdbc = jdbc;
		this.table = table;
		this.archive = archive;
	}

	public Object mapRow(ResultSet rs, int row) throws SQLException {
		String tablePre = "";
		if (archive) {
			tablePre = "autoarchive_";
		}
		Number id = null;
		final List<Object> params = new ArrayList<Object>();
		String sql = "insert into " + tablePre + table + " (";
		for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			sql += "`" + rs.getMetaData().getColumnName(i) + "`";
			if (i != rs.getMetaData().getColumnCount()) {
				sql += ",";
			}
			params.add(rs.getObject(i));

			if (rs.getMetaData().getColumnName(i).equals("id")) {
				id = rs.getLong(i);
			}
		}
		sql += ") values (";
		for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			sql += "?";
			if (i != rs.getMetaData().getColumnCount()) {
				sql += ",";
			}
		}
		sql += ")";
		jdbc.update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				for (int i = 1; i <= params.size(); i++) {
					ps.setObject(i, params.get(i - 1));
				}
			}
		});
		return id;
	}
}