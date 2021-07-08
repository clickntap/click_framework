package com.clickntap.platform.api;

import java.util.List;

import com.clickntap.api.BO;
import com.clickntap.api.SecureApiController;
import com.clickntap.api.SecureRequest;
import com.clickntap.hub.App;
import com.clickntap.platform.FExecutor;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.script.FreemarkerScriptEngine;

public interface ApiModule {

	boolean handleTvRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception;

	boolean handleWebRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception;

	boolean handleAppRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor) throws Exception;

	boolean handleTvRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	boolean handleWebRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	boolean handleAppRequest(SmartContext ctx, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onPreCreate(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onCreate(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onPreEdit(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onEdit(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onPreDelete(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

	void onDelete(SmartContext context, BO bo, List<String> path, App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor fExecutor, SecureRequest secureRequest) throws Exception;

}
