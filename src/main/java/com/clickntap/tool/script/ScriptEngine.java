package com.clickntap.tool.script;

import java.io.OutputStream;
import java.util.Map;

public interface ScriptEngine {

	public String eval(Map<String, Object> ctx, String templateName) throws Exception;

	public String evalScript(Map<String, Object> ctx, String script) throws Exception;

	public void eval(Map<String, Object> ctx, String templateName, OutputStream out) throws Exception;

	public void evalScript(Map<String, Object> ctx, String script, OutputStream out) throws Exception;

	public boolean evalRule(Map<String, Object> context, String templateName) throws Exception;

	public boolean evalRuleScript(Map<String, Object> context, String rule) throws Exception;
}
