package com.clickntap.tool.cache;

import java.io.Serializable;

public class MemoryCache implements Cache {

	private MemoryLruMap<Serializable, Serializable> memory;

	public MemoryCache() {
		memory = new MemoryLruMap<Serializable, Serializable>(999999);
	}

	public Serializable get(Serializable key) throws Exception {
		synchronized (memory) {
			return memory.get(key);
		}
	}

	public Serializable put(Serializable key, Serializable value) throws Exception {
		if (contains(key))
			return value;
		synchronized (memory) {
			return memory.put(key, value);
		}
	}

	public void remove(Serializable key) throws Exception {
		synchronized (memory) {
			memory.remove(key);
		}
	}

	public void removeAll() throws Exception {
		synchronized (memory) {
			memory.clear();
		}
	}

	public boolean contains(Serializable key) throws Exception {
		synchronized (memory) {
			return memory.containsKey(key);
		}
	}

}
