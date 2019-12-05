package com.clickntap.smart;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;

import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.utils.ConstUtils;

public class SmartView implements View {
	public final static String CACHE_CONTROL = "Cache-control";
	public final static String PRAGMA = "Pragma";
	public final static String NO_CACHE = "no-cache";
	private static Log log = LogFactory.getLog(SmartView.class);
	private String contentType;
	private Mode mode;
	private ScriptEngine viewEngine;
	private String binaryKey;

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void setBinaryKey(String binaryKey) {
		this.binaryKey = binaryKey;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object self = model.get(ConstUtils.THIS);
		SmartContext ctx = null;
		if (self instanceof SmartContext)
			ctx = (SmartContext) model.get(ConstUtils.THIS);
		else {
			ctx = new SmartContext(request, response);
			ctx.put(ConstUtils.THIS, self);
		}
		if (SmartCache.isCached(ctx)) {
			SmartCache.handleResponse(ctx);
			return;
		}
		OutputStream out = response.getOutputStream();
		if (SmartCache.cacheControl(ctx))
			out = new ByteArrayOutputStream();
		try {
			switch (mode) {
			case TEXT:
				response.setHeader(CACHE_CONTROL, NO_CACHE);
				response.setHeader(PRAGMA, NO_CACHE);
				response.setContentType(getContentType());
				response.setCharacterEncoding(ConstUtils.UTF_8);
				if (ctx.getException() != null) {
					throw ctx.getException();
				}
				viewEngine.eval(ctx, ctx.getRef(), out);
				break;
			case BINARY:
				Object outputBean = ctx.get(binaryKey);
				String contentType = null;
				try {
					contentType = BeanUtils.getValue(outputBean, "contentType").toString();
				} catch (Exception e) {
					contentType = null;
				}
				if (contentType == null)
					response.setContentType(getContentType());
				else {
					if (contentType.contains("UTF-8")) {
						response.setCharacterEncoding(ConstUtils.UTF_8);
					}
					response.setContentType(contentType);
				}
				if (ctx.getException() != null)
					throw ctx.getException();
				if (outputBean != null) {
					Method method = outputBean.getClass().getMethod("copyTo", OutputStream.class);
					if (method != null)
						method.invoke(outputBean, out);
				}
				break;
			}
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("template error", e);
			}
			if (ctx.getException() == null)
				ctx.setException(e);
			ctx.put(ConstUtils.THIS, ctx);
			viewEngine.eval(ctx, ctx.getController().getErrorRef(), out);
		}
		if (out instanceof ByteArrayOutputStream)
			SmartCache.handleResponse(ctx, (ByteArrayOutputStream) out);
	}

	public void setViewEngine(ScriptEngine viewEngine) {
		this.viewEngine = viewEngine;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public enum Mode {
		TEXT, BINARY
	}
}
