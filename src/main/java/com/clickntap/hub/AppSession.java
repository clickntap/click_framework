package com.clickntap.hub;

import com.clickntap.smart.SmartContext;
import com.clickntap.utils.ConstUtils;

public class AppSession extends BO {
	protected SmartContext ctx;
	private String form;
	private String locale;

	public void clear() throws Exception {
		super.clear();
		setForm(ConstUtils.EMPTY);
		setLocale(ConstUtils.LOCALE_EN);
	}

	public void setCtx(SmartContext ctx) {
		this.ctx = ctx;
	}

	public App getApp() throws Exception {
		return (App) super.getApp();
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
