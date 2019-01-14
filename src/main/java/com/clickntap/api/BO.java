package com.clickntap.api;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.clickntap.tool.types.Datetime;

public class BO extends com.clickntap.hub.BO {
  private Datetime creationDate;
  private Datetime lastModified;

  public BO() {
    super();
  }

  public BO(HttpServletRequest request) throws Exception {
    super(request);
  }

  public BOApp getApp() throws Exception {
    return (BOApp) super.getApp();
  }

  public Datetime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Datetime creationDate) {
    this.creationDate = creationDate;
  }

  public Datetime getLastModified() {
    return lastModified;
  }

  public void setLastModified(Datetime lastModified) {
    this.lastModified = lastModified;
  }

  public int deleteWeight() throws Exception {
    return 1;
  }

  public JSONObject json() {
    return json(true);
  }

  public JSONObject json(boolean recursive) {
    JSONObject json = new JSONObject();
    json.put("id", getId());
    json.put("class", getClass().getSimpleName());
    return json;
  }
}
