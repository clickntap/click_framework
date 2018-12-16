package com.clickntap.tool.mail;

public class Body {

	private String contentType;
	private String content;

	public Body(String content, String contentType) {
		this.content = content;
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
