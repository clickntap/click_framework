package com.clickntap.build;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.clickntap.utils.ConstUtils;

public class JavascriptCompiler extends AbstractCompiler {

	public void compile(File file) throws Exception {
		File tmpFile = tmpFile(file);
		StringBuffer sb = new StringBuffer();
		sb.append(libs("js", "js"));
		sb.append(libs("cnt", "js"));
		sb.append("cntSVG.add(").append(libs("svg", "json")).append(")\n");
		sb.append('\n');
		File minFile = new File(file.getParentFile().getAbsolutePath() + "/js/" + file.getName());
		sb.append(FileUtils.readFileToString(tmpFile, ConstUtils.UTF_8));
		FileUtils.writeStringToFile(minFile, sb.toString(), ConstUtils.UTF_8);
		tmpFile.delete();
	}

	public boolean compilable(File file) {
		if (FilenameUtils.getExtension(file.getName()).equals("js") && !templateName(file).contains("/") && !templateName(file).contains("_") && !templateName(file).contains("-")) {
			return true;
		}
		return false;
	}

}
