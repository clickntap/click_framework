package com.clickntap.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class Robocopy {

	public static void main(String[] args) throws Exception {
		new Robocopy().sync(args[0], args[1]);
		new Robocopy().clean(args[0], args[1]);
	}

	public void clean(String fromPath, String toPath) throws Exception {
		File fromFile = new File(fromPath);
		File toFile = new File(toPath);
		for (File file : fromFile.listFiles()) {
			String relativePath = file.getAbsolutePath().substring(fromFile.getAbsolutePath().length());
			File destFile = new File(toFile.getAbsolutePath() + relativePath);
			if (file.isFile()) {
				cleanFile(file, destFile);
			} else {
				if (!file.getName().startsWith(".")) {
					clean(file.getAbsolutePath(), destFile.getAbsolutePath());
					if (!destFile.exists()) {
						file.delete();
					}
				}
			}
		}
	}

	private void cleanFile(File file, File toFile) throws Exception {
		if (file.getName().startsWith("."))
			return;
		if (!toFile.exists()) {
			System.out.println(" delete --> " + file);
			file.delete();
		}
	}

	public void sync(String fromPath, String toPath) throws Exception {
		File fromFile = new File(fromPath);
		File toFile = new File(toPath);
		for (File file : fromFile.listFiles()) {
			String relativePath = file.getAbsolutePath().substring(fromFile.getAbsolutePath().length());
			File destFile = new File(toFile.getAbsolutePath() + relativePath);
			if (file.isFile()) {
				syncFile(file, destFile);
			} else {
				if (!file.getName().startsWith(".")) {
					destFile.mkdirs();
					sync(file.getAbsolutePath(), destFile.getAbsolutePath());
				}
			}
		}
	}

	private void syncFile(File file, File toFile) throws Exception {
		if (file.getName().startsWith("."))
			return;
		try {
			if (checksum(file) == checksum(toFile)) {
				return;
			}
		} catch (Exception e) {
		}
		System.out.println(file + " -- sync --> " + toFile);
		InputStream in = new FileInputStream(file);
		OutputStream out = new FileOutputStream(toFile);
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		in.close();
		out.close();
	}

	private long checksum(File file) throws Exception {
		CheckedInputStream in = new CheckedInputStream(new FileInputStream(file), new CRC32());
		byte[] buffer = new byte[1024];
		while (in.read(buffer) >= 0)
			;
		long value = in.getChecksum().getValue();
		in.close();
		return value;
	}

}
