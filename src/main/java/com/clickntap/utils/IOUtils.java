package com.clickntap.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.StringUtils;

public class IOUtils extends org.apache.commons.io.IOUtils {

	public static void copy(InputStream in, OutputStream out, int offset, int length) throws IOException {
		in.skip(offset);
		int c;
		while (length > 0 && (c = in.read()) != -1) {
			out.write(c);
			length--;
		}
	}

	public static String toString(File file) throws IOException {
		return slashize(file.getCanonicalPath());
	}

	private static String slashize(String path) throws IOException {
		return StringUtils.replace(path, ConstUtils.BACKSLASH, ConstUtils.SLASH);
	}
}
