package com.clickntap.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.clickntap.hub.BOManager;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.jdbc.JdbcManager;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;

import freemarker.template.utility.StringUtil;

public class AdvancedSearch {

	private JdbcManager db;
	private String searchTemplate;
	private FreemarkerScriptEngine engine;

	public AdvancedSearch() throws Exception {
		init();
		engine = new FreemarkerScriptEngine();
		engine.start();
	}

	private void init() throws IOException {
		Resource queryResource = new ClassPathResource("com/clickntap/api/query.sql");
		searchTemplate = FileUtils.readFileToString(queryResource.getFile(), ConstUtils.UTF_8);
	}

	public List<JSONObject> run(File query, SmartContext ctx) throws Exception {
		init();
		AdvancedSearchFilter filter = new AdvancedSearchFilter();
		JSONObject json = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(query, ConstUtils.UTF_8)));
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
			json.put("sort", sort);
		} catch (Exception e) {
		}
		filter.setJson(json);
		List<JSONObject> items = new ArrayList<JSONObject>();
		for (BO bo : (List<BO>) db.queryScript(searchTemplate, filter, Class.forName(json.getString("class")))) {
			bo.setApp((BOManager) ctx.getBean("app"));
			JSONObject item = new JSONObject();
			item.put("id", bo.getId());
			if (json.has("fields")) {
				JSONArray fields = json.getJSONArray("fields");
				for (int i = 0; i < fields.length(); i++) {
					String field;
					String fieldKey;
					if (fields.getString(i).contains(".")) {
						field = fields.getString(i);
						fieldKey = AdvancedSearchFilter.toCamelCase(field.replace(".", "_"));
					} else {
						fieldKey = field = AdvancedSearchFilter.toCamelCase(fields.getString(i));
					}
					try {
						item.put(fieldKey, BeanUtils.getValue(bo, field));
					} catch (Exception e) {
					}
				}
			}
			if (json.has("sqlFields")) {
				JSONArray fields = json.getJSONArray("sqlFields");
				for (int i = 0; i < fields.length(); i++) {
					String name = fields.getJSONObject(i).getString("name");
					try {
						item.put(name, bo.get(name));
					} catch (Exception e) {
					}
				}
			}
			items.add(item);
		}
		return items;
	}

	public void setDb(JdbcManager db) {
		this.db = db;
	}

}
