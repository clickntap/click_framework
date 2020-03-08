package com.clickntap.developers;

import com.caucho.hessian.client.HessianProxyFactory;

public class LibraryClient implements Library {

	public Library library;

	public LibraryClient(String url) throws Exception {
		library = (Library) new HessianProxyFactory().create(Library.class, url);
	}

	public void publishVersion(String json) throws Exception {
		library.publishVersion(json);
	}

	public String downloadVersion(String version) throws Exception {
		return library.downloadVersion(version);
	}

}
