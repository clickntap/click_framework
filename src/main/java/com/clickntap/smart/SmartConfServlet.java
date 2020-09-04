package com.clickntap.smart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.io.FileUtils;
//import org.apache.log4j.PropertyConfigurator;

import com.clickntap.utils.ConstUtils;

public class SmartConfServlet implements Servlet {

  public static boolean checkEnvPath(String path, String env) {
    try {
      int x = path.indexOf(env);
      if (!Character.isLetterOrDigit(path.charAt(x - 1)) && !Character.isLetterOrDigit(path.charAt(x + env.length()))) {
        return true;
      }
    } catch (Exception e) {
    }
    return false;
  }

  public void init(ServletConfig conf) throws ServletException {
    try {
      File root = new File(conf.getServletContext().getRealPath(""));
      File webInf = new File(conf.getServletContext().getRealPath("WEB-INF"));
      File dir = new File(webInf.getCanonicalPath() + getPathSeparator() + "conf");
      File out = new File(dir.getCanonicalPath() + getPathSeparator() + "auto");
      out.mkdirs();
      Properties envProperties = new Properties();
      envProperties.putAll(System.getProperties());
      String envFile = conf.getInitParameter("smartEnv");
      if (envFile != null) {
        int x = envFile.lastIndexOf('/');
        if (x >= 0) {
          envFile = envFile.substring(x + 1);
        }
        File etcParentDir = root;
        while (etcParentDir != null) {
          File file = new File(etcParentDir.getCanonicalPath() + "/etc/" + envFile);
          if (file.exists()) {
            envFile = file.getCanonicalPath();
            break;
          }
          etcParentDir = etcParentDir.getParentFile();
        }
        File file = new File(envFile);
        if (file.exists()) {
          FileInputStream in = new FileInputStream(file);
          envProperties.load(in);
          in.close();
          //          file = new File(file.getCanonicalPath().replace("-env", "-log4j"));
          //          if (file.exists()) {
          //            //PropertyConfigurator.configure(file.getCanonicalPath().replace("-env", "-log4j"));
          //          } else {
          //            System.out.println("LOG4J NOT FOUND: " + file.getCanonicalPath().replace("-env", "-log4j"));
          //          }
        } else {
          System.out.println("ENV NOT FOUND: " + file.getCanonicalPath());
        }
      }
      FileUtils.cleanDirectory(out);
      if (dir.exists() && dir.isDirectory()) {
        for (File file : dir.listFiles()) {
          if (file.isFile()) {
            String data = FileUtils.readFileToString(file, ConstUtils.UTF_8);
            Enumeration enu = envProperties.keys();
            while (enu.hasMoreElements()) {
              String key = enu.nextElement().toString();
              data = data.replaceAll("\\[" + key + "\\]", envProperties.getProperty(key));
            }
            data = data.replaceAll("\\[WEB-INF\\]", webInf.getCanonicalPath().replace("\\", "/"));
            data = data.replaceAll("\\[ROOT\\]", root.getCanonicalPath().replace("\\", "/"));
            out.mkdirs();
            FileUtils.writeStringToFile(new File(out.getAbsolutePath() + getPathSeparator() + file.getName()), data, ConstUtils.UTF_8);
          }
        }
      }
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private String getPathSeparator() {
    return System.getProperty("file.separator");
  }

  public void destroy() {
  }

  public ServletConfig getServletConfig() {
    return null;
  }

  public String getServletInfo() {
    return null;
  }

  public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {

  }

}
