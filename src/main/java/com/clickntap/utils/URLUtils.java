package com.clickntap.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class URLUtils {

	public static String getSource(String url, String charsetName) throws Exception {
		if (url.startsWith("//")) {
			url = "http:" + url;
			
		}
		String source = null;
		InputStream in = new URL(url).openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		source = out.toString(charsetName);
		out.close();
		in.close();
		return source;
	}

	public static void download(String url, File file) throws Exception {
		InputStream in = new URL(url).openStream();
		FileOutputStream out = new FileOutputStream(file);
		IOUtils.copy(in, out);
		out.close();
		in.close();
	}

	public static String getSource(String url) throws Exception {
		return getSource(url, ConstUtils.UTF_8);
	}
}
