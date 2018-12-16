package com.clickntap.build;

import java.util.List;

public interface BOService {
	public List<FileInfo> build(String conf) throws Exception;
}
