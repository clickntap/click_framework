package com.clickntap.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.dom4j.Element;
import org.json.JSONObject;
import org.springframework.core.io.Resource;

import com.cathive.sass.SassContext;
import com.cathive.sass.SassFileContext;
import com.cathive.sass.SassOptions;
import com.cathive.sass.SassOutputStyle;
import com.clickntap.api.ApiUtils;
import com.clickntap.api.HttpUtils;
import com.clickntap.hub.App;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;
import com.clickntap.utils.SecurityUtils;
import com.clickntap.utils.XMLUtils;

public class BuildCompiler implements FileAlterationListener {
	private Resource uiWorkDir;
	private App app;
	private FileAlterationMonitor monitor;
	private boolean compress;

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public void init() throws Exception {
		File directory = getUiWorkDir().getFile();
		FileAlterationObserver observer = new FileAlterationObserver(directory);
		observer.addListener(this);
		monitor = new FileAlterationMonitor(500);
		monitor.addObserver(observer);
		monitor.start();
	}

	public Element getConf() throws Exception {
		File confFile = new File(uiWorkDir.getFile().getCanonicalPath() + "/lib/libs.xml");
		Element root = XMLUtils.copyFrom(confFile).getRootElement();
		return root;
	}

	public void destroy() throws Exception {
		monitor.stop();
	}

	public Resource getUiWorkDir() {
		return uiWorkDir;
	}

	public void setUiWorkDir(Resource uiWorkDir) {
		this.uiWorkDir = uiWorkDir;
	}

	public String src(String resource) throws Exception {
		File file = new File(getUiWorkDir().getFile().getParentFile() + "/" + resource);
		if (file.exists()) {
			return resource + "?" + SecurityUtils.md5(file);
		}
		return ConstUtils.EMPTY;
	}

