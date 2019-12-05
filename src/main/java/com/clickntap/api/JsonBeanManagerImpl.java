package com.clickntap.api;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanManagerImpl;

public class JsonBeanManagerImpl extends BeanManagerImpl implements JsonBeanManager {

  public JsonBeanManagerImpl() throws Exception {
    super();
  }

  public String readAsJson(Number id, Class clazz) throws Exception {
    Bean bean = read(id, clazz);
    return JsonUtils.beanToJson(bean);
  }

  public String readListAsJsonByFilter(Class clazz, String filter, String what) throws Exception {
    List<Number> ids = readListByFilter(clazz, JsonUtils.jsonToBean(filter), what);
    return StringUtils.join(ids.toArray(), ",");
  }

}
