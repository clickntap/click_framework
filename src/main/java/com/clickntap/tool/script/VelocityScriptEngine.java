package com.clickntap.tool.script;

import java.io.OutputStream;
import java.util.Map;

import com.clickntap.tool.types.AbstractComponent;
import com.clickntap.tool.types.NotYetImplemented;

public class VelocityScriptEngine extends AbstractComponent implements ScriptEngine {

	public String eval(Map<String, Object> ctx, String templateName) throws Exception {
		throw new NotYetImplemented();
	}

	public void eval(Map<String, Object> ctx, String templateName, OutputStream out) throws Exception {
		throw new NotYetImplemented();
	}

	public boolean evalRule(Map<String, Object> context, String templateName) throws Exception {
		throw new NotYetImplemented();
	}

	public boolean evalRuleScript(Map<String, Object> context, String rule) throws Exception {
		throw new NotYetImplemented();
	}

	public String evalScript(Map<String, Object> ctx, String script) throws Exception {
		throw new NotYetImplemented();
	}

	public void evalScript(Map<String, Object> ctx, String script, OutputStream out) throws Exception {
		throw new NotYetImplemented();
	}

	public void start() throws Exception {
		throw new NotYetImplemented();
	}
	/*
	 * private static final String SCRIPT_LOG_TAG = "script"; private
	 * VelocityEngine engine; private String scriptPath;
	 * 
	 * public String eval(Map<String, Object> ctx, String templateName) throws
	 * Exception { StringWriter writer = new StringWriter();
	 * engine.mergeTemplate(scriptPath + templateName, new ScriptContext(ctx),
	 * writer); return writer.toString(); }
	 * 
	 * public void eval(Map<String, Object> ctx, String templateName, OutputStream
	 * out) throws Exception { OutputStreamWriter writer = new
	 * OutputStreamWriter(out); engine.mergeTemplate(scriptPath + templateName,
	 * new ScriptContext(ctx), writer); }
	 * 
	 * public boolean evalRule(Map<String, Object> context, String templateName)
	 * throws Exception { throw new NotYetImplemented(); }
	 * 
	 * public boolean evalRuleScript(Map<String, Object> context, String rule)
	 * throws Exception { throw new NotYetImplemented(); }
	 * 
	 * public String evalScript(Map<String, Object> ctx, String script) throws
	 * Exception { StringWriter writer = new StringWriter(); Velocity.evaluate(new
	 * ScriptContext(ctx), writer, SCRIPT_LOG_TAG, script); return
	 * writer.toString(); }
	 * 
	 * public void evalScript(Map<String, Object> ctx, String script, OutputStream
	 * out) throws Exception { OutputStreamWriter writer = new
	 * OutputStreamWriter(out); Velocity.evaluate(new ScriptContext(ctx), writer,
	 * SCRIPT_LOG_TAG, script); }
	 * 
	 * public void start() throws Exception { if (getConfResource() != null) {
	 * Properties properties = new Properties();
	 * properties.load(getConfResource().getInputStream()); engine = new
	 * VelocityEngine(properties); } else { engine = new VelocityEngine(); } if
	 * (scriptPath == null) scriptPath = ConstUtils.EMPTY; }
	 * 
	 * public class ScriptContext extends VelocityContext { public
	 * ScriptContext(Map<String, Object> ctx) { super(ctx); } }
	 * 
	 * public void setScriptPath(String scriptPath) throws IOException {
	 * this.scriptPath = scriptPath; }
	 */
}
