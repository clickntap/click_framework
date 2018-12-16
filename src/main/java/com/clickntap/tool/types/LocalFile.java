package com.clickntap.tool.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class LocalFile implements MultipartFile {

	private String name;
	private String contentType;
	private InputStream inputStream;
	private File file;

	public LocalFile(String name, String contentType, InputStream inputStream) {
		this.name = name;
		this.contentType = contentType;
		this.inputStream = inputStream;
	}

	public LocalFile(String name, String contentType, File file) {
		this.name = name;
		this.contentType = contentType;
		this.file = file;
	}

	public byte[] getBytes() throws IOException {
		throw new RuntimeException();
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() throws IOException {
		if (file != null)
			return new FileInputStream(file);
		return inputStream;
	}

	public String getName() {
		return name;
	}

	public String getOriginalFilename() {
		return name;
	}

	public long getSize() {
		try {
			return inputStream.available();
		} catch (IOException e) {
			return 0L;
		}
	}

	public boolean isEmpty() {
		throw new RuntimeException();
	}

	public void transferTo(File dest) throws IOException, IllegalStateException {
		throw new RuntimeException();
	}

}
