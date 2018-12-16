package com.clickntap.tool.cache;

import java.io.Serializable;

public interface Cache {
	public Serializable get(Serializable key) throws Exception;

	public Serializable put(Serializable key, Serializable value) throws Exception;

	public void remove(Serializable key) throws Exception;

	public void removeAll() throws Exception;

	public boolean contains(Serializable key) throws Exception;
}
