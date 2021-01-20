package com.clickntap.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtils {

	public static String svg(String payload) throws Exception {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 0, 0, hints);
		StringBuilder sbPath = new StringBuilder();
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BitArray row = new BitArray(width);
		for (int y = 0; y < height; y++) {
			row = bitMatrix.getRow(y, row);
			for (int x = 0; x < width; x++) {
				if (row.get(x)) {
					sbPath.append("M").append(x).append(',').append(y).append("h1v1h-1z");
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 ").append(width).append(" ").append(height).append("\">\n");
		sb.append("<path stroke=\"none\" fill=\"black\" d=\"").append(sbPath.toString()).append("\"/>\n");
		sb.append("</svg>\n");
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		FileUtils.write(new File("qrcode.svg"), QRCodeUtils.svg("https://www.clickntap.com"));
	}

}
