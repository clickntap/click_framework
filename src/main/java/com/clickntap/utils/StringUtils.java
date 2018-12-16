package com.clickntap.utils;

import freemarker.template.utility.StringUtil;

public class StringUtils extends org.springframework.util.StringUtils {

	public static String toString(Object value) {
		if (value == null)
			return ConstUtils.EMPTY;
		if (value instanceof String[]) {
			String[] values = (String[]) value;
			return StringUtils.arrayToCommaDelimitedString(values);
		} else
			return value.toString();
	}

	public static String code(String key) {
		return key + Long.toHexString(System.currentTimeMillis());
	}

	public static String shortName(String name, int len) {
		if (name == null)
			return ConstUtils.EMPTY;
		if (name.length() <= len)
			return name;
		name = name.substring(0, len - 3).trim() + "...";
		return name;
	}

	public static String splitWords(String text, int maxSize) {
		if (text == null)
			return null;
		String[] strings = StringUtil.split(text, ConstUtils.SPACE_CHAR);
		String retVal = ConstUtils.EMPTY;
		for (String s : strings) {
			int n = s.length() / maxSize;
			if (n == 0)
				retVal += ConstUtils.SPACE + s;
			else {
				int i;
				for (i = 0; i < n; i++)
					retVal += ConstUtils.SPACE + s.substring(i * maxSize, (i + 1) * maxSize);
				retVal += ConstUtils.SPACE + s.substring(i * maxSize);
			}
		}
		return retVal.trim();
	}

	public static String randomPassword() {
		return code("pass");
	}

}
