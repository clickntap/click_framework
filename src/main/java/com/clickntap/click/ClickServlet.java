package com.clickntap.click;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClickServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
  }

  protected long getLastModified(HttpServletRequest req) {
    return super.getLastModified(req);
  }

  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doHead(req, resp);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }

  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPut(req, resp);
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
  }

  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doOptions(req, resp);
  }

  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doTrace(req, resp);
  }

  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.service(req, resp);
  }

  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    super.service(req, res);
  }

}
