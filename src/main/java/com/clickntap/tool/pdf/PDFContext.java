package com.clickntap.tool.pdf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clickntap.api.HttpUtils;
import com.clickntap.api.SecureApiController;
import com.clickntap.smart.SmartContext;

public class PDFContext extends SmartContext {

	private int numberOfPages;
	private int pageNumber;
	private Map<String, String> params;

	public PDFContext() {
		super(null, null);
		params = new HashMap<String, String>();
	}

	public void addParam(String name, String value) {
		params.put(name, value);
	}

	public PDFContext(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		this.pageNumber = 1;
		this.numberOfPages = 1;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
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
