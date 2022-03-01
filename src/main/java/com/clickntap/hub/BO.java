package com.clickntap.hub;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanManager;
import com.clickntap.utils.BindUtils;

public class BO extends Bean {

  public BO() {
  }

  public BO(HttpServletRequest request) throws Exception {
    ServletRequestDataBinder binder = new ServletRequestDataBinder(this, this.getClass().getName());
    BindUtils.registerCustomEditor(binder);
    binder.bind(request);
  }

  public BOManager getApp() throws Exception {
     return (BOManager) getBeanManager();
  }

  public void setApp(BOManager app) {
    setBeanManager((BeanManager) app);
  }

  public JSONObject toJSON() {
    return new JSONObject(this);
  }
}
