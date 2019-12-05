package com.clickntap.developers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageSource {
	private Map<String, Map<String, String>> languages;

	public MessageSource() {
		languages = new HashMap<String, Map<String, String>>();
	}

	public MessageSource(String messageProperties, String locale) throws Exception {
		Properties p = new Properties();
		InputStream in = new FileInputStream(messageProperties);
		p.load(in);
		in.close();
		HashMap<String, String> values = new HashMap<String, String>();
		for (Object key : p.keySet()) {
			values.put(key.toString(), p.getProperty(key.toString()));
		}
		languages = new HashMap<String, Map<String, String>>();
		languages.put(locale, values);
	}

	public Map<String, Map<String, String>> getLanguages() {
		return languages;
	}

	public void setLanguages(Map<String, Map<String, String>> languages) {
		this.languages = languages;
	}

	public void export(String messagesJson) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String json = gson.toJson(this);
		FileUtils.writeStringToFile(new File(messagesJson), json);
	}
}
