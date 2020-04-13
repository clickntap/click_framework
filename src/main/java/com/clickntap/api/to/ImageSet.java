package com.clickntap.api.to;

import java.io.Serializable;
import java.util.List;

public class ImageSet implements Serializable {
	private List<byte[]> images;

	public List<byte[]> getImages() {
		return images;
	}

	public void setImages(List<byte[]> images) {
		this.images = images;
	}

}
