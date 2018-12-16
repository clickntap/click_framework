package com.clickntap.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Element;
import org.json.JSONObject;
import org.springframework.core.io.Resource;

import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.XMLUtils;

abstract class AbstractCompiler implements Compiler {

  private Resource workDir;
  private String classLoader;

  public String getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(String classLoader) {
    this.classLoader = classLoader;
  }

  public FreemarkerScriptEngine getEngine() throws Exception {
    FreemarkerScriptEngine engine = new FreemarkerScriptEngine();
    engine.setClassLoader(classLoader);
    engine.setTemplateDir(getWorkDir());
    engine.setExtension(ConstUtils.EMPTY);
    engine.setUpdateDelay(0);
    engine.start();
    return engine;
  }

  public Resource getWorkDir() {
    return workDir;
  }

  public void setWorkDir(Resource workDir) {
    this.workDir = workDir;
  }

  public void precompile(File file) {
    try {
      String templateName = templateName(file);
      Map<String, Object> ctx = new HashMap<String, Object>();
      FileOutputStream out = new FileOutputStream(tmpFile(file));
      getEngine().eval(ctx, templateName, out);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String templateName(File file) {
    String templateName = null;
    try {
      String dirPath = workDir.getFile().getCanonicalPath();
      String filePath = file.getCanonicalPath();
      templateName = filePath.substring(dirPath.length() + 1);
    } catch (IOException e) {
    }
    return templateName;
  }

  public String libs(String channel, String extension) {
    JSONObject json = new JSONObject();
    StringBuffer sb = new StringBuffer();
    try {
      if (extension.equals("json")) {
        File dir = new File(workDir.getFile().getCanonicalPath() + "/lib/" + channel);
        for (File file : dir.listFiles()) {
          if (FilenameUtils.getExtension(file.getName()).equals(channel)) {
            json.put(FilenameUtils.getBaseName(file.getName()), FileUtils.readFileToString(file, ConstUtils.UTF_8));
          }
        }
      } else {
        File confFile = new File(workDir.getFile().getCanonicalPath() + "/lib/libs.xml");
        Element root = XMLUtils.copyFrom(confFile).getRootElement();
        for (Element lib : (List<Element>) root.elements(channel)) {
          File libFile = new File(workDir.getFile().getCanonicalPath() + "/lib/" + channel + "/" + lib.attributeValue("src"));
          if (FilenameUtils.getExtension(lib.attributeValue("src")).equals(extension)) {
            sb.append('\n');
            sb.append(FileUtils.readFileToString(libFile, ConstUtils.UTF_8));
            sb.append('\n');
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (extension.equals("json")) {
      return json.toString();
    } else {
      return sb.toString();
    }
  }

  protected File tmpFile(File file) {
    return new File(file.getParentFile(), file.getName() + ".tmp");
  }

}
