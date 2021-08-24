package com.clickntap.smart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.clickntap.tool.script.ScriptEngine;

public class SmartMessageSource implements MessageSource {

  protected ScriptEngine scriptEngine;

  protected Resource messageResource;

  private Map<String, Properties> messagesMap;

  public void start() throws Exception {
    messagesMap = new HashMap<String, Properties>();
  }

  public void setMessageResource(Resource messageResource) throws IOException {
    this.messageResource = messageResource;
  }

  public void setScriptEngine(ScriptEngine scriptEngine) {
    this.scriptEngine = scriptEngine;
  }

  public String getMessage(String code, Object[] args, String ignored, Locale locale) {
    return getMessage(code, args, locale);
  }

  public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
    locale = new Locale("en");
    if (code.contains("[")) {
      int x1 = 0;
      int x2 = 0;
      while ((x1 = code.indexOf("[")) > 0) {
        x2 = x1;
        while (code.charAt(x2) != ']' && x2 < code.length())
          x2++;
        code = code.substring(0, x1) + code.substring(x2 + 1);
      }
    }
    Properties messages = messagesMap.get(locale.getLanguage());
    if (messages == null) {
      messages = new Properties();
      try {
        InputStream in = new FileInputStream(new File(messageResource.getFile().getAbsolutePath().replace("/message.", "/message_" + locale.getLanguage() + ".")));
        messages.load(in);
        in.close();
      } catch (Exception e) {
        try {
          messages.load(messageResource.getInputStream());
        } catch (Exception e1) {
        }
      }
      messagesMap.put(locale.getLanguage(), messages);
    }
    String script = null;
    try {
      script = (String) messages.getProperty(code);
    } catch (Throwable e) {
    }
    if (script == null) {
      script = code;
    }
    try {
      if (args != null && args.length > 0) {
        if (args[0] instanceof SmartContext)
          script = scriptEngine.evalScript((SmartContext) args[0], script);
        else {
          HashMap ctx = new HashMap();
          ctx.put("args", Arrays.asList(args));
          script = scriptEngine.evalScript(ctx, script);
        }
      }
      return script;
    } catch (Exception e) {
      return e.getMessage();
    }

  }

  public String getMessage(MessageSourceResolvable messageSource, Locale locale) throws NoSuchMessageException {
    String message = null;
    for (int i = 0; i < messageSource.getCodes().length && message == null; i++)
      message = getMessage(messageSource.getCodes()[i], messageSource.getArguments(), locale);
    if (message == null)
      return StringUtils.arrayToCommaDelimitedString(messageSource.getCodes());
    return message;
  }
}
