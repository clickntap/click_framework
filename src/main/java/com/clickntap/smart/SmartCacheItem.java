package com.clickntap.smart;

import java.io.OutputStream;
import java.io.Serializable;

public class SmartCacheItem implements Serializable {

	private String contentType;
	private long lastModified;
	private byte[] data;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void copyTo(OutputStream out) throws Exception {
		out.write(data);
	}
}
