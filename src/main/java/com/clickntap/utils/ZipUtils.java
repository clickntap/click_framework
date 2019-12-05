package com.clickntap.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class ZipUtils {

	public static void unzip(String source, String destination, String password) throws ZipException {
		ZipFile zipFile = new ZipFile(source);
		if (zipFile.isEncrypted()) {
			zipFile.setPassword(password);
		}
		zipFile.extractAll(destination);
	}

	public static void unzip(InputStream in, String destination) throws Exception {
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry;
		while ((entry = zipIn.getNextEntry()) != null) {
			if (!entry.isDirectory()) {
				File outFile = new File(destination + "/" + entry.getName());
				outFile.getParentFile().mkdirs();
				FileOutputStream out = new FileOutputStream(outFile);
				IOUtils.copy(zipIn, out);
				out.close();
			}
		}
		zipIn.close();
	}

	public static void unzip(String source, String destination) throws ZipException {
		unzip(source, destination, null);
	}

	public static void zipFolder(File folder, File destination) throws ZipException {
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		ZipFile zipFile = new ZipFile(destination);
		zipFile.addFolder(folder, parameters);
	}
}
