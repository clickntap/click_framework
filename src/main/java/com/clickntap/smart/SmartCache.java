package com.clickntap.smart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;

import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.cache.Cache;
import com.clickntap.tool.cache.CacheManager;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;

public class SmartCache {

	private Cache cache;

	public static String getUniqueKey(SmartContext ctx) {
		String ukey = ctx.getRef();
		Enumeration<String> e = ctx.getRequest().getParameterNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			ukey += ConstUtils.MINUS + key + ConstUtils.MINUS + ctx.param(key);
		}
		return ukey;
	}

	public static boolean isCached(SmartContext ctx, Long lastModified) throws Exception {
		SmartCache smartCache = (SmartCache) ctx.getBean("smartCache");
		if (smartCache != null) {
			String ukey = SmartCache.getUniqueKey(ctx);
			SmartCacheItem item = smartCache.get(ukey);
			if (item != null && item.getLastModified() == lastModified)
				return true;
			smartCache.remove(ukey);
		}
		return false;
	}

	public static boolean cacheControl(SmartContext ctx) throws Exception {
		return getTarget(ctx) != null;
	}

	public static boolean isCached(SmartContext ctx) throws Exception {
		Object target = getTarget(ctx);
		if (target == null)
			return false;
		Long lastModified = (Long) BeanUtils.getValue(target, "lastModified");
		return isCached(ctx, lastModified);
	}

	private static Object getTarget(SmartContext ctx) {
		Object target;
		try {
			target = ctx.get(ctx.getController().getCacheAction().getTarget());
		} catch (Throwable e) {
			return null;
		}
		return target;
	}

	public static void handleResponse(SmartContext ctx) throws Exception {
		SmartCache smartCache = (SmartCache) ctx.getBean("smartCache");
		SmartCacheItem item = smartCache.get(SmartCache.getUniqueKey(ctx));
		ctx.getResponse().setContentType(item.getContentType());
		String range = ctx.getRequest().getHeader("Range");
		if (range != null) {
			int x1 = range.indexOf("=");
			int x2 = range.indexOf("-");
			int start = Integer.parseInt(range.substring(x1 + 1, x2));
			int end = Integer.parseInt(range.substring(x2 + 1));
			ctx.getResponse().setContentLength(end - start);
			IOUtils.copy(new ByteArrayInputStream(item.getData()), ctx.getResponse().getOutputStream(), start, end - start);
		} else {
			ctx.getResponse().setContentLength(item.getData().length);
			ctx.getResponse().getOutputStream().write(item.getData());
		}
	}

	public static void handleResponse(SmartContext ctx, ByteArrayOutputStream out) throws Exception {
		SmartCache smartCache = (SmartCache) ctx.getBean("smartCache");
		Object target = getTarget(ctx);
		Long lastModified = (Long) BeanUtils.getValue(target, "lastModified");
		SmartCacheItem item = new SmartCacheItem();
		item.setData(out.toByteArray());
		item.setLastModified(lastModified);
		item.setContentType(ctx.getResponse().getContentType());
		smartCache.put(SmartCache.getUniqueKey(ctx), item);
		handleResponse(ctx);
	}

	public void setCacheManager(CacheManager cacheManager) throws Exception {
		this.cache = cacheManager.getCache("smartCache", Integer.MAX_VALUE);
	}

	private SmartCacheItem get(String ukey) throws Exception {
		return (SmartCacheItem) cache.get(ukey);
	}

	private void remove(String ukey) throws Exception {
		cache.remove(ukey);
	}

	private void put(String ukey, SmartCacheItem item) throws Exception {
		cache.put(ukey, item);
	}
}
