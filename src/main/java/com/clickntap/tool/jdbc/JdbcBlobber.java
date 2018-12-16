package com.clickntap.tool.jdbc;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface JdbcBlobber {
	public void update(Object object, MultipartFile multipartFile, List<String> scripts, JdbcManager jdbcManager) throws Exception;
}
