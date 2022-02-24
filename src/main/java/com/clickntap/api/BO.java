package com.clickntap.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.clickntap.tool.types.Datetime;

public class BO extends com.clickntap.hub.BO {
  private Datetime creationDate;
  private Datetime lastModified;
  private Map<String, Object> values;

  public BO() {
    super();
    values = new HashMap<String, Object>();
  }

  public BO(HttpServletRequest request) throws Exception {
    super(request);
    values = new HashMap<String, Object>();
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

  public Object get(String key) {
    return values.get(key);
  }

  public Set<String> valuesKeySet() {
    return values.keySet();
  }

  public Object put(String key, Object value) {
    return values.put(key, value);
  }
}
