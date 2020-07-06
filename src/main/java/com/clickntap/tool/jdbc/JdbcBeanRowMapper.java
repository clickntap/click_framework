package com.clickntap.tool.jdbc;

import java.io.ByteArrayOutputStream;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TimeZone;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.jdbc.core.RowMapper;

import com.clickntap.api.BO;
import com.clickntap.api.M;
import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.BindUtils;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

public class JdbcBeanRowMapper implements RowMapper {
  private Class beanClass;

  public JdbcBeanRowMapper(Class beanClass) {
    this.beanClass = beanClass;
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    Object bean = null;
    try {
      bean = beanClass.newInstance();
      MutablePropertyValues pvs = new MutablePropertyValues();
      for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
        Object o = rs.getObject(i);
        if (o instanceof Timestamp) {
          o = new Datetime(((Timestamp) o).getTime());
        }
        if (o instanceof Clob) {
          Clob clob = (Clob) o;
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          IOUtils.copy(clob.getAsciiStream(), out);
          o = new String(out.toByteArray(), ConstUtils.UTF_8);
        }
        String label = rs.getMetaData().getColumnLabel(i).trim();
        pvs.addPropertyValue(label, o);
        if (bean instanceof BO) {
          if (!M.has(bean, "get" + label.substring(0, 1).toUpperCase() + (label.length() > 1 ? label.substring(1) : ""))) {
            BO bo = (BO) bean;
            bo.put(label, o);
          }
        }
      }
      BindUtils.bind(bean, pvs);
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    }
    return bean;
  }
}
