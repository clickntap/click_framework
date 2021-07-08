package com.clickntap.platform;

import com.clickntap.tool.f.F;

public class FThread extends Thread {

  private F f;

  public FThread(Runnable r) {
    super(r);
  }

  public void init(F f) throws Exception {
    this.f = new F();
    this.f.setCrypto(f.getCrypto());
    this.f.setExtension(f.getExtension());
    this.f.setFile(f.getFile());
    this.f.setServerURL(f.getServerURL());
    this.f.init();
  }

  public F getF() {
    return f;
  }

}
