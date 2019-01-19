package com.clickntap.build;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.cathive.sass.SassContext;
import com.cathive.sass.SassFileContext;
import com.cathive.sass.SassOptions;
import com.cathive.sass.SassOutputStyle;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;

public class CascadingStyleSheetsCompiler extends AbstractCompiler {

  public void compile(File file) throws Exception {
    File tmpFile = null;
    try {
      tmpFile = tmpFile(file);
      if (FilenameUtils.getExtension(file.getName()).equals("less")) {
        LessUtils.compile(tmpFile);
        File cssFile = cssFile(file);
        File minFile = new File(cssFile.getParentFile().getAbsolutePath() + "/css/" + file.getName().replace(".less", ".css"));
        StringBuffer sb = new StringBuffer();
        if (!file.getAbsolutePath().contains("/lib/")) {
          sb.append(libs("css", "css"));
          sb.append(libs("cnt", "css"));
          sb.append('\n');
        }
        sb.append(FileUtils.readFileToString(cssFile, ConstUtils.UTF_8));
        FileUtils.writeStringToFile(minFile, sb.toString(), ConstUtils.UTF_8);
        cssFile.delete();
      }
      if (FilenameUtils.getExtension(file.getName()).equals("sass")) {
        Path srcRoot = Paths.get(file.getParentFile().getCanonicalPath());
        SassContext ctx = SassFileContext.create(srcRoot.resolve(file.getName()));
        SassOptions options = ctx.getOptions();
        options.setOutputStyle(SassOutputStyle.COMPRESSED);
        File minFile = new File(file.getParentFile().getAbsolutePath() + "/" + file.getName().replace(".sass", ".css"));
        FileUtils.writeStringToFile(minFile, ctx.compile(), ConstUtils.UTF_8);
      }
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
    if (FilenameUtils.getExtension(file.getName()).equals("sass")) {
      return true;
    }
    return false;
  }

}
