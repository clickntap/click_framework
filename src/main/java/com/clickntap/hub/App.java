package com.clickntap.hub;

import org.springframework.core.io.Resource;

public class App extends BOManager {
	public void init() throws Exception {

	}

	public void sync() throws Exception {

	}

	private Resource workDir;

	public void setWorkDir(Resource workDir) {
		this.workDir = workDir;
	}

	public Resource getWorkDir() {
		return workDir;
	}

}
