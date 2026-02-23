package com.clickntap.utils;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.clickntap.api.CryptoUtils;

@SuppressWarnings("deprecation")
public class DecryptingPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	private byte S = (byte) 0x5D;
	private byte[] K = new byte[] { (byte) 0x6E, (byte) 0x1A, (byte) 0x29, (byte) 0x33, (byte) 0x09, (byte) 0x33, (byte) 0x68, (byte) 0x69, (byte) 0x6E, (byte) 0x3A, (byte) 0x17, (byte) 0x1A, (byte) 0x0E, (byte) 0x16, (byte) 0x3A, (byte) 0x04, (byte) 0x2F, (byte) 0x31, (byte) 0x2B, (byte) 0x32, (byte) 0x16, (byte) 0x28, (byte) 0x27, (byte) 0x1E, (byte) 0x05, (byte) 0x08, (byte) 0x2A, (byte) 0x33, (byte) 0x24, (byte) 0x1E, (byte) 0x1A, (byte) 0x24, (byte) 0x37, (byte) 0x2A, (byte) 0x3F, (byte) 0x3C, (byte) 0x09, (byte) 0x24, (byte) 0x29, (byte) 0x6A, (byte) 0x37, (byte) 0x31, (byte) 0x1C, (byte) 0x60 };
	private CryptoUtils crypto;

	private String build() {
		byte[] decoded = new byte[K.length];
		for (int i = 0; i < K.length; i++) {
			decoded[i] = (byte) (K[i] ^ S);
		}
		return new String(decoded, StandardCharsets.UTF_8);
	}

	public DecryptingPlaceholderConfigurer() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
		CryptoUtils crypto = new CryptoUtils();
		crypto.setKey(build());
		this.crypto = crypto;
	}

	protected String convertProperty(String propertyName, String propertyValue) {
		try {
			propertyValue = crypto.decrypt(propertyValue);
		} catch (Exception e) {
		}
		return propertyValue;
	}
}