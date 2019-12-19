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

	public AdvancedSearch() throws Exception {
		init();
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
		init();
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

	public List<JSONObject> run(JSONObject json, SmartContext ctx, JSONObject data, boolean sortable) throws Exception {
		
		init();
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
		List list =  db.queryScript(searchTemplate, filter, Class.forName(json.getString("class")));
		for (BO bo : (List<BO>) list) {
			bo.setApp((BOManager) ctx.getBean("app"));
			JSONObject item = new JSONObject();
			item.put("id", bo.getId());
			item.put("className", bo.getClass().getSimpleName());
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
			//System.out.println((System.currentTimeMillis()-t)+" millis");
			items.add(item);
		}
		
		return items;
	}

	public void setDb(JdbcManager db) {
		this.db = db;
	}

}
