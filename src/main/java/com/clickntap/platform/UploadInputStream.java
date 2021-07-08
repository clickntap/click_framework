package com.clickntap.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class UploadInputStream extends InputStream {

	private JSONObject info;
	private long fileSize;
	private long fileIndex;
	private int chunkIndex;
	private int chunkN;
	private byte[] bytes;

	public UploadInputStream(JSONObject info) throws Exception {
		this.info = info;
		this.fileSize = info.getLong("fileSize");
		this.fileIndex = 0;
		this.chunkIndex = 0;
		this.chunkN = 0;
		File file = UploadFile.tmpFile(info, this.chunkN++);
		this.bytes = FileUtils.readFileToByteArray(file);
		file.delete();
	}

	public int read() throws IOException {
		if (this.fileIndex < fileSize) {
			int chunkIndex = this.chunkIndex;
			if (chunkIndex >= this.bytes.length) {
				try {
					File file = UploadFile.tmpFile(info, this.chunkN++);
					int seconds = 10;
					while (!file.exists()) {
						if (seconds == 0) {
							throw new Exception("chunk " + this.chunkN + " not found - " + info.toString());
						}
						Thread.sleep(1000);
						seconds--;
					}
					this.bytes = FileUtils.readFileToByteArray(file);
					file.delete();
					chunkIndex = this.chunkIndex = 0;
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
			int value = (this.bytes[chunkIndex] & 0xff);
			this.fileIndex++;
			this.chunkIndex++;
			return value;
		}
		return -1;
	}

}
