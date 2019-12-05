package com.clickntap.smart;

import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.springframework.context.NoSuchMessageException;

import com.clickntap.developers.MessageSource;
import com.google.gson.Gson;

public class SmartJsonMessageSource extends SmartMessageSource {

	private boolean isJson;
	private MessageSource messageSource;

	public void start() throws Exception {
		isJson = true;
		if (messageResource.getFile().getName().endsWith(".properties")) {
			isJson = false;
			super.start();
		} else {
			messageSource = new Gson().fromJson(FileUtils.readFileToString(messageResource.getFile()), MessageSource.class);
		}
	}

	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		locale = new Locale("en");
		if (isJson) {
			try {
				if (code.contains("[")) {
					int x1 = 0;
					int x2 = 0;
					while ((x1 = code.indexOf("[")) > 0) {
						x2 = x1;
						while (code.charAt(x2) != ']' && x2 < code.length())
							x2++;
						code = code.substring(0, x1) + code.substring(x2 + 1);
					}
				}
				String value = (String) messageSource.getLanguages().get(locale.getLanguage()).get(code);
				if (args[0] instanceof SmartContext)
					return scriptEngine.evalScript((SmartContext) args[0], value);
				return value;
			} catch (Exception e) {
				return code;
			}
		} else {
			return super.getMessage(code, args, locale);
		}
	}
}
