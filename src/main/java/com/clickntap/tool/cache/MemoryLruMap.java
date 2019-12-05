package com.clickntap.tool.cache;

import java.util.LinkedHashMap;

public class MemoryLruMap<K, V> extends LinkedHashMap<K, V> {
	private int capacity;

	public MemoryLruMap(int capacity) {
		super(capacity + 1, 1.0f, true);
		this.capacity = capacity;
	}

	protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
		return (size() > this.capacity);
	}
}
