package com.clickntap.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
import com.clickntap.hub.App;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;
import com.clickntap.utils.SecurityUtils;
import com.clickntap.utils.XMLUtils;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

public class BuildCompiler implements FileAlterationListener {
	private Resource uiWorkDir;
	private App app;
	private FileAlterationMonitor monitor;

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
		Compiler compiler = new Compiler();
		CompilerOptions options = new CompilerOptions();
		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		SourceFile source = SourceFile.fromCode("js", js);
		compiler.compile(Collections.<SourceFile>emptyList(), Collections.singletonList(source), options);
		return compiler.toSource();
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
				JSONObject json = new JSONObject();
				File svgDir = new File(getUiWorkDir().getFile().getCanonicalPath() + "/lib/svg");
				boolean svgsExists = false;
				for (File svg : svgDir.listFiles()) {
					if (FilenameUtils.getExtension(svg.getName()).equals("svg")) {
						json.put(FilenameUtils.getBaseName(svg.getName()), FileUtils.readFileToString(svg, ConstUtils.UTF_8));
						svgsExists = true;
					}
				}
				if (svgsExists) {
					sb.append("UI.svg(").append(json.toString()).append(");\n\n\n");
				}
				sb.append(FileUtils.readFileToString(tmpFile, ConstUtils.UTF_8));
				tmpFile.delete();
				FileUtils.writeStringToFile(destFile, jsCompress(sb.toString()), ConstUtils.UTF_8);
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

	private List<File> getFiles(String extension, File dir, boolean includeSiblings) throws Exception {
		List<File> files = new ArrayList<File>();
		for (File file : dir.listFiles()) {
			String fileExtension = FilenameUtils.getExtension(file.getName());
			if (file.isFile() && includeSiblings) {
				if (fileExtension.equals(extension)) {
					files.add(file);
				}
			}
		}
		if (!dir.equals(getUiWorkDir().getFile())) {
			files.addAll(getFiles(extension, dir.getParentFile(), true));
		}
		return files;
	}

	public void onFileChange(File changedFile) {
		try {
			if (changedFile.getName().equals("libs.xml")) {
				FileUtils.touch(new File(getUiWorkDir().getFile().getAbsolutePath() + "/lib/cnt/conf.less"));
			}
			String extension = FilenameUtils.getExtension(changedFile.getName());
			if (extension.equals("svg")) {
				for (File file : getUiWorkDir().getFile().listFiles()) {
					if (file.isFile()) {
						FileUtils.touch(file);
					}
				}
			}
			if (extension.equals("less")) {
				lessCompile(changedFile);
				List<File> files = getFiles("less", changedFile.getParentFile(), changedFile.getName().equals("conf.less"));
				for (File file : files) {
					lessCompile(file);
				}
			}
			if (extension.equals("sass")) {
				Path srcRoot = Paths.get(changedFile.getParentFile().getCanonicalPath());
				SassContext ctx = SassFileContext.create(srcRoot.resolve(changedFile.getName()));
				SassOptions options = ctx.getOptions();
				options.setOutputStyle(SassOutputStyle.COMPRESSED);
				File minFile = new File(changedFile.getParentFile().getAbsolutePath() + "/" + changedFile.getName().replace(".sass", ".css"));
				FileUtils.writeStringToFile(minFile, ctx.compile(), ConstUtils.UTF_8);
			}
			if (extension.equals("js") && !changedFile.getParentFile().getName().equals("js")) {
				jsCompile(changedFile);
				List<File> files = getFiles("js", changedFile.getParentFile(), true);
				for (File file : files) {
					String changedName = FilenameUtils.getBaseName(changedFile.getName());
					String name = FilenameUtils.getBaseName(file.getName());
					if (changedName.contains(name) && !name.equals(changedName)) {
						jsCompile(file);
					}
					if (file.getParentFile().getParentFile().getAbsolutePath().contains(name) && !name.equals(changedName)) {
						jsCompile(file);
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