	public String http(String resource) throws Exception {
		return HttpUtils.get(resource);
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

	public FreemarkerScriptEngine getEngine() throws Exception {
		FreemarkerScriptEngine engine = new FreemarkerScriptEngine();
		engine.setTemplateDir(getUiWorkDir());
		engine.setExtension(ConstUtils.EMPTY);
		engine.setUpdateDelay(0);
		engine.start();
		return engine;
	}

	public File precompile(File file) {
		File f = tmpFile(file);
		try {
			String templateName = templateName(file);
			Map<String, Object> ctx = new HashMap<String, Object>();
			ctx.put(ConstUtils.THIS, this);
			FileOutputStream out = new FileOutputStream(f);
			getEngine().eval(ctx, templateName, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	protected File tmpFile(File file) {
		return new File(file.getParentFile(), file.getName() + ".tmp");
	}

	public String templateName(File file) {
		String templateName = null;
		try {
			String dirPath = getUiWorkDir().getFile().getCanonicalPath();
			String filePath = file.getCanonicalPath();
			templateName = filePath.substring(dirPath.length() + 1);
		} catch (IOException e) {
		}
		return templateName;
	}

	private void lessCompile(File file) {
		if (file.getName().equals("conf.less"))
			return;
		long lo = System.currentTimeMillis();
		File tmpFile = null;
		try {
			tmpFile = precompile(file);
			LessUtils.compile(tmpFile);
			File cssFile = new File(tmpFile.getAbsolutePath().replace(".tmp", ".css"));
			File destFile = new File(tmpFile.getParentFile().getAbsolutePath() + "/css/" + tmpFile.getName().replace(".less.tmp", ".css"));
			destFile.getParentFile().mkdirs();
			if (getUiWorkDir().getFile().getAbsolutePath().equals(file.getParentFile().getAbsolutePath())) {
				StringBuffer sb = libs("css");
				sb.append(FileUtils.readFileToString(cssFile, ConstUtils.UTF_8));
				cssFile.delete();
				FileUtils.writeStringToFile(destFile, sb.toString(), ConstUtils.UTF_8);
			} else {
				cssFile.renameTo(destFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				tmpFile.delete();
			} catch (Exception e) {
			}
		}
		System.out.println(file.getName() + " compiled in " + (System.currentTimeMillis() - lo) + " millis");
	}

	public String jsCompress(String js) throws Exception {
		if (compress) {
			return ApiUtils.jsCompile(js);
		} else {
			return js;
		}
	}

	private void jsCompile(File file) {
		long lo = System.currentTimeMillis();
		File tmpFile = null;
		try {
			tmpFile = precompile(file);
			File destFile = new File(tmpFile.getParentFile().getAbsolutePath() + "/js/" + tmpFile.getName().replace(".js.tmp", ".js"));
			destFile.getParentFile().mkdirs();
			if (getUiWorkDir().getFile().getAbsolutePath().equals(file.getParentFile().getAbsolutePath())) {
				StringBuffer sb = libs("js");
				sb.append(jsCompress(FileUtils.readFileToString(tmpFile, ConstUtils.UTF_8)));
				tmpFile.delete();
				FileUtils.writeStringToFile(destFile, sb.toString(), ConstUtils.UTF_8);
			} else {
				tmpFile.renameTo(destFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				tmpFile.delete();
			} catch (Exception e) {
			}
		}
		System.out.println(file.getName() + " compiled in " + (System.currentTimeMillis() - lo) + " millis");
	}

	private StringBuffer libs(String name) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (Element lib : (List<Element>) getConf().elements("lib")) {
			File libFile = new File(getUiWorkDir().getFile().getCanonicalPath() + "/lib/" + name + "/" + lib.attributeValue("src"));
			if (FilenameUtils.getExtension(lib.attributeValue("src")).equals(name)) {
				sb.append('\n');
				sb.append(FileUtils.readFileToString(libFile, ConstUtils.UTF_8));
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb;
	}

	private void compile(File changedFile) throws Exception {
		String extension = FilenameUtils.getExtension(changedFile.getName());
		if (extension.equals("less")) {
			lessCompile(changedFile);
		}
		if (extension.equals("js")) {
			jsCompile(changedFile);
		}
		if (extension.equals("sass")) {
			Path srcRoot = Paths.get(changedFile.getParentFile().getCanonicalPath());
			SassContext ctx = SassFileContext.create(srcRoot.resolve(changedFile.getName()));
			SassOptions options = ctx.getOptions();
			options.setOutputStyle(SassOutputStyle.COMPRESSED);
			File minFile = new File(changedFile.getParentFile().getAbsolutePath() + "/" + changedFile.getName().replace(".sass", ".css"));
			FileUtils.writeStringToFile(minFile, ctx.compile(), ConstUtils.UTF_8);
		}
	}

	public void onFileChange(File changedFile) {
		try {
			String filePath = changedFile.getAbsolutePath();
			String dirPath = uiWorkDir.getFile().getAbsolutePath();
			filePath = filePath.replace(dirPath, "");
			int n = 0;
			int x = 0;
			while ((x = filePath.indexOf('/')) >= 0) {
				n++;
				filePath = filePath.substring(x + 1);
			}
			if (n <= 4) {
				String extension = FilenameUtils.getExtension(changedFile.getName());
				if (extension.equalsIgnoreCase("js") || extension.endsWith("ss")) {
					String code = FileUtils.readFileToString(changedFile, ConstUtils.UTF_8).trim();
					String formattedCode = ApiUtils.codeFormat(code).trim();
					if (!code.equals(formattedCode)) {
						FileUtils.writeByteArrayToFile(changedFile, formattedCode.trim().getBytes(ConstUtils.UTF_8));
						return;
					}
				}
				if (changedFile.getParentFile().getAbsolutePath().endsWith(extension)) {
					return;
				}
				File workDir = getUiWorkDir().getFile();
				File fileDir = changedFile.getParentFile();
				if (workDir.getAbsolutePath().equalsIgnoreCase(fileDir.getAbsolutePath())) {
					compile(changedFile);
				} else {
					for (File file : getUiWorkDir().getFile().listFiles()) {
						String fileExtension = FilenameUtils.getExtension(file.getName());
						if ((fileExtension.equalsIgnoreCase("js") && extension.equalsIgnoreCase("js")) || (fileExtension.endsWith("ss") && extension.endsWith("ss"))) {
							compile(file);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onFileDelete(File file) {

	}

	public void onStop(FileAlterationObserver observer) {

	}

}
