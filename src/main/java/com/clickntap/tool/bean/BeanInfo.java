package com.clickntap.tool.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import com.clickntap.tool.jdbc.JdbcBlobber;

public class BeanInfo {
	private String beanName;

	private String cacheName;

	private int cacheMaxSize;

	private String createScript;

	private String currValScript;

	private String updateScript;

	private String deleteScript;

	private Map<String, String> executeScriptMap;

	private Map<String, String> readScriptMap;

	private Map<String, String> readListScriptMap;

	private Map<String, BlobInfo> blobInfoMap;

	private ValidationInfo validationInfo;

	public BeanInfo(Document document, String beanName, BeanInfo beanInfo) throws Exception {
		if (document != null) {
			this.beanName = beanName;
			Element root = document.getRootElement();
			cacheName = root.attributeValue("cache");
			try {
				cacheMaxSize = Integer.parseInt(root.attributeValue("cache-max-size"));
			} catch (Exception e) {
				cacheMaxSize = Integer.MAX_VALUE;
			}
			createScript = getText(root, "create");
			currValScript = getText(root, "curr-val");
			updateScript = getText(root, "update");
			deleteScript = getText(root, "delete");
			executeScriptMap = new HashMap<String, String>();
			loadScriptMap(root, "execute", executeScriptMap, beanInfo);
			readScriptMap = new HashMap<String, String>();
			loadScriptMap(root, "read", readScriptMap, beanInfo);
			readListScriptMap = new HashMap<String, String>();
			loadScriptMap(root, "read-list", readListScriptMap, beanInfo);

			List<Element> elementList;
			blobInfoMap = new HashMap<String, BlobInfo>();
			elementList = root.elements("blob");
			for (Element element : elementList) {
				blobInfoMap.put(element.attributeValue("db"), new BlobInfo(element));
			}
			Element element = root.element("validation");
			if (element != null)
				validationInfo = new ValidationInfo(element);
		}
	}

	private String getText(Element root, String tagName) {
		Element element;
		return (element = root.element(tagName)) != null ? element.getTextTrim() : null;
	}

	private void loadScriptMap(Element root, String tagName, Map<String, String> map, BeanInfo beanInfo) {
		if (beanInfo != null && beanInfo.getExecuteScriptMap() != null) {
			for (String key : beanInfo.getExecuteScriptMap().keySet()) {
				map.put(key, beanInfo.getExecuteScriptMap().get(key));
			}
		}
		if (root != null) {
			Map<String, String> beanMap = new HashMap<String, String>();
			List<Element> elementList;
			elementList = root.elements(tagName);
			for (Element element : elementList) {
				String key = element.attributeValue("name");
				beanMap.put(key, element.getTextTrim());
			}
			for (String key : beanMap.keySet()) {
				map.put(key, beanMap.get(key));
			}
		}
	}

	public boolean isCacheEnabled() {
		return getCacheName() != null;
	}

	public String getCacheName() {
		return cacheName;
	}

	public String getCreateScript() {
		return createScript;
	}

	public String getCurrValScript() {
		return currValScript;
	}

	public String getReadScript(String key) {
		return readScriptMap.get(key);
	}

	public String getExecuteScript(String key) {
		return executeScriptMap.get(key);
	}

	public String getDeleteScript() {
		return deleteScript;
	}

	public String getUpdateScript() {
		return updateScript;
	}

	public String getReadListScript(String key) {
		return readListScriptMap.get(key);
	}

	public ValidationInfo getValidationInfo() {
		return validationInfo;
	}

	public String getBeanName() {
		return beanName;
	}

	public BlobInfo getBlobInfo(String db) {
		if (!blobInfoMap.containsKey(db))
			throw new RuntimeException("unsupported database '" + db + "'");
		return blobInfoMap.get(db);
	}

	public Map<String, String> getExecuteScriptMap() {
		return executeScriptMap;
	}

	public class BlobInfo {
		private JdbcBlobber blobber;

		private List<String> updateScriptList;

		private String readScript;

		public BlobInfo(Element root) throws Exception {
			blobber = (JdbcBlobber) Class.forName(root.attributeValue("class")).newInstance();
			List<Element> elementList = root.elements("update");
			updateScriptList = new ArrayList<String>(elementList.size());
			for (Element element : elementList) {
				updateScriptList.add(element.getTextTrim());
			}
			Element element = root.element("read");
			if (element != null) {
				readScript = element.getTextTrim();
			}
		}

		public JdbcBlobber getBlobber() {
			return blobber;
		}

		public List<String> getUpdateScriptList() {
			return updateScriptList;
		}

		public String getReadScript() {
			return readScript;
		}

	}

	public class ValidationInfo {
		private Map<String, List<String>> validationScriptListMap;

		public ValidationInfo(Element root) throws Exception {
			List<Element> elementList = root.elements("group");
			validationScriptListMap = new HashMap<String, List<String>>(elementList.size());
			for (Element element : elementList) {
				String[] keys = StringUtils.commaDelimitedListToStringArray(element.attributeValue("name"));
				for (String key : keys) {
					List<String> scriptList = getValidationScriptList(key);
					scriptList.add(element.getTextTrim());
				}
			}
		}

		public List<String> getValidationScriptList(String key) {
			if (validationScriptListMap.containsKey(key)) {
				return validationScriptListMap.get(key);
			} else {
				List<String> validationScriptList = new ArrayList<String>();
				validationScriptListMap.put(key, validationScriptList);
				return validationScriptList;
			}
		}
	}

	public int getCacheMaxSize() {
		return cacheMaxSize;
	}

}
