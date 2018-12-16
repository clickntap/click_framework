package com.clickntap.tool.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

public class Disk {

	private static final String MASTER = "master";
	private int DIR_NAME_MAX_LENGHT = 2;
	private File workFile;
	private String volumeName;
	private String dir;

	public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}

	public void setWorkDir(Resource workDir) throws IOException {
		this.workFile = workDir.getFile();
	}

	public void setWorkFile(File workFile) {
		this.workFile = workFile;
	}

	public void start() throws Exception {
		File dir = workFile;
		if (!dir.isDirectory())
			throw new IOException("workDir must be a directory");
		FileUtils.forceMkdir(dir = new File(dir.getCanonicalPath() + ConstUtils.SLASH + volumeName));
		this.dir = dir.getCanonicalPath() + ConstUtils.SLASH;
	}

	public void store(Number id, InputStream in) throws Exception {
		store(id, MASTER, in);
	}

	public boolean exists(Number id) throws Exception {
		return exists(id, MASTER);
	}

	public void load(Number id, OutputStream out) throws Exception {
		load(id, MASTER, out);
	}

	public void delete(Number id) throws Exception {
		delete(id, MASTER);
	}

	public InputStream stream(Number id) throws Exception {
		return inputStream(id, MASTER);
	}

	public void store(Number id, String key, InputStream in) throws Exception {
		ByteArrayOutputStream bin = new ByteArrayOutputStream();
		IOUtils.copy(in, bin);
		CRC32 crc = new CRC32();
		crc.update(bin.toByteArray());
		OutputStream out = outputStream(id, key, crc.getValue());
		out.write(bin.toByteArray());
		out.close();
	}

	public InputStream inputStream(Number id, String key) throws Exception {
		return new FileInputStream(getFile(id, key));
	}

	public OutputStream outputStream(Number id, String key, long crc) throws Exception {
		File file = getFile(id, key);
		FileUtils.forceMkdir(file.getParentFile());
		return new FileOutputStream(getFile(id, key, crc));
	}

	public void load(Number id, String key, OutputStream out) throws Exception {
		InputStream in = inputStream(id, key);
		IOUtils.copy(in, out);
		in.close();
	}

	public boolean exists(Number id, String key) throws Exception {
		// System.out.println(getFile(id, key));
		return getFile(id, key).exists();
	}

	public void delete(Number id, String key) throws Exception {
		if (key.equals(MASTER)) {
			int x1;
			String filen;
			File parentFile = getFile(id, key).getParentFile();
			File[] files = parentFile.listFiles();
			if (files != null)
				for (File file : files) {
					x1 = file.getName().indexOf(ConstUtils.DOT);
					filen = file.getName().substring(0, x1);
					if (filen.equals(Long.toString(id.longValue())))
						file.delete();
				}
			files = parentFile.listFiles();
			if (files == null || files.length == 0)
				parentFile.delete();
		} else
			getFile(id, key).delete();
	}

	private File getFile(Number id, String key) {
		return new File(dir + toPath(Long.toString(id.longValue()), key, null));
	}

	private File getFile(Number id, String key, Long crc) {
		return new File(dir + toPath(Long.toString(id.longValue()), key, crc));
	}

	private String toPath(String n, String key, Long crc) {
		key = ConstUtils.MINUS + key;
		String path = ConstUtils.EMPTY;
		for (int i = 0; i < n.length(); i += DIR_NAME_MAX_LENGHT) {
			if (i != 0)
				path += ConstUtils.SLASH;
			if (i == n.length() - 1)
				path += n;
			else {
				if (i + DIR_NAME_MAX_LENGHT >= n.length())
					path += n;
				else
					path += n.substring(i, Math.min(i + DIR_NAME_MAX_LENGHT, n.length()));
			}
		}
		if (crc != null)
			path += ConstUtils.DOT + crc;
		else {
			int x1, x2;
			String filen, filekey;
			File parentFile = new File(dir + path).getParentFile();
			File[] files = parentFile.listFiles();
			if (files != null)
				for (File file : files) {
					if (!file.isDirectory()) {
						x1 = file.getName().indexOf(ConstUtils.DOT);
						x2 = file.getName().indexOf(ConstUtils.MINUS);
						filen = file.getName().substring(0, x1);
						filekey = file.getName().substring(x2);
						if (filen.equals(n) && filekey.equals(key))
							path += file.getName().substring(x1, x2);
					}
				}
		}
		path += key;
		return path;
	}
}
