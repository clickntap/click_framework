package com.clickntap.tool.f;

import java.io.File;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
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
		javascriptEngine.eval("var Proxy = function(){};");
		javascriptEngine.eval("var document = {};");
		javascriptEngine.eval("var window = {};");
		javascriptEngine.eval("var sessionStorage = {};");
		javascriptEngine.eval("document.addEventListener = function() {};");
		javascriptEngine.eval("window.addEventListener = function() {};");
		javascriptEngine.eval("sessionStorage.getItem = function() { return null; };");
		javascriptEngine.eval("load('" + file.getFile().getAbsolutePath() + "');");
	}

	public Object run(String script) throws Exception {
		return javascriptEngine.eval(script);
	}

	public String chart(JSONObject json) throws Exception {
		return run("f().chart(" + json.toString() + ").render()").toString();
	}

	public static void main(String[] args) throws Exception {
		F f = new F();
		f.setFile(new FileSystemResource(new File("src/main/webapp/ui/js/f.js")));
		f.init();
		JSONObject json = new JSONObject("{\"values\" : [200,300,560,500,444],\"colors\" : [\"red\",\"blue\",\"indigo\",\"green\",\"orange\"]}");
		System.out.println(f.chart(json));
	}

}
