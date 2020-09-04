package com.clickntap.api;

import java.beans.PropertyDescriptor;

import org.json.JSONObject;

import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.types.Datetime;

public class JsonUtils {
  public static String beanToJson(Bean bean) throws Exception {
    JSONObject json = new JSONObject();
    json.put("class", bean.getClass().getCanonicalName());
    PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(bean.getClass());
    for (PropertyDescriptor pd : pds) {
      try {
        String name = pd.getName();
        Class<?> clazz = pd.getPropertyType();
        if (clazz.getTypeName().equals("java.lang.String") || clazz.getTypeName().equals("java.lang.Number") || clazz.getTypeName().equals("com.clickntap.tool.types.Datetime")) {

          Object value = BeanUtils.getValue(bean, name);
          if (value != null) {
            JSONObject field = new JSONObject();
            if (value instanceof Datetime) {
              field.put("type", "datetime");
            } else if (value instanceof Number) {
              field.put("type", "number");
            } else if (value instanceof String) {
              field.put("type", "string");
            }
            if (field.has("type")) {
              field.put("value", value);
              json.put(name, field);
            }
          }
        }
      } catch (Exception exception) {
      }
    }
    return json.toString();
  }

  public static Bean jsonToBean(String jsonAsString) throws Exception {
    JSONObject json = new JSONObject(jsonAsString);
    Bean bean = (Bean) Class.forName(json.getString("class")).getDeclaredConstructor().newInstance();
    for (Object key : json.keySet()) {
      if (key.equals("class"))
        continue;
      JSONObject field = json.getJSONObject(key.toString());
      if (field.getString("type").equals("datetime")) {
        BeanUtils.setValue(bean, key.toString(), new Datetime(field.getString("value")));
        continue;
      }
      if (field.getString("type").equals("number")) {
        BeanUtils.setValue(bean, key.toString(), field.get("value"));
        continue;
      }
      BeanUtils.setValue(bean, key.toString(), field.getString("value"));
    }

    return bean;
  }
}
