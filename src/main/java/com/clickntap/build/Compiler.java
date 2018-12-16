package com.clickntap.build;

import java.io.File;

public interface Compiler {

  public void precompile(File file) throws Exception;

  public void compile(File file) throws Exception;

  public boolean compilable(File file);
}
