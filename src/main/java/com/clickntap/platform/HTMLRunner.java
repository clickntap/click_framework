package com.clickntap.platform;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.api.HttpUtils;
import com.clickntap.api.SecureApiController;
import com.clickntap.hub.App;
import com.clickntap.tool.f.FContext;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;

public class HTMLRunner {
  private FreemarkerScriptEngine engine;

  public HTMLRunner(App app, SecureApiController api, FreemarkerScriptEngine engine, FExecutor f) {
    this.engine = engine;
  }

  public boolean handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (request.getRequestURI().contains("qrcode")) {
      FContext ctx = new FContext(request, response);
      try {
        engine.eval(ctx, "website/webapp.html", response.getOutputStream());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    }
    if (request.getRequestURI().equals("/privacy/")) {
      String html = HttpUtils.get("https://www.iubenda.com/privacy-policy/21327669/legal");
      fixIubenda(html, request, response);
      return true;
    }
    if (request.getRequestURI().equals("/terms/")) {
      String html = HttpUtils.get("https://www.iubenda.com/termini-e-condizioni/21327669");
      fixIubenda(html, request, response);
      return true;
    }
    if (request.getRequestURI().equals("/cookie/")) {
      String html = HttpUtils.get("https://www.iubenda.com/privacy-policy/21327669/cookie-policy");
      fixIubenda(html, request, response);
      return true;
    }
    FContext ctx = new FContext(request, response);
    try {
      engine.eval(ctx, "website/page.html", response.getOutputStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  private void fixIubenda(String html, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.getOutputStream().write(html.getBytes(ConstUtils.UTF_8));
  }

}
