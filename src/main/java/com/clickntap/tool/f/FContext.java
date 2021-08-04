package com.clickntap.tool.f;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clickntap.api.HttpUtils;
import com.clickntap.api.SecureApiController;
import com.clickntap.smart.SmartContext;

public class FContext extends SmartContext {

	private Map<String, String> params;

	public FContext() {
		super(null, null);
		params = new HashMap<String, String>();
	}

	public FContext(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		params = new HashMap<String, String>();
	}

	public void addParam(String name, String value) {
		params.put(name, value);
	}

	public String http(String url) throws Exception {
		return HttpUtils.get(url);
	}

	public String param(String key) {
		if (params.containsKey(key)) {
			return params.get(key);
		}
		return super.param(key);
	}

	public JSONObject fapi(String uri, String folder) throws Exception {
		SecureApiController controller = (SecureApiController) getBean("apiController");
		return controller.fapi(this, uri, folder);
	}

}
