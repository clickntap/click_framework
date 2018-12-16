package com.clickntap.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLUtils {

	public static void copyTo(Document doc, OutputStream out) throws UnsupportedEncodingException, IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(doc);
	}

	public static void copyTo(Document doc, File file) throws UnsupportedEncodingException, IOException {
		copyTo(doc, new FileOutputStream(file));
	}

	public static void copyTo(Document doc, String file) throws UnsupportedEncodingException, IOException {
		copyTo(doc, new File(file));
	}

	public static Document copyFrom(String file) throws DocumentException {
		return copyFrom(file, false);
	}

	public static Document copyFrom(File file) throws DocumentException, MalformedURLException {
		return copyFrom(file, false);
	}

	public static Document copyFrom(InputStream in) throws DocumentException, MalformedURLException {
		return copyFrom(in, false);
	}

	public static Document copyFrom(String fileName, boolean validating) throws DocumentException {
		return getReader(validating).read(fileName);
	}

	public static Document copyFrom(File file, boolean validating) throws DocumentException, MalformedURLException {
		return getReader(validating).read(file);
	}

	public static Document copyFrom(InputStream in, boolean validating) throws DocumentException {
		return getReader(validating).read(in);
	}

	private static SAXReader getReader(boolean validating) {
		SAXReader reader = new SAXReader();
		reader.setValidation(validating);
		return reader;
	}

	public static String toString(Element element) throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copyTo(element.getDocument(), out);
		return out.toString(ConstUtils.UTF_8);
	}

}
