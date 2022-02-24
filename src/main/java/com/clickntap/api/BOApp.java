package com.clickntap.api;

import java.util.List;

import com.clickntap.tool.script.ScriptEngine;

public class BOApp extends com.clickntap.hub.App {
  private BOSorter sorter;
  private ScriptEngine boEngine;
  private Mailer mailer;

  public ScriptEngine getBoEngine() {
    return boEngine;
  }

  public void setBoEngine(ScriptEngine boEngine) {
    this.boEngine = boEngine;
  }

  public Mailer getMailer() {
    return mailer;
  }

  public void setMailer(Mailer mailer) {
    this.mailer = mailer;
  }

  public void init() throws Exception {
    super.init();
    sorter = new BOSorter();
  }

  public void sort(List<? extends BO> items, String propertyName, boolean ascending) {
    sorter.sort(items, propertyName, ascending);
  }

  public void sort(List<? extends BO> items, String propertyName) {
    sorter.sort(items, propertyName, true);
  }

  public static String createUserCode() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 3; i++) {
      char d;
      do {
        d = (char) ('A' + (Math.random() * ((int) 'Z' - (int) 'A')));
      } while (d == 'O' || d == 'I');
      int n = (int) (Math.random() * (8)) + 2;
      sb.append(d).append(n);
    }
    return sb.toString().toLowerCase();
  }

}
