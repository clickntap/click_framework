package com.clickntap.tool.f;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.JSONObject;
import org.springframework.core.io.Resource;

import com.clickntap.api.CryptoUtils;

public class F {

  private ScriptEngine javascriptEngine;
  private Resource file;
  private CryptoUtils crypto;
  private UtilExtension extension;
  private String serverURL;

  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public Resource getFile() {
    return file;
  }

  public void setFile(Resource file) {
    this.file = file;
  }

  public CryptoUtils getCrypto() {
    return crypto;
  }

  public void setCrypto(CryptoUtils crypto) {
    this.crypto = crypto;
  }

  public UtilExtension getExtension() {
    return extension;
  }

  public void setExtension(UtilExtension extension) {
    this.extension = extension;
  }

  public void clear() {
    javascriptEngine.put("util", new Util(file, crypto, extension));
  }

  public void init() throws Exception {
    ScriptEngineManager manager = new ScriptEngineManager();
    javascriptEngine = manager.getEngineByName("nashorn");
    javascriptEngine.eval("var Proxy = function(){};");
    javascriptEngine.eval("var document = {};");
    javascriptEngine.eval("var console = {};");
    javascriptEngine.eval("var window = {};");
    javascriptEngine.eval("var sessionStorage = {};");
    javascriptEngine.eval("document.addEventListener = function() {};");
    javascriptEngine.eval("document.location = {};");
    javascriptEngine.eval("document.location.pathname = 'web.app';");
    javascriptEngine.eval("console.log = print;");
    javascriptEngine.eval("window.addEventListener = function() {};");
    javascriptEngine.eval("sessionStorage.getItem = function() { return null; };");
    clear();
    load();
  }

  public void load() throws Exception {
    javascriptEngine.eval("load('" + file.getFile().getAbsolutePath() + "');");
    javascriptEngine.eval("f().serverURL('" + getServerURL() + "')");
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
