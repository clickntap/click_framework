package com.clickntap.tool.cache;

import java.util.Arrays;
import java.util.List;

public class EhCacheManager implements CacheManager {
	private net.sf.ehcache.CacheManager cacheManager;

	public EhCacheManager() {
		cacheManager = net.sf.ehcache.CacheManager.create();
	}

	public Cache getCache(String cacheName, int maxSize) throws Exception {
		return new EhCache(cacheManager, cacheName, maxSize);
	}

	public boolean containsCache(String cacheName) {
		return cacheManager.cacheExists(cacheName);
	}

	public void reset() {
		cacheManager.clearAll();
	}

	public void shutdown() {
		cacheManager.shutdown();
	}

	public List<String> getCacheNames() throws Exception {
		return Arrays.asList(cacheManager.getCacheNames());
	}
}
