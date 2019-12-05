package com.clickntap.tool.types;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

public class HttpFile implements MultipartFile {

	private InputStream inputStream;

	public HttpFile(String url) throws IOException {
		this(new URL(url));
	}

	public HttpFile(URL url) throws IOException {
		this.inputStream = url.openStream();
	}

	public byte[] getBytes() throws IOException {
		throw new RuntimeException();
	}

	public String getContentType() {
		throw new RuntimeException();
	}

	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	public String getName() {
		throw new RuntimeException();
	}

	public String getOriginalFilename() {
		throw new RuntimeException();
	}

	public long getSize() {
		throw new RuntimeException();
	}

	public boolean isEmpty() {
		return false;
	}

	public void transferTo(File arg0) throws IOException, IllegalStateException {
		throw new RuntimeException();
	}

}
