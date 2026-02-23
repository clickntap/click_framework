package com.clickntap.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.clickntap.api.SecureUtils;

public final class FileCryptoUtils {
	private static final SecureRandom RNG = new SecureRandom();
	private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
	private static final String ALGORITHM = "AES";
	private static final int IV_LEN = 12;
	private static final int TAG_BITS = 128;
	private String key = null;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private Cipher cipher(int mode, byte[] iv) throws Exception {
		byte[] bytes = SecureUtils.base64dec(key);
		GCMParameterSpec paramSpec = new GCMParameterSpec(TAG_BITS, iv);
		SecretKeySpec secretKey = new SecretKeySpec(bytes, ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(mode, secretKey, paramSpec);
		return cipher;
	}

	public byte[] encrypt(InputStream in, OutputStream encryptedOut) throws Exception {
		byte[] iv = new byte[IV_LEN];
		RNG.nextBytes(iv);
		Cipher cipher = cipher(Cipher.ENCRYPT_MODE, iv);
		try (CipherOutputStream out = new CipherOutputStream(encryptedOut, cipher)) {
			IOUtils.copyLarge(in, out, new byte[1024 * 1024]);
		}
		return iv;
	}

	public void decrypt(InputStream encryptedIn, OutputStream out, byte[] iv) throws Exception {
		Cipher cipher = cipher(Cipher.DECRYPT_MODE, iv);
		try (var in = new CipherInputStream(encryptedIn, cipher)) {
			IOUtils.copyLarge(in, out, new byte[1024 * 1024]);
		}
	}
}