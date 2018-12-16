package com.clickntap.tool.types;

import org.springframework.core.io.Resource;

public abstract class AbstractComponent implements Component {

	private Resource confResource;

	public Resource getConfResource() {
		return confResource;
	}

	public void setConfResource(Resource confResource) {
		this.confResource = confResource;
	}

	public void restart() throws Exception {
		start();
	}

}
