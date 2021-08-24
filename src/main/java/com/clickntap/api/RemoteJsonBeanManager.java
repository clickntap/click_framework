package com.clickntap.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.caucho.hessian.client.HessianProxyFactory;
import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.RemoteBeanManager;

public class RemoteJsonBeanManager extends RemoteBeanManager implements JsonBeanManager {

  private JsonBeanManager remoteJsonBeanManager;

  public void setServiceUrl(String serviceUrl) {
    HessianProxyFactory factory = new HessianProxyFactory();
    try {
      remoteJsonBeanManager = (JsonBeanManager) factory.create(JsonBeanManager.class, serviceUrl);
    } catch (Exception e) {
    }
    super.setServiceUrl(serviceUrl);
  }

  public Bean read(Number id, Class clazz) throws Exception {
    String jsonAsString = readAsJson(id, clazz);
    if (jsonAsString != null) {
      Bean bean = JsonUtils.jsonToBean(jsonAsString);
      bean.setBeanManager(this);
      return bean;
    }
    return null;
  }

  public List<Number> readListByFilter(Class clazz, Bean filter, String what) throws Exception {
    List<Number> ids = new ArrayList<Number>();
    for (String id : StringUtils.split(readListAsJsonByFilter(clazz, JsonUtils.beanToJson(filter), what), ",")) {
      ids.add(Long.parseLong(id));
    }
    return ids;
  }

  public String readAsJson(Number id, Class clazz) throws Exception {
    return remoteJsonBeanManager.readAsJson(id, clazz);
  }

  public String readListAsJsonByFilter(Class clazz, String filter, String what) throws Exception {
    return remoteJsonBeanManager.readListAsJsonByFilter(clazz, filter, what);
  }
}
