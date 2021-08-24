package com.clickntap.tool.script;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.clickntap.utils.ConstUtils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FreemarkerScriptEngine implements ScriptEngine {

  private static final String EXTENSION_DOTFTL = ".ftl";
  private Configuration ftl;
  private Resource templateDir;
  private String classLoader;
  private String extension;
  private int updateDelay;

  public FreemarkerScriptEngine() {
    updateDelay = 1;
  }

  public int getUpdateDelay() {
    return updateDelay;
  }

  public void setUpdateDelay(int updateDelay) {
    this.updateDelay = updateDelay;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public void start() throws Exception {
    Version version = Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
    ftl = new Configuration(version);
    ftl.setTemplateUpdateDelayMilliseconds(getUpdateDelay());
    ftl.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    ftl.setDefaultEncoding(ConstUtils.UTF_8);
    ftl.setObjectWrapper(new BeansWrapper(version));
    ftl.setNumberFormat("computer");
    ftl.setURLEscapingCharset(ConstUtils.UTF_8);
    ftl.setOutputEncoding(ConstUtils.UTF_8);
    // ftl.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
    List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
    if (templateDir != null)
      loaders.add(new FileTemplateLoader(templateDir.getFile()));
    if (classLoader != null)
      loaders.add(new ClassTemplateLoader(Class.forName(classLoader), ConstUtils.EMPTY));
    if (loaders.size() > 0) {
      MultiTemplateLoader mtl = new MultiTemplateLoader(loaders.toArray((new TemplateLoader[loaders.size()])));
      ftl.setTemplateLoader(mtl);
    }
  }

  public void setClassLoader(String classLoader) {
    this.classLoader = classLoader;
  }

  public void setTemplateDir(Resource templateDir) {
    this.templateDir = templateDir;
  }

  public String eval(Map<String, Object> ctx, String templateName) throws Exception {
    StringWriter w = new StringWriter();
    ftl.getTemplate(getTemplateName(templateName)).process(ctx, w);
    return w.toString();
  }

  private String getTemplateName(String templateName) {
    if (extension != null) {
      return templateName + extension;
    } else {
      if (templateName.endsWith(EXTENSION_DOTFTL))
        return templateName;
      else
        return templateName + EXTENSION_DOTFTL;
    }
  }

  public void eval(Map<String, Object> ctx, String templateName, OutputStream out) throws Exception {
    StringWriter w = new StringWriter();
    ftl.getTemplate(getTemplateName(templateName)).process(ctx, w);
    out.write(w.toString().getBytes(ConstUtils.UTF_8));
  }

  public boolean evalRule(Map<String, Object> context, String templateName) throws Exception {
    throw new Exception();
  }

  public boolean evalRuleScript(Map<String, Object> context, String rule) throws Exception {
    rule = "<#if " + rule + ">true<#else>false</#if>";
    return Boolean.valueOf(evalScript(context, rule));
  }

  public String evalScript(Map<String, Object> ctx, String script) throws Exception {
    Template t = new Template(script, new StringReader(script), ftl);
    StringWriter w = new StringWriter();
    t.process(ctx, w);
    return w.toString();

  }

  public void evalScript(Map<String, Object> ctx, String script, OutputStream out) throws Exception {
    throw new Exception();
  }

  class MyTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out) throws TemplateException {
      try {
        out.write("<div style=\"position:absolute;z-index:99;color:rgba(255,255,255,0.8);background-color:rgba(200,50,50,0.8);font-family:sans-serif;padding:5px;padding-left:10px;padding-right:10px;border-radius:10px;font-size:12px;font-weight:normal\"><b>SMART FRAMEWORK 1.171 &copy; 2009-2011 Click'nTap</b><br>" + te.getMessage() + "</div>");
      } catch (IOException e) {
      }
    }
  }

}
