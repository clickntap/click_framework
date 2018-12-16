package com.clickntap.tool.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.clickntap.utils.IOUtils;

public class JdbcOutputStreamRowMapper implements RowMapper {

	private OutputStream outputStream;

	private int offset = 0;
	private int length = 0;

	public JdbcOutputStreamRowMapper(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public JdbcOutputStreamRowMapper(OutputStream outputStream, int offset, int length) {
		this(outputStream);
		this.offset = offset;
		this.length = length;
	}

	public Object mapRow(ResultSet rs, int i) throws SQLException {
		InputStream inputStream = rs.getBinaryStream(1);
		if (rs.wasNull())
			return null;
		try {
			if (length > 0)
				IOUtils.copy(inputStream, outputStream, offset, length);
			else
				IOUtils.copy(inputStream, outputStream);
			inputStream.close();
		} catch (IOException e) {
		}
		return null;
	}
}
