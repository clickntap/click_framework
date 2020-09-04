package com.clickntap.utils;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.lesscss.LessCompiler;

public class LessUtils {

	public static LessCompiler compiler = null;
	public static LessCompiler compressingCompiler = null;

	public static synchronized String eval(String code) throws Exception {
		return compressingCompiler().compile(code);
	}

	private static LessCompiler compressingCompiler() {
		if (compressingCompiler == null) {
			compressingCompiler = new LessCompiler();
			compressingCompiler.setCompress(false);
		}
		return compressingCompiler;
	}

	private static LessCompiler compiler() {
		if (compiler == null) {
			compiler = new LessCompiler();
			compiler.setCompress(false);
		}
		return compiler;
	}

	public static synchronized void compile(String file) throws Exception {
		LessUtils.compile(file, true);
	}

	public static synchronized void compile(File file) throws Exception {
		LessUtils.compile(file, true);
	}

	public static synchronized void compile(String file, boolean compress) throws Exception {
		LessUtils.compile(new File(file), compress);
	}

	public static synchronized void compile(File file, boolean compress) throws Exception {
		String css = (compress ? compressingCompiler() : compiler()).compile(file);
		FileUtils.writeByteArrayToFile(new File(file.getParent() + "/" + FilenameUtils.getBaseName(file.getName()) + ".css"), css.getBytes(ConstUtils.UTF_8));
	}
}
