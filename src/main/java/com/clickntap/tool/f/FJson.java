package com.clickntap.tool.f;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.clickntap.utils.ConstUtils;

public class FJson {

  private Resource uiDir;
  private JSONArray templates;
  private String name = "lib/j.json";

  public void setUiDir(Resource uiDir) {
    this.uiDir = uiDir;
  }

  public void compile() throws Exception {
    templates = new JSONArray();
    compile(uiDir.getFile());
    String json = templates.toString();
    FileUtils.writeStringToFile(new File(uiDir.getFile().getAbsolutePath() + '/' + name), json, ConstUtils.UTF_8);
    FileUtils.writeStringToFile(new File(uiDir.getFile().getAbsolutePath() + '/' + name.replace("json", "js")), "\n\nf().form(" + json + ");\n\n", ConstUtils.UTF_8);
  }

  private void compile(File file) throws Exception {
    if (file.isFile()) {
      compileFile(file);
    } else {
      compileDir(file);
    }
  }

  private void compileDir(File dir) throws Exception {
    for (File file : dir.listFiles()) {
      compile(file);
    }
  }

  private void compileFile(File file) throws Exception {
    if (file.getName().endsWith(".json") && !file.getCanonicalPath().contains("/lib/")) {
      JSONObject t = new JSONObject();
      t.put("url", url(file));
      t.put("code", FileUtils.readFileToString(file, ConstUtils.UTF_8));
      templates.put(t);
    }
  }

  private String url(File file) throws Exception {
    String baseUrl = uiDir.getFile().getAbsolutePath();
    String url = file.getAbsolutePath();
    return url.substring(baseUrl.length() - 2);
  }

  public static void main(String[] args) throws Exception {
    FJson t = new FJson();
    t.setUiDir(new FileSystemResource("src/main/webapp/ui"));
    t.compile();
  }

}
