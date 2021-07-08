package com.clickntap.platform;

import java.io.File;
import java.io.InputStream;

import org.json.JSONObject;

public class UploadFile {
	private String originalFilename;
	private String contentType;
	private long contentLength;
	private JSONObject info;

	public UploadFile(JSONObject info) throws Exception {
		this.originalFilename = info.getString("fileName");
		this.contentType = info.getString("fileType");
		this.contentLength = info.getLong("fileSize");
		this.info = info;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public JSONObject getInfo() {
		return info;
	}

	public static File tmpFile(JSONObject fileInfo, int i) throws Exception {
		return new File("/tmp/" + fileInfo.get("id") + "_" + i);

	}

	public InputStream getInputStream() throws Exception {
		return new UploadInputStream(info);
	}

	public long getContentLength() {
		return contentLength;
	}

}
