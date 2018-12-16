package com.clickntap.tool.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.clickntap.utils.ImageUtils;

public class Mosaic {

	private BufferedImage image;

	public static double colourDistance(Color e1, Color e2) {
		long rmean = ((long) e1.getRed() + (long) e2.getRed()) / 2;
		long r = (long) e1.getRed() - (long) e2.getRed();
		long g = (long) e1.getGreen() - (long) e2.getGreen();
		long b = (long) e1.getBlue() - (long) e2.getBlue();
		return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
	}

	public List<Number> exec() {
		List<Number> map = new ArrayList<Number>();
		int i = 0;
		int j = 0;
		for (i = 0; i < image.getWidth(); i += image.getWidth() / 60) {
			for (j = 0; j < image.getHeight(); j += image.getHeight() / 60) {
				if (image.getRGB(i, j) == 0xFFFFFF) {
					map.add(0);
				} else {
					map.add(1);
				}
			}
		}
		return map;
	}

	public void setInput(String filename) throws Exception {
		image = ImageUtils.open(filename);
	}

	public void setInput(File file) throws Exception {
		image = ImageUtils.open(file);
	}

	public void setInput(InputStream in) throws Exception {
		image = ImageUtils.open(in);
	}

}
