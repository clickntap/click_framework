package com.clickntap.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.XMLUtils;
import com.google.googlejavaformat.java.Formatter;

import freemarker.template.utility.StringUtil;

public class BOCompiler {

  private Element projectElement;
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
    engine.setExtension(ConstUtils.EMPTY);
    engine.setUpdateDelay(0);
    engine.start();
    return engine;
  }

  public void compile(File file) throws Exception {
    setClassLoader(BOCompiler.class.getName());
    getEngine().eval(createCtx(file), "java.ftl");
    getEngine().eval(createCtx(file), "xml.ftl");
    getEngine().eval(createCtx(file), "app.ftl");
    getEngine().eval(createCtx(file), "db.ftl");
  }

  public boolean compilable(File file) {
    return false;
  }

  public Map<String, Object> createCtx(File file) throws DocumentException, IOException, Exception {
    Map<String, Object> ctx = new HashMap<String, Object>();
    setProjectElement(XMLUtils.copyFrom(file).getRootElement());
    ctx.put(ConstUtils.THIS, this);
    return ctx;
  }

  public Element getProjectElement() {
    return projectElement;
  }

  public void setProjectElement(Element projectElement) {
    this.projectElement = projectElement;
  }

  public String getProjectPackage() {
    return getProjectElement().attributeValue("package");
  }

  public void save(String content, String path) throws Exception {
    System.out.println(path);
    for (int i = 'a'; i <= 'z'; i++) {
      if (i == 'a' || i == 'e' || i == 'i' || i == 'o' || i == 'u') {
        continue;
      }
      content = StringUtil.replace(content, ((char) i) + "ys(", ((char) i) + "ies(");
      content = StringUtil.replace(content, ((char) i) + "ys\"", ((char) i) + "ies\"");
    }
    if (path.endsWith(".java")) {
      content = organizeImports(content);
      content = new Formatter().formatSource(content);
    }
    FileUtils.writeStringToFile(new File(path), content, ConstUtils.UTF_8);
  }

  private String organizeImports(String content) {
    String[] lines = content.split(System.getProperty("line.separator"));
    String packageLine = null;
    List<String> imports = new ArrayList<String>();
    StringBuffer sb = new StringBuffer();
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("package")) {
        packageLine = line;
      }
    }
    String packageName = packageLine.substring("package".length()).trim().replace(";", "");
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("import")) {
        if (!imports.contains(line)) {
          String path = line.replace(packageName + ".", "");
          if (path.contains(".")) {
            int x1 = line.lastIndexOf(".");
            int x2 = line.indexOf(";");
            String className = line.substring(x1 + 1, x2);
            boolean skip = true;
            for (String codeLine : lines) {
              if (!codeLine.startsWith("import") && (codeLine.contains(className + ' ') || codeLine.contains(className + '<') || codeLine.contains(className + '>') || codeLine.contains(className + '.'))) {
                skip = false;
                break;
              }
            }
            if (!skip) {
              imports.add(line);
            }
          }
        }
      }
    }
    sb.append(packageLine).append('\n').append('\n');
    Collections.sort(imports, new Comparator<String>() {
      public int compare(String s1, String s2) {
        if (s1.contains(" com.") && !s2.contains(" com.")) {
          return 1;
        }
        if (!s1.contains(" com.") && s2.contains(" com.")) {
          return -1;
        }
        return s1.compareTo(s2);
      }
    });
    String currentPath = "";
    for (String line : imports) {
      int x = line.indexOf(".");
      String newPath = line.substring(0, x);
      if (!currentPath.equals(newPath)) {
        currentPath = newPath;
        sb.append('\n');
      }
      sb.append(line).append('\n');
    }
    for (String line : lines) {
      line = line.trim();
      if (!line.startsWith("import") && !line.startsWith("package") && !line.isEmpty()) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }

  public String camelize(String name, boolean startsUpper, String prefix) {
    StringBuffer sb = new StringBuffer(prefix);
    boolean upper = startsUpper;
    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) == '_') {
        upper = true;
      } else {
        if (upper)
          sb.append(name.toUpperCase().charAt(i));
        else
          sb.append(name.toLowerCase().charAt(i));
        upper = false;
      }
    }
    return sb.toString();
  }

  public String name(String name) {
    return camelize(name, false, "");
  }

  public String setter(String name) {
    return camelize(name, true, "set");
  }

  public String getter(String name) {
    return camelize(name, true, "get");
  }
}
