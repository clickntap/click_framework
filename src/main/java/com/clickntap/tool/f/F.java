package com.clickntap.tool.f;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.JSONObject;
import org.springframework.core.io.Resource;

public class F {

	private ScriptEngine javascriptEngine;
	private Resource file;

	public Resource getFile() {
		return file;
	}

	public void setFile(Resource file) {
		this.file = file;
	}

	public void init() throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		javascriptEngine = manager.getEngineByName("nashorn");
		javascriptEngine.put("util", new Util(file));
		javascriptEngine.eval("var Proxy = function(){};");
		javascriptEngine.eval("var document = {};");
		javascriptEngine.eval("var console = {};");
		javascriptEngine.eval("var window = {};");
		javascriptEngine.eval("var sessionStorage = {};");
		javascriptEngine.eval("document.addEventListener = function() {};");
		javascriptEngine.eval("console.log = print;");
		javascriptEngine.eval("window.addEventListener = function() {};");
		javascriptEngine.eval("sessionStorage.getItem = function() { return null; };");
		load();
	}

	public void load() throws Exception {
		javascriptEngine.eval("load('" + file.getFile().getAbsolutePath() + "');");
	}

	public Object run(String script) throws Exception {
		return javascriptEngine.eval(script);
	}

	public String chart(JSONObject json) throws Exception {
		return run("f().chart(" + json.toString() + ").render()").toString();
	}

	public ScriptEngine getJavascriptEngine() {
		return javascriptEngine;
	}

}
