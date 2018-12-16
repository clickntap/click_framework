package com.clickntap.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class AsciiUtils {
	private static final String ENCODE_PRE = "&#";
	private static final String ENCODE_POST = ";";

	public static String limit(String text, int limit) {
		return text != null ? text.substring(0, Math.min(text.length(), limit)) + (text.length() > limit ? "..." : ConstUtils.EMPTY) : ConstUtils.EMPTY;
	}

	public static String utf7ToText(String text) {
		if (text != null)
			text = text.replace("&nbsp;", " ");
		int x0, x1, x2, code;
		while ((x0 = text.indexOf(ENCODE_PRE)) >= 0) {
			x2 = x1 = x0 + ENCODE_PRE.length();
			while (Character.isDigit(text.charAt(x2)))
				x2++;
			code = Integer.parseInt(text.substring(x1, x2));
			text = new StringBuffer(text.substring(0, x0)).append((char) code).append(text.substring(x2 + ENCODE_POST.length())).toString();
		}
		return text;
	}

	public static String textToUtf7(String text) {
		return textToUtf7(text.toCharArray());
	}

	public static String textToUtf7(char[] text) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length; i++)
			if (text[i] > 127)
				sb.append(ENCODE_PRE).append((int) text[i]).append(ENCODE_POST);
			else
				sb.append(text[i]);
		return sb.toString();
	}

	public static void binaryToText(String fileName1, String fileName2) throws IOException {
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		AsciiUtils.binaryToText(file1, file2);
	}

	public static void binaryToText(File file1, File file2) throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(file1);
		OutputStreamWriter out = new FileWriter(file2);
		AsciiUtils.binaryToText(in, out);
	}

	/*
	 * public static String toBinary(String in) throws IOException {
	 * StringInputStream input = new StringInputStream(in); ByteArrayOutputStream
	 * output = new ByteArrayOutputStream(); textToBinary(input, output); return
	 * output.toString(); }
	 * 
	 * public static String toText(String in) throws IOException {
	 * StringInputStream input = new StringInputStream(in); StringWriter output =
	 * new StringWriter(); binaryToText(input, output); return output.toString();
	 * }
	 */
	public static void binaryToText(InputStream in, Writer out) throws IOException {
		int i;
		String hex;
		while ((i = in.read()) != -1) {
			hex = Integer.toHexString(i);
			if (hex.length() == 1)
				out.write('0');
			out.write(hex);
		}
		in.close();
		out.close();
	}

	public static void textToBinary(String fileName1, String fileName2) throws IOException {
		File file1 = new File(fileName1);
		File file2 = new File(fileName2);
		AsciiUtils.textToBinary(file1, file2);
	}

	public static void textToBinary(File file1, File file2) throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(file1);
		OutputStream out = new FileOutputStream(file2);
		AsciiUtils.textToBinary(in, out);
	}

	public static void textToBinary(InputStream in, OutputStream out) throws IOException {
		byte[] c = new byte[2];
		while (in.read(c, 0, 2) != -1)
			out.write(AsciiUtils.bytesToInt(c));
		in.close();
		out.close();
	}

	public static int bytesToInt(byte[] b) {
		return AsciiUtils.byteToInt(b[0]) * 16 + AsciiUtils.byteToInt(b[1]);
	}

	public static int byteToInt(byte b) {
		switch (b) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		case 'a':
			return 10;
		case 'b':
			return 11;
		case 'c':
			return 12;
		case 'd':
			return 13;
		case 'e':
			return 14;
		case 'f':
			return 15;
		default:
			return -1;
		}
	}

	public static String webize(String text) {
		return webize(text.toLowerCase().toCharArray());
	}

	public static String webize(char[] text) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length; i++)
			if ((text[i] >= 'a' && text[i] <= 'z') || (text[i] >= '0' && text[i] <= '9') || text[i] == '-' || text[i] == '_' || text[i] == '.')
				sb.append(text[i]);
		return sb.toString().toLowerCase();
	}

	public static String phonize(String text) {
		return phonize(text.toLowerCase().toCharArray());
	}

	public static String phonize(char[] text) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length; i++)
			if ((text[i] >= '0' && text[i] <= '9') || text[i] == '+')
				sb.append(text[i]);
		return sb.toString().toLowerCase();
	}

	public static boolean isWebized(String text) {
		if (text == null)
			return false;
		return text.toLowerCase().equals(webize(text));
	}
}