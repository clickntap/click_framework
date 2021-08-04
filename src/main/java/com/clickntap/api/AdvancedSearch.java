package com.clickntap.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.clickntap.hub.BOManager;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.jdbc.JdbcManager;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

import freemarker.template.utility.StringUtil;

public class AdvancedSearch {

	private JdbcManager db;
	private String searchTemplate;
	private BOApp app;

	public AdvancedSearch() throws Exception {
		init();
	}

	public void setApp(BOApp app) {
		this.app = app;
	}

	private void init() throws Exception {
		Resource queryResource = new ClassPathResource("com/clickntap/api/query.sql");
		InputStream in = queryResource.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		searchTemplate = new String(out.toByteArray(), ConstUtils.UTF_8);
		out.close();
		in.close();
	}

	public int count(JSONObject json, SmartContext ctx, JSONObject data) throws Exception {
		AdvancedSearchFilter filter = new AdvancedSearchFilter();

		try {
			data.put("limit", json.get("limit"));
			data.put("from", json.get("from"));
		} catch (Exception e) {
		}
		filter.setCount(true);
		filter.setJson(json);
		for (BO bo : (List<BO>) db.queryScript(searchTemplate, filter, Class.forName(json.getString("class")))) {
			return Integer.parseInt(bo.get("count").toString());
		}
		return 0;
	}

	public List<JSONObject> sql(String sql, SmartContext ctx, JSONObject data) throws Exception {
		List<JSONObject> items = new ArrayList<JSONObject>();
		AdvancedSearchFilter filter = new AdvancedSearchFilter();
		List list = db.queryScript(sql, filter, Class.forName("com.clickntap.api.BO"));
		for (BO bo : (List<BO>) list) {
			bo.setApp((BOManager) ctx.getBean("app"));
			JSONObject item = new JSONObject();
			item.put("id", bo.getId());
			for (String name : bo.valuesKeySet()) {
				try {
					item.put(name, bo.get(name));
				} catch (Exception e) {
				}
			}
			items.add(item);
		}
		return items;
	}

	public List<JSONObject> run(JSONObject json, SmartContext ctx, JSONObject data, boolean sortable) throws Exception {
		AdvancedSearchFilter filter = new AdvancedSearchFilter();
		try {
			String[] sorts = StringUtil.split(ctx.param("sort"), ',');
			List<JSONObject> sort = new ArrayList<JSONObject>();
			for (int i = 0; i < sorts.length; i++) {
				String[] parts = StringUtil.split(sorts[i], '|');
				JSONObject item = new JSONObject();
				item.put("name", parts[0]);
				item.put("type", parts[1]);
				sort.add(item);
			}
			if (sortable) {
				json.put("sort", sort);
			}
		} catch (Exception e) {
		}

		filter.setCount(false);
		filter.setJson(json);
		List<JSONObject> items = new ArrayList<JSONObject>();
		List list = db.queryScript(searchTemplate, filter, Class.forName(json.getString("class")));
		for (BO bo : (List<BO>) list) {
			if (app == null) {
				bo.setApp((BOManager) ctx.getBean("app"));
			} else {
				bo.setApp(app);
			}
			JSONObject item = new JSONObject();
			item.put("id", bo.getId());
			item.put("className", bo.getClass().getSimpleName());
			if (json.has("fields")) {
				JSONArray fields = json.getJSONArray("fields");
				for (int i = 0; i < fields.length(); i++) {
					String field = fields.getString(i);
					String fieldKey = field;
					if (field.contains(".")) {
						fieldKey = fieldKey.replace(".", "_");
					}
					if (field.contains("[")) {
						fieldKey = fieldKey.replace("[", "_").replace("]", "_");
					}
					fieldKey = AdvancedSearchFilter.toCamelCase(fieldKey);
					field = AdvancedSearchFilter.toCamelCase(field);
					try {
						if (field.startsWith("this.")) {
							field = field.substring(5);
						}
						Object value = BeanUtils.getValue(bo, field);
						if (value instanceof List) {
							List<JSONObject> values = new ArrayList<JSONObject>();
							for (BO valueItem : (List<BO>) value) {
								values.add(valueItem.json(false));
							}
							item.put(fieldKey, values);
						} else if (value instanceof BO) {
							item.put(fieldKey, ((BO) value).json(false));
						} else {
							item.put(fieldKey, value);
						}
					} catch (Exception e) {
					}
				}
			}
			if (json.has("selectFields")) {
				JSONArray fields = json.getJSONArray("selectFields");
				for (int i = 0; i < fields.length(); i++) {
					String name = fields.getJSONObject(i).getString("name");
					try {
						item.put(name, bo.get(name));
					} catch (Exception e) {
					}
				}
			}
			// System.out.println((System.currentTimeMillis()-t)+" millis");
			items.add(item);
		}

		return items;
	}

	public void setDb(JdbcManager db) {
		this.db = db;
	}

}
