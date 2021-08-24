package com.clickntap.tool.mail;

import java.util.HashMap;
import java.util.Map;

import com.clickntap.smart.SmartContext;
import com.clickntap.tool.script.ScriptEngine;
import com.clickntap.utils.ConstUtils;

public class Mailer {

  private String from;
  private String host;
  private String port;
  private String username;
  private String password;
  private Boolean startTtl;
  private ScriptEngine scriptEngine;
  private String prefix;

  public String getPrefix() {
    if (prefix == null) {
      prefix = "mail.";
    }
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public Boolean getStartTtl() {
    return startTtl;
  }

  public void setStartTtl(Boolean startTtl) {
    this.startTtl = startTtl;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setScriptEngine(ScriptEngine scriptEngine) {
    this.scriptEngine = scriptEngine;
  }

  public Mail newMail(String key, boolean starttl) {
    Mail mail = new Mail(starttl);
    mail.setKey(key);
    mail.setFrom(from);
    mail.setHost(host);
    mail.setPort(port);
    mail.setUsername(username);
    mail.setPassword(password);
    return mail;
  }

  public Mail newMail(String key, SmartContext ctx) throws Exception {
    Mail mail = newMail(key);
    Map<String, Object> mailCtx = new HashMap<String, Object>();
    mailCtx.put(ConstUtils.THIS, ctx);
    setup(mail, mailCtx);
    return mail;
  }

  public Mail newMail(String key) {
    return newMail(key, getStartTtl());
  }

  public void setup(Mail mail, Map<String, Object> ctx) throws Exception {
    setSubject(mail, ctx);
    setPlainBody(mail, ctx);
    setHtmlBody(mail, ctx);
  }

  public void setSubject(Mail mail, Map<String, Object> ctx) throws Exception {
    mail.setSubject(scriptEngine.eval(ctx, getPrefix() + mail.getKey() + ".subject.txt"));
  }

  public void setHtmlBody(Mail mail, Map<String, Object> ctx) throws Exception {
    mail.addBody(scriptEngine.eval(ctx, getPrefix() + mail.getKey() + ".htm"), ConstUtils.TEXT_HTML_CONTENT_TYPE);
  }

  public void setPlainBody(Mail mail, Map<String, Object> ctx) throws Exception {
    mail.addBody(scriptEngine.eval(ctx, getPrefix() + mail.getKey() + ".txt"), ConstUtils.TEXT_PLAIN_CONTENT_TYPE);
  }

  public void sendmail(Mail mail) throws Exception {
    mail.sendAsynchronous();
  }
}
