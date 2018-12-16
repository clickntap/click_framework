package com.clickntap.tool.bean;

import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.List;

import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.AsciiUtils;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

public class BeanErrors {
	public final static String mailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private Errors errors;

	private Object target;

	private BeanManager beanManager;

	public BeanErrors(Object target, Errors errors, BeanManager beanManager) {
		this.target = target;
		this.errors = errors;
		this.beanManager = beanManager;
	}

	public Object getTarget() {
		return target;
	}

	// public void assertRule(String rule, String propertyName) throws Exception {
	// if (!beanManager.evaluateRule(rule, target))
	// errors.rejectValue(propertyName, rule);
	// }

	public void assertEmptyList(String propertyName) throws Exception {
		assertEmptyList(propertyName, propertyName);
	}

	public void assertEmptyList(String propertyName, String propertyErrorName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null && value instanceof List) {
			List list = (List) value;
			if (list.size() == 0) {
				errors.rejectValue(propertyErrorName, "listEmpty");
			}
		} else {
			errors.rejectValue(propertyErrorName, "listEmpty");
		}
	}

	public void assertBarcode39(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		Code39Bean bean = new Code39Bean();
		try {
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(100, BufferedImage.TYPE_INT_RGB, true, 0);
			bean.generateBarcode(canvas, value.toString());
			canvas.finish();
		} catch (Exception e) {
			errors.rejectValue(propertyName, "barcode39");
		}
	}

	public void assertDataIntegrity(String propertyName, String className) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null && beanManager.read(((Number) value), findClass(className)) == null)
			errors.rejectValue(propertyName, "dataIntegrity");
	}

	public void assertDataIntegrity(String propertyName, String className, String classPropertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			Class clazz = findClass(className);
			Object bean = clazz.newInstance();
			BeanUtils.setValue(bean, classPropertyName, value);
			if (beanManager.readByFilter((Bean) bean, classPropertyName, clazz) == null)
				errors.rejectValue(propertyName, "dataIntegrity");
		}
	}

	private Class findClass(String className) throws ClassNotFoundException {
		Class clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			className = target.getClass().getPackage().getName() + "." + className;
			clazz = Class.forName(className);
		}
		return clazz;
	}

	public void assertWebName(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			if (!AsciiUtils.isWebized(value.toString()))
				errors.rejectValue(propertyName, "web");
		}
	}

	public void assertTaxCode(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			String taxCode = value.toString();
			if (taxCode.trim().length() != 16)
				errors.rejectValue(propertyName, "bad");
		}
	}

	public void assertPassword(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			if (!AsciiUtils.isWebized(value.toString()))
				errors.rejectValue(propertyName, "badPassword");
			else if (value.toString().length() < 4)
				errors.rejectValue(propertyName, "shortPassword");
		}
	}

	public void assertNotNull(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null)
			errors.rejectValue(propertyName, "null");
	}

	public void assertNotEmpty(String propertyName) {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || value.toString().trim().equals(ConstUtils.EMPTY))
			errors.rejectValue(propertyName, "empty");
	}

	public void assertEquals(String propertyName1, String propertyName2) {
		Object value1 = BeanUtils.getValue(target, propertyName1);
		Object value2 = BeanUtils.getValue(target, propertyName2);
		if (value1 == null && value2 == null)
			return;
		if (value1 != null && !value1.equals(value2))
			errors.rejectValue(propertyName1, "different");
	}

	public void assertEqualsMD5(String propertyName1, String propertyName2) throws Exception {
		String value1 = BeanUtils.getValue(target, propertyName1).toString();
		String value2 = BeanUtils.getValue(target, propertyName2).toString();
		value1 = SecurityUtils.md5(value1);
		if (value1 == null && value2 == null)
			return;
		if (value1 != null && !value1.equals(value2))
			errors.rejectValue(propertyName1, "differentMD5");
	}

	public void assertImage(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null)
			errors.rejectValue(propertyName, "empty");
		else {
			MultipartFile file = (MultipartFile) value;
			if (file.getContentType() == null)
				errors.rejectValue(propertyName, "empty");
			else if (!file.getContentType().startsWith("image"))
				errors.rejectValue(propertyName, "badContentType");
		}
	}

	public void assertEmail(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || !isEmail(value))
			errors.rejectValue(propertyName, "bad");
	}

	public static boolean isEmail(Object value) {
		return value.toString().matches(mailRegex);
	}

	public void assertPhoneNumber(String propertyName, int maxLen) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null && !value.equals("")) {
			try {
				value = AsciiUtils.phonize(value.toString());
				Double.parseDouble(value.toString());
				if (value.toString().length() > maxLen) {
					errors.rejectValue(propertyName, "tooLong");
				}
			} catch (Exception e) {
				errors.rejectValue(propertyName, "notNumber");
			}
		}
	}

	public void assertLength(String propertyName, int minLen, int maxLen) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null && !value.equals("")) {
			try {
				if (value.toString().length() < minLen) {
					errors.rejectValue(propertyName, "tooShort");
				}
				if (value.toString().length() > maxLen) {
					errors.rejectValue(propertyName, "tooLong");
				}
			} catch (Exception e) {
				errors.rejectValue(propertyName, "bad");
			}
		}
	}

	public void assertPhoneNumber(String propertyName) throws Exception {
		assertPhoneNumber(propertyName, Integer.MAX_VALUE);
	}

	public void assertRegExp(String propertyName, String regExp) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || !value.toString().matches(regExp))
			errors.rejectValue(propertyName, "bad");
	}

	public void assertOnlyLetters(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || notOnlyLetters(value.toString()))
			errors.rejectValue(propertyName, "onlyletters");
	}

	private boolean notOnlyLetters(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (!Character.isLetter(string.charAt(i)))
				return true;
		}
		return false;
	}

	public void assertEmailIfNotEmpty(String propertyName) throws Exception {
		assertRegExp(propertyName, mailRegex);
	}

	public void assertPositive(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			if (value != null && value.toString().trim().equals(ConstUtils.EMPTY))
				return;
			if (((Number) value).intValue() <= 0)
				errors.rejectValue(propertyName, "negative");
		}
	}

	public void assertPercentage(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			if (value != null && value.toString().trim().equals(ConstUtils.EMPTY))
				return;
			if (((Number) value).intValue() < 0 || ((Number) value).intValue() > 100)
				errors.rejectValue(propertyName, "percentage");
		}
	}

	public void assertTrue(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value != null) {
			if (Boolean.parseBoolean(value.toString()))
				return;
		}
		errors.rejectValue(propertyName, "false");
	}

	public void assertGt(String propertyName1, String propertyName2) throws Exception {
		assertGt(propertyName1, propertyName2, propertyName1, "gt");
	}

	public void assertLt(String propertyName1, String propertyName2) throws Exception {
		assertGt(propertyName2, propertyName1, propertyName1, "lt");
	}

	public void assertGt(String propertyName1, String propertyName2, String propertyName) throws Exception {
		assertGt(propertyName1, propertyName2, propertyName, "gt");
	}

	public void assertLt(String propertyName1, String propertyName2, String propertyName) throws Exception {
		assertGt(propertyName2, propertyName1, propertyName, "lt");
	}

	public void assertUnique(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || value.toString().trim().equals(ConstUtils.EMPTY))
			return;
		if (beanManager.readList((Bean) target, propertyName).size() > 0)
			errors.rejectValue(propertyName, "conflict");
	}

	public void assertExists(String propertyName) throws Exception {
		Object value = BeanUtils.getValue(target, propertyName);
		if (value == null || value.toString().trim().equals(ConstUtils.EMPTY))
			return;
		List<Number> ids = beanManager.readList((Bean) target, propertyName);
		if (ids.size() > 0) {

		} else {
			errors.rejectValue(propertyName, "unknown");
		}
	}

	private void assertGt(String propertyName1, String propertyName2, String propertyName, String error) {
		Object value1 = BeanUtils.getValue(target, propertyName1);
		Object value2 = BeanUtils.getValue(target, propertyName2);
		if (value1 == null || value1.toString().trim().equals(ConstUtils.EMPTY))
			return;
		if (value2 == null || value2.toString().trim().equals(ConstUtils.EMPTY))
			return;
		if (value1 instanceof String) {
			if (((String) value1).compareToIgnoreCase((String) value2) < 0)
				errors.rejectValue(propertyName, error);
		}
		if (value1 instanceof Datetime) {
			if (((Datetime) value1).getTimeInMillis() / 1000 < ((Datetime) value2).getTimeInMillis() / 1000)
				errors.rejectValue(propertyName, error);
		}
		if (value1 instanceof Timestamp) {
			long lo1 = ((Timestamp) value1).getTime() / 1000;
			long lo2 = ((Timestamp) value2).getTime() / 1000;
			if (lo1 < lo2)
				errors.rejectValue(propertyName, error);
		}
		if (value1 instanceof Number) {
			if (((Number) value1).longValue() < ((Number) value2).longValue())
				errors.rejectValue(propertyName, error);
		}
	}
}
