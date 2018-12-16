package com.clickntap.tool.cache;

import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.Element;

import com.clickntap.tool.types.Datetime;

public class EhCache implements Cache {
	private net.sf.ehcache.Cache cache;

	public EhCache(net.sf.ehcache.CacheManager cacheManager, String cacheName, int maxSize) throws Exception {
		synchronized (cacheName) {
			cache = cacheManager.getCache(cacheName);
			if (cache == null) {
				cache = new net.sf.ehcache.Cache(cacheName, maxSize, false, false, 5 * Datetime.ONEHOURINSECONDS, 2 * Datetime.ONEHOURINSECONDS);
				cacheManager.addCache(cache);
				cache = cacheManager.getCache(cacheName);
			}
		}
	}

	public Serializable get(Serializable key) throws Exception {
		Element element = cache.get(key);
		return (Serializable) (element != null ? element.getObjectValue() : null);
	}

	public Serializable put(Serializable key, Serializable value) throws Exception {
		Element element = new Element(key, value);
		cache.put(element);
		return value;
	}

	public void remove(Serializable theKey) throws Exception {
		String rootKey = theKey.toString().substring(0, theKey.toString().lastIndexOf('-'));
		for (Object aKey : (List) cache.getKeys()) {
			if (aKey.toString().startsWith(rootKey)) {
				cache.remove(aKey);
			}
		}
	}

	public void removeAll() throws Exception {
		cache.removeAll();
	}

	public boolean contains(Serializable key) throws Exception {
		return cache.isKeyInCache(key);
	}
}
