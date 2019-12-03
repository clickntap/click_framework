package com.clickntap.api;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.clickntap.smart.SmartContext;

public class AdvancedSearchFilter extends BO {

	private JSONObject json;

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	public List<JSONObject> list(JSONArray array) {
		return SmartContext.asList(array);
	}

	public static String toCamelCase(String value) {
		return ApiUtils.toCamelCase(value, true);
	}

}
