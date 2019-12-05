package com.clickntap.autoarchive;

public interface AutoArchiver {
	public void archive() throws Exception;

	public void unarchive(Number id) throws Exception;
}
