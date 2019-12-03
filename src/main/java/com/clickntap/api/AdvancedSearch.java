package com.clickntap.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
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
import com.clickntap.utils.IOUtils;

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

	private void init() throws Exception {
		Resource queryResource = new ClassPathResource("com/clickntap/api/query.sql");
		InputStream in = queryResource.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		searchTemplate = new String(out.toByteArray(), ConstUtils.UTF_8);
		out.close();
		in.close();
	}

	public int count(File query, SmartContext ctx, JSONObject data) throws Exception {
		AdvancedSearchFilter filter = new AdvancedSearchFilter();
		JSONObject json = new JSONObject(engine.evalScript(ctx, FileUtils.readFileToString(query, ConstUtils.UTF_8)));
		data.put("limit", json.get("limit"));
		data.put("from", json.get("from"));
		filter.setCount(true);
		filter.setJson(json);
		for (BO bo : (List<BO>) db.queryScript(searchTemplate, filter, Class.forName(json.getString("class")))) {
			return Integer.parseInt(bo.get("count").toString());
		}
		return 0;
	}

	public List<JSONObject> run(File query, SmartContext ctx, JSONObject data) throws Exception {
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
		filter.setCount(false);
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
			items.add(item);
		}
		data.put("size", items.size());
		return items;
	}

	public void setDb(JdbcManager db) {
		this.db = db;
	}

}
