package com.clickntap.hub;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.clickntap.tool.bean.Bean;
import com.clickntap.utils.BindUtils;

public class BO extends Bean {

	public BO() {
	}

	public BO(HttpServletRequest request) throws Exception {
		ServletRequestDataBinder binder = new ServletRequestDataBinder(this, this.getClass().getName());
		BindUtils.registerCustomEditor(binder);
		binder.bind(request);
	}

	public BOManager getApp() throws Exception {
		BOManager manager = null;
		try {
			manager = (BOManager) getBeanManager();
		} catch (ClassCastException e) {
			manager = new BOManager();
			manager.setBeanManager(getBeanManager());
		}
		return manager;
	}

	public void setApp(BOManager app) {
		setBeanManager(app);
	}

	public JSONObject toJSON() {
		return new JSONObject(this);
	}
}
