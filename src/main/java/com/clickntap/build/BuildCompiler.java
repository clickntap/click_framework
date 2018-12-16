package com.clickntap.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.core.io.Resource;

public class BuildCompiler implements FileAlterationListener {
	private List<com.clickntap.build.Compiler> compilers;
	private Resource uiWorkDir;
	private FileAlterationMonitor monitor;
	private long timestamp;

	public void init() throws Exception {
		timestamp = System.currentTimeMillis();
		compilers = new ArrayList<com.clickntap.build.Compiler>();
		{
			CascadingStyleSheetsCompiler compiler = new CascadingStyleSheetsCompiler();
			compiler.setWorkDir(getUiWorkDir());
			compilers.add(compiler);
		}
		{
			JavascriptCompiler compiler = new JavascriptCompiler();
			compiler.setWorkDir(getUiWorkDir());
			compilers.add(compiler);
		}
		File directory = getUiWorkDir().getFile();
		FileAlterationObserver observer = new FileAlterationObserver(directory);
		observer.addListener(this);
		monitor = new FileAlterationMonitor(500);
		monitor.addObserver(observer);
		monitor.start();
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

	public void onStart(FileAlterationObserver observer) {

	}

	public void onDirectoryCreate(File directory) {

	}

	public void onDirectoryChange(File directory) {

	}

	public void onDirectoryDelete(File directory) {

	}

	public void onFileCreate(File file) {
		onFileChange(file);
	}

	public void onFileChange(File file) {
		boolean ok = false;
		for (com.clickntap.build.Compiler compiler : compilers) {
			if (compiler.compilable(file)) {
				long lo = System.currentTimeMillis();
				try {
					compiler.precompile(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					compiler.compile(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println((System.currentTimeMillis() - lo) + " millis --> " + file.getName());
				timestamp = System.currentTimeMillis();
				ok = true;
			}
		}
		if (!ok) {
			try {
				for (File f : uiWorkDir.getFile().listFiles()) {
					if (f.isFile()) {
						if (file.getCanonicalPath().contains("lib")) {
							FileUtils.touch(f);
						}
						if (!f.getName().equals(file.getName()) && f.getParentFile().equals(file.getParentFile())) {
							FileUtils.touch(f);
						}
					}
				}
				System.out.println();
			} catch (Exception e) {
			}
		}
	}

	public void onFileDelete(File file) {
		onFileChange(file);
	}

	public void onStop(FileAlterationObserver observer) {

	}

	public long getTimestamp() {
		return timestamp;
	}

}
