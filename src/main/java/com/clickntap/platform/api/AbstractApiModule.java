package com.clickntap.platform.api;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.clickntap.api.BO;
import com.clickntap.api.SecureApiController;
import com.clickntap.api.SecureRequest;
import com.clickntap.hub.App;
import com.clickntap.platform.FExecutor;
import com.clickntap.platform.SVG;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.script.FreemarkerScriptEngine;

public class AbstractApiModule implements ApiModule {

	public boolean handleTvRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception {
		return false;
	}

	public boolean handleWebRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception {
		return false;
	}

	public boolean handleAppRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception {
		return false;
	}

	public boolean handleTvRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {
		return false;
	}

	public boolean handleWebRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {
		return false;
	}

	public boolean handleAppRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {
		return false;
	}

	public void onPreCreate(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {

	}

	public void onCreate(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {

	}

	public void onPreEdit(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {

	}

	public void onEdit(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {
	}

	public void onPreDelete(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {
	}

	public void onDelete(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception {

	}

	public void svg(HttpServletResponse response, String format, SVG svg, String name) throws Exception {
		if (format.equalsIgnoreCase("png"))
			response.setContentType("image/png");
		if (format.equalsIgnoreCase("eps"))
			response.setContentType("application/postscript");
		if (format.equalsIgnoreCase("jpg"))
			response.setContentType("image/jpeg");
		if (format.equalsIgnoreCase("pdf"))
			response.setContentType("application/pdf");
		if (format.equalsIgnoreCase("svg"))
			response.setContentType("image/svg+xml");
		if (format.equalsIgnoreCase("png")) {
			svg.png(response.getOutputStream());
		}
		if (format.equalsIgnoreCase("jpg")) {
			if (name != null) {
				response.addHeader("Content-Disposition", "attachment; filename=" + name + ".jpeg");
			}
			svg.jpg(response.getOutputStream());
		}
		if (format.equalsIgnoreCase("pdf")) {
			if (name != null) {
				response.addHeader("Content-Disposition", "attachment; filename=" + name + ".pdf");
			}
			svg.pdf(response.getOutputStream());
		}
		if (format.equalsIgnoreCase("svg")) {
			svg.svg(response.getOutputStream());
		}
	}

}
