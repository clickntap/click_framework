package com.clickntap.api;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

public class CryptoUtils {
	private String key = "JYdM3AsUWaegpIqykYrmLPiUuJYtWT9Hosg9R10yH8o=";

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

  public String encrypt(String s) throws Exception {
    byte[] bytes = SecureUtils.base64dec(key);
    byte[] encoded = SecurityUtils.encrypt(s.getBytes(), bytes);
    s = SecureUtils.base64enc(encoded);
    s = convertStringToHex(s);
    return s;
  }

  public String md5(String s) throws Exception {
    return SecurityUtils.md5(s);
  }

	public String decrypt(String s) throws Exception {
		s = convertHexToString(s);
		byte[] decoded = SecurityUtils.decrypt(SecureUtils.base64dec(s), SecureUtils.base64dec(key));
		return new String(decoded, ConstUtils.UTF_8);
	}

	public static String convertStringToHex(String str) {
		char[] chars = str.toCharArray();
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}
		return hex.toString();
	}

	public static String convertHexToString(String hex) {
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < hex.length() - 1; i += 2) {
			String output = hex.substring(i, (i + 2));
			int decimal = Integer.parseInt(output, 16);
			sb.append((char) decimal);
			temp.append(decimal);
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		{
			System.out.println(new CryptoUtils().getKey());
			String s = "K192346306";
			System.out.println(s);
			System.out.println(s = new CryptoUtils().encrypt(s));
			System.out.println(new CryptoUtils().decrypt(s));
		}
		String key = "";
		//while (!key.startsWith("")) {
		CryptoUtils utils = new CryptoUtils();
		key = SecureUtils.base64enc(SecurityUtils.generateKey(256).getEncoded());
		utils.setKey(key);
		System.out.println("key=" + utils.getKey());
		String s = "K192346306";
		System.out.println(s);
		System.out.println(s = utils.encrypt(s));
		System.out.println(utils.decrypt(s));
		//}
	}

}
