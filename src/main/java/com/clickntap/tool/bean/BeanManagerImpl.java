package com.clickntap.tool.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.clickntap.api.CryptoUtils;
import com.clickntap.tool.bean.BeanInfo.BlobInfo;
import com.clickntap.tool.cache.Cache;
import com.clickntap.tool.cache.CacheManager;
import com.clickntap.tool.jdbc.JdbcManager;
import com.clickntap.tool.jdbc.JdbcOutputStreamRowMapper;
import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.utils.ConstUtils;

public class BeanManagerImpl implements BeanManager {
  public static final String ID_FILTER = "id";

  private JdbcManager jdbcManager;
  private CacheManager cacheManager;
  private Map<Class, BeanInfo> beanInfoMap;
  private ScriptEngine scriptEngine;
  private String channel;
  private Boolean cacheEnabled;
  protected CryptoUtils crypto;

  public void setCrypto(CryptoUtils crypto) {
    this.crypto = crypto;
  }

  public CryptoUtils getCrypto() {
    return crypto;
  }

  public Boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public Boolean getCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled(Boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public BeanManagerImpl() throws Exception {
    start();
  }

  public String getChannel() {
    if (channel == null)
      channel = "All";
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public void copyFrom(Bean bean, MultipartFile multipartFile) throws Exception {
    BeanInfo beanInfo = getBeanInfo(bean.getClass());
    validate(bean, beanInfo, "copyFrom");
    BlobInfo blobInfo = beanInfo.getBlobInfo(jdbcManager.getDb());
    blobInfo.getBlobber().update(bean, multipartFile, blobInfo.getUpdateScriptList(), jdbcManager);
  }

  public void copyTo(Bean bean, OutputStream out) throws Exception {
    copyTo(bean, out, 0, 0);
  }

  public void copyTo(Bean bean, OutputStream out, int offset, int length) throws Exception {
    BeanInfo beanInfo = getBeanInfo(bean.getClass());
    validate(bean, beanInfo, "copyTo");
    jdbcManager.queryScript(beanInfo.getBlobInfo(jdbcManager.getDb()).getReadScript(), bean, new JdbcOutputStreamRowMapper(out, offset, length));
  }

  public InputStream stream(Bean bean) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copyTo(bean, out);
    return new ByteArrayInputStream(out.toByteArray());
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#read(java.lang.Number,
   * java.lang.Class)
   */
  public Bean read(Number id, Class beanClass) throws Exception {
    return readByFilter(new Bean(id), ID_FILTER, beanClass);
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#read(java.lang.Object,
   * java.lang.String, java.lang.Class)
   */
  public Bean readByFilter(Bean filter, String filterName, Class beanClass) throws Exception {
    BeanInfo beanInfo = getBeanInfo(beanClass);

    // validate(filter, beanInfo, "read-" + filterName);

    Bean bean = null;
    Cache cache = null;
    Serializable key = null;
    if (isCacheEnabled() && beanInfo.isCacheEnabled() && filterName.equals(ID_FILTER)) {
      cache = cacheManager.getCache(beanInfo.getCacheName(), beanInfo.getCacheMaxSize());
      Number id = ((BeanId) filter).getId();
      if (id != null) {
        key = makeKey(beanInfo, id);
        bean = (Bean) cache.get(key);
      }
    }
    if (bean == null) {
      bean = readFromRepository(filter, filterName, beanClass);
      if (bean != null && cache != null && key != null) {
        cache.put(key, bean);
      }
    }
    if (bean != null) {
      ((Bean) bean).setBeanManager(this);
      return bean;
    }
    return null;
  }

  private String makeKey(BeanInfo beanInfo, Number id) {
    StringBuffer sb = new StringBuffer(beanInfo.getBeanName());
    sb.append(ConstUtils.MINUS_CHAR);
    if (id != null) {
      sb.append(id.toString());
    }
    sb.append(ConstUtils.MINUS_CHAR);
    sb.append(getChannel());
    // System.out.println(sb.toString());
    return sb.toString();
  }

  private Bean readFromRepository(Object filter, String filterName, Class beanClass) throws Exception {
    Bean bean = null;
    BeanInfo beanInfo = getBeanInfo(beanClass);
    List<Bean> resultList = jdbcManager.queryScript(beanInfo.getReadScript(filterName), filter, beanClass);
    bean = resultList.size() == 1 ? resultList.get(0) : null;
    if (bean != null)
      bean.setupAfterRead();
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.clickntap.tool.bean.BeanManagerInterface#create(com.clickntap.tool.
   * bean.Bean)
   */
  public Number create(Bean bean) throws Exception {
    Class beanClass = bean.getClass();
    BeanInfo beanInfo = getBeanInfo(beanClass);

    validate(bean, beanInfo, "create");

    Number id = (Number) jdbcManager.execute(new BeanCreator(bean, beanInfo, jdbcManager));
    if (id != null) {
      invalidateCache(beanInfo, id);
      return id;
    } else
      return null;
  }

  public void invalidateCache(BeanInfo beanInfo, Number id) throws Exception {
    if (beanInfo.isCacheEnabled())
      cacheManager.getCache(beanInfo.getCacheName(), beanInfo.getCacheMaxSize()).remove(makeKey(beanInfo, id));
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#update(com.clickntap.tool.
   * bean.Bean)
   */
  public int update(Bean bean) throws Exception {
    bean.setupBeforeUpdate();
    Class beanClass = bean.getClass();
    BeanInfo beanInfo = getBeanInfo(beanClass);

    validate(bean, beanInfo, "update");

    int i = jdbcManager.updateScript(beanInfo.getUpdateScript(), bean);
    if (beanInfo.isCacheEnabled())
      invalidateCache(beanInfo, bean.getId());
    return i;
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#delete(com.clickntap.tool.
   * bean.Bean)
   */
  public int delete(Bean bean) throws Exception {
    Class beanClass = bean.getClass();
    BeanInfo beanInfo = getBeanInfo(beanClass);

    validate(bean, beanInfo, "delete");

    int i = jdbcManager.updateScript(beanInfo.getDeleteScript(), bean);
    if (beanInfo.isCacheEnabled())
      invalidateCache(beanInfo, bean.getId());
    return i;
  }

  public BeanInfo getBeanInfo(Class beanClass) throws Exception {
    BeanInfo beanInfo = beanInfoMap.get(beanClass);
    if (beanInfo == null) {
      beanInfo = BeanUtils.beanTobeanInfo(beanClass);
      beanInfoMap.put(beanClass, beanInfo);
    }
    return beanInfo;
  }

  public void restart() throws Exception {
    start();
  }

  public void start() throws Exception {
    beanInfoMap = new HashMap<Class, BeanInfo>();
    cacheEnabled = true;
  }

  public void stop() throws Exception {
  }

  public void setCacheManager(CacheManager cacheManager) throws Exception {
    this.cacheManager = cacheManager;
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#readList(com.clickntap.tool
   * .bean.Bean, java.lang.String)
   */
  public List<Number> readList(Bean bean, String fieldName) throws Exception {
    return readListByFilter(bean.getClass(), bean, fieldName);
  }

  public int execute(Bean bean, String scriptName) throws Exception {
    return execute(bean.getClass(), bean, scriptName);
  }

  public int execute(Class beanClass, String scriptName) throws Exception {
    return execute(beanClass, null, scriptName);
  }

  private int execute(Class beanClass, Bean bean, String scriptName) throws Exception {
    BeanInfo beanInfo = getBeanInfo(beanClass);

    if (bean != null)
      validate(bean, beanInfo, "execute-" + scriptName);

    int i = jdbcManager.updateScript(beanInfo.getExecuteScript(scriptName), bean);
    if (beanInfo.isCacheEnabled())
      invalidateCache(beanInfo, bean.getId());
    return i;
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#readList(java.lang.Class,
   * java.lang.String)
   */
  public List<Number> readListByClass(Class beanClass, String fieldName) throws Exception {
    BeanInfo beanInfo = getBeanInfo(beanClass);
    return toIdList(jdbcManager.queryScript(beanInfo.getReadListScript(fieldName), null, BeanId.class));
  }

  public List exportList(Class beanClass, String fieldName) throws Exception {
    BeanInfo beanInfo = getBeanInfo(beanClass);
    List<Bean> beans = jdbcManager.queryScript(beanInfo.getReadListScript(fieldName), null, beanClass);
    for (Bean bean : beans) {
      bean.setBeanManager(this);
      bean.setupAfterRead();
    }
    return beans;
  }

  private List<Number> toIdList(List<BeanId> list) {
    List<Number> idList = new ArrayList<Number>(list.size());
    for (BeanId beanId : list) {
      idList.add(beanId.getId());
    }
    return idList;
  }

  /*
   * @see com.clickntap.tool.bean.BeanManagerInterface#readList(java.lang.Class,
   * com.clickntap.tool.bean.Bean, java.lang.String)
   */
  public List<Number> readListByFilter(Class beanClass, Bean bean, String fieldName) throws Exception {
    BeanInfo beanInfo = getBeanInfo(beanClass);

    // validate(bean, beanInfo, "readlist-" + fieldName);

    return toIdList(jdbcManager.queryScript(beanInfo.getReadListScript(fieldName), bean, BeanId.class));
  }

  public List exportList(Class beanClass, Bean bean, String fieldName) throws Exception {
    BeanInfo beanInfo = getBeanInfo(bean.getClass());

    // validate(bean, beanInfo, "exportlist-" + fieldName);

    return jdbcManager.queryScript(beanInfo.getReadListScript(fieldName), bean, beanClass);
  }

  public Validator getValidator(BeanInfo beanInfo, String validationGroup) throws Exception {
    return new BeanValidator(beanInfo, scriptEngine, validationGroup, this);
  }

  public Validator getValidator(Bean bean, String validationGroup) throws Exception {
    Validator validator = getValidator(getBeanInfo(bean.getClass()), validationGroup);
    if (validator == null)
      throw new Exception("validator '" + validationGroup + "'not found");
    else
      return validator;
  }

  public void setScriptEngine(ScriptEngine scriptEngine) {
    this.scriptEngine = scriptEngine;
  }

  private void validate(Object bean, BeanInfo beanInfo, String validationGroup) throws Exception, BindException {
    if (beanInfo.getValidationInfo() != null) {
      Validator validator = getValidator(beanInfo, validationGroup);
      BindException errors = new BindException(bean, beanInfo.getBeanName());
      ValidationUtils.invokeValidator(validator, bean, errors);
      if (errors.hasErrors())
        throw errors;
    }
  }

  public JdbcManager getJdbcManager() {
    return jdbcManager;
  }

  public void setJdbcManager(JdbcManager jdbcManager) {
    this.jdbcManager = jdbcManager;
  }

  public void batchUpdate(String[] sql) throws Exception {
    jdbcManager.batchUpdate(sql);
  }

  public Object executeTransaction(TransactionCallback transactionCallback) {
    return jdbcManager.execute(transactionCallback);
  }

  public PlatformTransactionManager getTransactionManager() {
    return jdbcManager.getTransactionManager();
  }

  public void init() throws Exception {

  }

}
