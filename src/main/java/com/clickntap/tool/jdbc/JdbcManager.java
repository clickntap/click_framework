package com.clickntap.tool.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.utils.ConstUtils;

public class JdbcManager {

  private Log log = LogFactory.getLog(JdbcManager.class);

  private PlatformTransactionManager transactionManager;

  private JdbcTemplate jdbcTemplate;

  private ScriptEngine scriptEngine;

  private String db;

  public void batchUpdate(String[] sql) {
    jdbcTemplate.batchUpdate(sql);
  }

  public int updateScript(String script, Object object) {
    JdbcParams params = new JdbcParams(object);
    int i = 0;
    String sql = evalScript(script, params, object);
    if (!ConstUtils.EMPTY.equals(sql))
      i = jdbcTemplate.update(sql, new JdbcPreparedStatementCreator(params.toArray()));
    params.close();
    return i;
  }

  public List queryScript(String script, Object object, RowMapper mapper) {
    JdbcParams params = new JdbcParams(object);
    List resultList = null;
    String sql = evalScript(script, params, object);
    // System.out.println(sql);
    if (!ConstUtils.EMPTY.equals(sql))
      resultList = jdbcTemplate.query(sql, params.toArray(), mapper);
    params.close();
    return resultList;
  }

  public List queryScript(String script, Object object, Class beanClass) {
    JdbcParams params = new JdbcParams(object);
    if (script.contains("${autoarchive!}")) {
      List resultList = new ArrayList();
      resultList.addAll(query(script, object, beanClass, params));
      script = script.replace("${autoarchive!}", "autoarchive_");
      params.close();
      params = new JdbcParams(object);
      resultList.addAll(query(script, object, beanClass, params));
      params.close();
      return resultList;
    } else {
      List resultList = query(script, object, beanClass, params);
      params.close();
      return resultList;
    }
  }

  private List query(String script, Object object, Class beanClass, JdbcParams params) {
    List resultList = null;
    String sql = evalScript(script, params, object);
    // System.out.println(ApiUtils.codeFormat(sql, '(', ')'));
    // for (Object param : params.getParams()) {
    // System.out.print(" / ");
    // System.out.print(param);
    // }
    // System.out.println();
    if (!ConstUtils.EMPTY.equals(sql)) {
      if (log.isDebugEnabled()) {
        log.debug("template sql: " + sql);
      }
      resultList = (List) jdbcTemplate.query(sql, params.toArray(), new JdbcBeanRowMapper(beanClass));
    }
    return resultList;
  }

  public long queryScriptForLong(String script, Object object) {
    JdbcParams params = new JdbcParams(object);
    long lo = 0;
    String sql = evalScript(script, params, object);
    if (!ConstUtils.EMPTY.equals(sql))
      lo = jdbcTemplate.queryForObject(sql, params.toArray(), Number.class).longValue();
    params.close();
    return lo;
  }

  public Object execute(TransactionCallback transactionCallback) {
    return new TransactionTemplate(transactionManager).execute(transactionCallback);
  }

  private String evalScript(String script, JdbcParams params, Object object) {
    try {
      if (script == null) {
        throw new JdbcException("sql script is null, object: " + object);
      }
      Map<String, Object> map = new HashMap<String, Object>();
      if (params != null)
        map.put("this", params);
      String sql = scriptEngine.evalScript(map, script);
      log(sql, params.getParams());
      return sql;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void log(String sql, List<Object> params) {
    if (log.isDebugEnabled()) {
      try {
        for (Object param : params) {
          if (param != null) {
            sql = sql.replaceFirst("\\?", "'" + param + "'");
          } else {
            sql = sql.replaceFirst("\\?", "NULL");
          }
        }
      } catch (Throwable e) {
      }
      log.debug(sql);
    }
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public void setScriptEngine(ScriptEngine scriptEngine) {
    this.scriptEngine = scriptEngine;
  }
}
