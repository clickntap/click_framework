package com.clickntap.build;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;

public class CascadingStyleSheetsCompiler extends AbstractCompiler {

  public void compile(File file) throws Exception {
    File tmpFile = null;
    try {
      tmpFile = tmpFile(file);
      LessUtils.compile(tmpFile);
      File cssFile = cssFile(file);
      File minFile = new File(cssFile.getParentFile().getAbsolutePath() + "/css/" + file.getName().replace(".less", ".css"));
      StringBuffer sb = new StringBuffer();
      sb.append(libs("css", "css"));
      sb.append(libs("cnt", "css"));
      sb.append('\n');
      sb.append(FileUtils.readFileToString(cssFile, ConstUtils.UTF_8));
      FileUtils.writeStringToFile(minFile, sb.toString(), ConstUtils.UTF_8);
      cssFile.delete();
    } finally {
      tmpFile.delete();
    }
  }

  protected File cssFile(File file) {
    return new File(file.getParentFile(), file.getName() + ".css");
  }

  public boolean compilable(File file) {
    if (FilenameUtils.getExtension(file.getName()).equals("less")) {
      return true;
    }
    return false;
  }

}
