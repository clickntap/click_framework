package com.clickntap.build;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.XMLUtils;
import com.google.googlejavaformat.java.Formatter;

import freemarker.template.utility.StringUtil;

public class BOCompiler extends AbstractCompiler {

  private Element projectElement;

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
    StringBuffer sb = new StringBuffer();
    for (String line : lines) {
      line = line.trim();
      boolean skip = true;
      if (line.startsWith("import")) {
        int x1 = line.lastIndexOf(".");
        int x2 = line.indexOf(";");
        String className = line.substring(x1 + 1, x2);
        for (String codeLine : lines) {
          if (!codeLine.startsWith("import") && codeLine.contains(className)) {
            skip = false;
            break;
          }
        }
      } else {
        skip = false;
      }
      if (!skip && !line.isEmpty()) {
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
