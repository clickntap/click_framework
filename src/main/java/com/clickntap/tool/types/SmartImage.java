package com.clickntap.tool.types;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.clickntap.utils.IOUtils;
import com.clickntap.utils.ImageUtils;

public class SmartImage {

	private BufferedImage image = null;

	private InputStream in = null;
	private int jpegQuality;

	public SmartImage() {
		jpegQuality = 0;
	}

	public SmartImage(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		set(in);
		load();
		in.close();
		jpegQuality = 0;
	}

	public SmartImage(SmartImage image) {
		this.image = image.getImage();
		jpegQuality = 0;
	}

	public SmartImage(BufferedImage image) {
		this.image = image;
		jpegQuality = 0;
	}

	public int getJpegQuality() {
		return jpegQuality;
	}

	public void setJpegQuality(int jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	public boolean isValid() {
		return image != null;
	}

	public BufferedImage getImage() {
		return image;
	}

	public SmartImage set(InputStream in) throws IOException {
		this.in = in;
		return this;
	}

	public SmartImage load() throws Exception {
		image = ImageUtils.open(in);
		if (image.getType() != BufferedImage.TYPE_INT_ARGB)
			scale(image.getWidth(), image.getHeight());
		return this;
	}

	public SmartImage create(int w, int h) throws IOException {
		image = ImageUtils.createImage(w, h);
		return this;
	}

	public SmartImage fill(int r, int g, int b, int a) throws IOException {
		ImageUtils.fillRect(image, new Color(r, g, b, a));
		return this;
	}

	public SmartImage rotate(float degr) {
		image = ImageUtils.rotate(image, degr);
		return this;
	}

	public SmartImage letterbox(int w, int h, int r, int g, int b, int a) throws Exception {
		image = ImageUtils.letterbox(image, w, h, new Color(r, g, b, a));
		return this;
	}

	public SmartImage scaleWidth(int w) throws Exception {
		image = ImageUtils.scaleWidth(image, w);
		return this;
	}

	public SmartImage scaleHeight(int h) throws Exception {
		image = ImageUtils.scaleHeight(image, h);
		return this;
	}

	public SmartImage scale(float f) throws Exception {
		image = ImageUtils.scale(image, f);
		return this;
	}

	public SmartImage scale(int w, int h) throws Exception {
		image = ImageUtils.scale(image, w, h);
		return this;
	}

	public SmartImage drawText(String s, int x, int y, int width, int height, float linespace, Font font, Color color) throws Exception {
		image = ImageUtils.drawText(image, s, x, y, width, height, linespace, font, color);
		return this;
	}

	public SmartImage drawImage(String file) throws Exception {
		image = ImageUtils.drawImage(image, file);
		return this;
	}

	public SmartImage drawImage(String file, int x, int y, int w, int h) throws Exception {
		image = ImageUtils.drawImage(image, file, x, y, w, h);
		return this;
	}

	public SmartImage drawImage(BufferedImage image2, int x, int y, int w, int h) throws Exception {
		image = ImageUtils.drawImage(image, image2, x, y, w, h);
		return this;
	}

	public void drawSubImage(String file, int x, int y, int w, int h) throws Exception {
		image = ImageUtils.drawSubImage(image, file, x, y, w, h);
	}

	public Rectangle2D getTextBound(String s, Font font) {
		return ImageUtils.getTextBound(image, s, font);
	}

	public SmartImage cropRect() throws Exception {
		image = ImageUtils.cropRect(image, 1);
		return this;
	}

	public SmartImage crop(int w, int h) {
		image = ImageUtils.crop(image, w, h);
		return this;
	}

	public SmartImage crop(float ratio) throws Exception {
		float imageRatio = (float) getWidth() / getHeight();
		if (ratio > imageRatio) {
			int offset = 0;
			int h = (int) (getHeight() * imageRatio / ratio);
			offset = (getHeight() - h) / 2;
			subImage(0, offset, getWidth(), h);
		} else {
			int offset = 0;
			int w = (int) (getWidth() * ratio / imageRatio);
			offset = (getWidth() - w) / 2;
			subImage(offset, 0, w, getHeight());
		}
		return this;
	}

	public SmartImage centeredSubImage(int w, int h) throws Exception {
		image = ImageUtils.centeredSubImage(image, w, h);
		return this;
	}

	public SmartImage subImage(int x, int y, int w, int h) throws Exception {
		image = ImageUtils.subImage(image, x, y, w, h);
		return this;
	}

	public SmartImage drawText(int x, int y, int w, int h, String s) throws Exception {
		image = ImageUtils.drawText(image, s, x, y, w, h, 1.0f, null, null);
		return this;
	}

	public SmartImage setAlpha(String alpha, int x, int y) throws Exception {
		return setAlpha(ImageUtils.open(alpha), x, y);
	}

	public SmartImage setAlpha(BufferedImage alpha, int x, int y) {
		Graphics2D graphics2D = image.createGraphics();
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F));
		graphics2D.drawImage(alpha, x, y, null);
		graphics2D.dispose();
		return this;
	}

	public void copyTo(File file) throws Exception {
		FileOutputStream out = new FileOutputStream(file);
		copyTo(out);
		out.close();
	}

	public void copyTo(OutputStream out) throws Exception {
		if (image != null) {
			if (jpegQuality > 0)
				ImageUtils.saveAsJpeg(image, jpegQuality, out);
			else
				ImageUtils.saveAs(image, "png", out);
			return;
		}
		if (in != null) {
			IOUtils.copy(in, out);
		}
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public SmartImage expand(int w, int h) throws IOException {
		image = ImageUtils.expand(image, w, h);
		return this;
	}

	public SmartImage expand(int w, int h, int x, int y) throws IOException {
		image = ImageUtils.expand(image, w, h, x, y);
		return this;
	}

	public SmartImage drawImage(BufferedImage newImage) throws IOException {
		image = ImageUtils.drawImage(image, newImage);
		return this;
	}

	public SmartImage cropBottom(int percentage) {
		image = ImageUtils.cropBottom(image, percentage);
		return this;
	}

}
