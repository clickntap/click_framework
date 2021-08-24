package com.clickntap.platform;

import org.json.JSONObject;

public class FTask implements Runnable {

  private String code;
  private JSONObject data;
  private String codeScript;
  private String dataScript;
  private boolean done;

  public FTask() {
    this.done = false;
  }

  public void run() {
    FThread f = (FThread) Thread.currentThread();
    try {
      f.getF().clear();
      String code = f.getF().run(codeScript).toString();
      setCode(code);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      setData(new JSONObject(f.getF().run(dataScript).toString()));
    } catch (Exception e) {
    }
    this.done = true;
  }

  public JSONObject getData() {
    return data;
  }

  public void setData(JSONObject data) {
    this.data = data;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getCodeScript() {
    return codeScript;
  }

  public void setCodeScript(String codeScript) {
    this.codeScript = codeScript;
  }

  public String getDataScript() {
    return dataScript;
  }

  public void setDataScript(String dataScript) {
    this.dataScript = dataScript;
  }

  public boolean isDone() {
    return done;
  }

}
