package com.clickntap.tool.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryCacheManager implements CacheManager {

	private HashMap<String, MemoryCache> caches;

	public MemoryCacheManager() {
		caches = new HashMap<String, MemoryCache>();
	}

	public Cache getCache(String cacheName, int maxSize) throws Exception {
		synchronized (caches) {
			if (!caches.containsKey(cacheName))
				caches.put(cacheName, new MemoryCache());
			return caches.get(cacheName);
		}
	}

	public boolean containsCache(String cacheName) {
		return true;
	}

	public void reset() {
		synchronized (caches) {
			caches = new HashMap<String, MemoryCache>();
		}
	}

	public List<String> getCacheNames() throws Exception {
		synchronized (caches) {
			List<String> names = new ArrayList<String>();
			names.addAll(caches.keySet());
			return names;
		}
	}

}
