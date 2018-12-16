package com.clickntap.smart;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.context.NoSuchMessageException;

import com.clickntap.developers.MessageSource;
import com.clickntap.hub.AppSession;
import com.clickntap.utils.ConstUtils;

public class SmartTxtMessageSource extends SmartJsonMessageSource {

	private boolean isTxt;
	private MessageSource messageSource;

	public void start() throws Exception {
		isTxt = false;
		if (messageResource.getFile().getName().endsWith(".txt")) {
			isTxt = true;
			messageSource = new MessageSource();
		} else {
			super.start();
		}
	}

	public void loadLanguage(String language) throws Exception {
		File f = null;
		if ("en".equals(language)) {
			f = messageResource.getFile();
		} else {
			f = new File(messageResource.getFile().getAbsolutePath().replace(".txt", "_" + language + ".txt"));
		}
		List<String> lines = FileUtils.readLines(f, ConstUtils.UTF_8);
		HashMap<String, String> values = new HashMap<String, String>();
		for (String line : lines) {
			try {
				int x = line.indexOf('=');
				String key = line.substring(0, x).trim();
				String value = line.substring(x + 1).trim();
				values.put(key, value);
			} catch (Exception e) {
			}
		}
		messageSource.getLanguages().put(language, values);
	}

	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		if (isTxt) {
			try {
				for (Object arg : args) {
					if (arg instanceof Map) {
						Object obj = ((Map) arg).get("ws");
						if (obj instanceof AppSession) {
							AppSession ws = (AppSession) obj;
							locale = new Locale(ws.getLocale());
						}
					}
				}
			} catch (Exception e) {
			}
			if (!messageSource.getLanguages().containsKey(locale.getLanguage())) {
				try {
					loadLanguage(locale.getLanguage());
				} catch (Exception e) {
					locale = new Locale("en");
					try {
						loadLanguage(locale.getLanguage());
					} catch (Exception e1) {
						return code;
					}
				}
			}
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
