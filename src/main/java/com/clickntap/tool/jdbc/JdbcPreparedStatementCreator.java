package com.clickntap.tool.jdbc;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.web.multipart.MultipartFile;

import com.clickntap.tool.types.Datetime;

public class JdbcPreparedStatementCreator implements PreparedStatementSetter {

	private Object[] values;

	public JdbcPreparedStatementCreator(Object[] values) {
		this.values = values;
	}

	public void setValues(PreparedStatement ps) throws SQLException {
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MultipartFile) {
				MultipartFile file = (MultipartFile) values[i];
				try {
					ps.setBinaryStream(i + 1, file.getInputStream(), (int) file.getSize());
				} catch (IOException e) {
					throw new SQLException(e.getMessage());
				}
			} else if (values[i] instanceof Datetime) {
				ps.setTimestamp(i + 1, new Timestamp(((Datetime) values[i]).getTimeInMillis()));
			} else
				ps.setObject(i + 1, values[i]);
		}
	}
}
