package com.clickntap.developers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;

import com.clickntap.api.ApiUtils;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;

public class LibraryVersion implements FileAlterationListener {

	private String url;
	private String name;
	private String version;
	private File workDir;
	private LibraryClient library;
	private JSONObject data;
	private FreemarkerScriptEngine engine;
	private FileAlterationMonitor monitor;

	public void setUrl(String url) {
		this.url = url;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}

	public void publish(String name) throws Exception {
		JSONObject version = loadFiles();
		version.put("name", name);
		library.publishVersion(version.toString(2));
	}

	public void init() throws Exception {
		library = new LibraryClient(url);
		data = new JSONObject(library.downloadVersion(version));
		buildFiles();
		FileAlterationObserver observer = new FileAlterationObserver(workDir);
		observer.addListener(this);
		monitor = new FileAlterationMonitor(500);
		monitor.addObserver(observer);
		monitor.start();
	}

	public void destroy() throws Exception {
		monitor.stop();
	}

	private void buildFiles() throws Exception {
		JSONObject conf = new JSONObject();
		conf.put("version", version);
		conf.put("items", new JSONArray());
		File file = new File(workDir.getAbsoluteFile() + "/" + version + "/0");
		writeFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".less"), val(data, "less"));
		writeFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".js"), val(data, "js"));
		writeFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".html"), val(data, "html"));
		for (JSONObject item : (List<JSONObject>) SmartContext.asList(data.getJSONArray("items"))) {
			file = new File(file.getParentFile().getAbsoluteFile() + "/" + item.getString("position") + "_" + item.getString("name"));
			file.mkdirs();
			writeFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".less"), val(item, "less"));
			writeFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".js"), val(item, "js"));
			writeFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".html"), val(item, "html"));
			List confItems = SmartContext.asList(conf.getJSONArray("items"));
			JSONObject confItem = new JSONObject();
			confItem.put("position", item.getString("position"));
			confItem.put("name", item.getString("name"));
			confItems.add(confItem);
			conf.put("items", confItems);
		}
		FileUtils.writeStringToFile(jsonFile(), conf.toString(2), ConstUtils.UTF_8);
		engine = new FreemarkerScriptEngine();
		engine.setTemplateDir(new FileSystemResource(file.getParentFile()));
		engine.setExtension(ConstUtils.EMPTY);
		engine.start();
		compile();
		loadFiles();
	}

	private JSONObject loadFiles() throws Exception {
		JSONObject conf = new JSONObject(FileUtils.readFileToString(jsonFile(), ConstUtils.UTF_8));
		File file = new File(workDir.getAbsoluteFile() + "/" + version + "/0");
		conf.put("less", loadFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".less")));
		conf.put("js", loadFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".js")));
		conf.put("html", loadFile(new File(file.getAbsolutePath() + "/" + name.toLowerCase() + ".html")));
		for (JSONObject item : (List<JSONObject>) SmartContext.asList(conf.getJSONArray("items"))) {
			file = new File(file.getParentFile().getAbsoluteFile() + "/" + item.getString("position") + "_" + item.getString("name"));
			file.mkdirs();
			item.put("less", loadFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".less")));
			item.put("js", loadFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".js")));
			item.put("html", loadFile(new File(file.getAbsolutePath() + "/" + item.getString("name") + ".html")));
		}
		return conf;
	}

	private File jsonFile() {
		return new File(workDir.getAbsoluteFile() + "/" + version + "/0" + "/" + name.toLowerCase() + ".json");
	}

	private void writeFile(File file, String data) throws Exception {
		if (!file.exists()) {
			FileUtils.writeStringToFile(file, data, ConstUtils.UTF_8);
		}
	}

	private String loadFile(File file) throws Exception {
		if (file.exists()) {
			return FileUtils.readFileToString(file, ConstUtils.UTF_8);
		} else {
			return ConstUtils.EMPTY;
		}
	}

	private String val(JSONObject item, String name) {
		if (item.has(name)) {
			return item.getString(name);
		} else {
			return ConstUtils.EMPTY;
		}
	}

	private void compile() throws Exception {
		File versionDir = new File(workDir.getAbsolutePath() + "/" + version);
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("items", SmartContext.asList(new JSONObject(FileUtils.readFileToString(jsonFile(), ConstUtils.UTF_8)).getJSONArray("items")));
		ctx.put("this", this);
		String js = engine.eval(ctx, "0/" + name.toLowerCase() + ".js");
		js = ApiUtils.jsCompile(js);
		FileUtils.writeStringToFile(new File(versionDir.getAbsolutePath() + "/" + name.toLowerCase() + ".js"), js, ConstUtils.UTF_8);
		String less = engine.eval(ctx, "0/" + name.toLowerCase() + ".less");
		less = LessUtils.eval(less);
		FileUtils.writeStringToFile(new File(versionDir.getAbsolutePath() + "/" + name.toLowerCase() + ".css"), less, ConstUtils.UTF_8);
	}

	public String code(String position, String name, String format) {
		File versionDir = new File(workDir.getAbsolutePath() + "/" + version);
		try {
			Map<String, Object> ctx = new HashMap<String, Object>();
			File file = new File(versionDir.getAbsolutePath() + "/" + position + "_" + name + "/" + name + "." + format);
			return engine.evalScript(ctx, FileUtils.readFileToString(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ConstUtils.EMPTY;
	}

	public void onStart(FileAlterationObserver observer) {

	}

	public void onDirectoryCreate(File directory) {

	}

	public void onDirectoryChange(File directory) {

	}

	public void onDirectoryDelete(File directory) {
	}

	public void onFileCreate(File file) {

	}

	public void onFileChange(File file) {
		String filePath = file.getAbsolutePath();
		String dirPath = workDir.getAbsolutePath();
		filePath = filePath.replace(dirPath, "");
		int n = 0;
		int x = 0;
		while ((x = filePath.indexOf('/')) >= 0) {
			n++;
			filePath = filePath.substring(x + 1);
		}
		if (n > 2) {
			try {
				compile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onFileDelete(File file) {

	}

	public void onStop(FileAlterationObserver observer) {

	}
}
