package com.clickntap.smart;

import com.clickntap.hub.BOManager;
import com.clickntap.tool.script.ScriptEngine;

public enum AppSingleton {
	INSTANCE;

	private BOManager app;
	private ScriptEngine viewEngine;

	public BOManager getApp() {
		return app;
	}

	public void setApp(BOManager app) {
		this.app = app;
	}

	public ScriptEngine getViewEngine() {
		return viewEngine;
	}

	public void setViewEngine(ScriptEngine viewEngine) {
		this.viewEngine = viewEngine;
	}
}
