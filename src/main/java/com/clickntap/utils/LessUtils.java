package com.clickntap.utils;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.asual.lesscss.LessEngine;

public class LessUtils {

  public static LessEngine compiler = new LessEngine();

  public static synchronized String eval(String code) throws Exception {
    return compiler.compile(code);
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
    String css = compiler.compile(file, compress);
    FileUtils.writeByteArrayToFile(new File(file.getParent() + "/" + FilenameUtils.getBaseName(file.getName()) + ".css"), css.getBytes(ConstUtils.UTF_8));
  }
}
