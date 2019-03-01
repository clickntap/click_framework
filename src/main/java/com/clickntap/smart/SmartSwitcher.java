package com.clickntap.smart;

import java.beans.PropertyDescriptor;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.clickntap.smart.SmartAction.BindingElement;
import com.clickntap.tool.bean.Bean;
import com.clickntap.tool.bean.BeanManager;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.jdbc.JdbcManager;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.WebUtils;

public class SmartSwitcher implements Controller {
  private static final String CHANNEL_SCRIPT_KEY = "channelScript";
  private static final String FORM_SCRIPT_KEY = "formScript";
  private static final String ACTION_SCRIPT_KEY = "actionScript";
  private static final String RESET_SCRIPT_KEY = "resetScript";
  private static Log log = LogFactory.getLog(SmartSwitcher.class);
  private JdbcManager jdbcManager;

  private static boolean isReset(SmartContext context) {
    try {
      if (Integer.parseInt(context.eval(context.conf(RESET_SCRIPT_KEY))) == 1)
        return true;
    } catch (Exception e) {
    }
    return false;
  }

  public static boolean executeAction(SmartContext context, SmartAction action, boolean reset) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("[ action");
    }
    if (context.isBreak()) {
      if (log.isDebugEnabled()) {
        log.debug("break");
        log.debug("action ]");
      }
      return false;
    }
    boolean canContinue = true;
    executeLoads(context, action);
    if (canContinue)
      executeScripts(context, action.getInits());
    if (canContinue)
      canContinue = executeRules(context, action, canContinue);
    if (canContinue || (action.getElses().size() > 0))
      canContinue = executeBinds(context, action, canContinue, true, reset);
    if (canContinue)
      executeScripts(context, action.getExecs());
    else
      executeScripts(context, action.getElses());
    if (log.isDebugEnabled()) {
      log.debug("action ]");
    }
    return canContinue;
  }

  public static boolean executeRules(SmartContext context, SmartAction action, boolean canContinue) throws Exception {
    if (context.isBreak())
      return false;
    for (String rule : action.getRules()) {
      if (log.isDebugEnabled()) {
        log.debug("eval rule '" + rule + "'");
      }
      if (!context.evalRule(rule)) {
        if (log.isDebugEnabled()) {
          log.debug("break with rule '" + rule + "'");
        }
        canContinue = false;
        break;
      }
    }
    return canContinue;
  }

  public static void executeScripts(SmartContext context, List<String> scripts) throws Exception {
    if (context.isBreak())
      return;
    for (String script : scripts) {
      if (log.isDebugEnabled()) {
        log.debug("eval script '" + script + "'");
      }
      try {
        context.eval(script);
      } catch (Exception e) {
        throw e;
      }
    }
  }

  public static boolean executeBinds(SmartContext context, SmartAction action, boolean canContinue, boolean storable, boolean reset) throws Exception {
    if (context.isBreak())
      return false;
    for (BindingElement be : action.getBinds()) {
      SmartBindingResult bindingResult = context.bind(be.getObjectName(), be.getObjectClass(), be.getChannel(getSmartChannelContext(context)), be.getAllowedFields(), be.getDisallowedFields(), be.getScope());
      Object target = bindingResult.getBindingResult().getTarget();
      if (reset || bindingResult.isNew()) {
        context.eval(be.getScript());
        Object tempObject = Class.forName(be.getObjectClass()).newInstance();
        context.bind(tempObject);
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(tempObject.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
          try {
            Object value = BeanUtils.getValue(tempObject, descriptor.getName());
            if (value != null && !(value instanceof Bean)) {
              BeanUtils.setValue(target, descriptor.getName(), value);
            }
          } catch (Exception e) {
          }
        }
      }
      if (storable) {
        if (target instanceof Bean && be.getValidationGroup() != null)
          ValidationUtils.invokeValidator(((BeanManager) context.getBean(be.getChannel(getSmartChannelContext(context)))).getValidator((Bean) target, be.getValidationGroup()), target, bindingResult.getBindingResult());
      }
      if (bindingResult.getBindingResult().hasErrors()) {
        canContinue = false;
      }
    }
    return canContinue;
  }

  public static void executeLoads(SmartContext context, SmartAction action) throws Exception {
    if (context.isBreak())
      return;
    for (BindingElement be : action.getLoads()) {
      context.load(be.getObjectName(), be.getObjectClass(), be.getChannel(getSmartChannelContext(context)), be.getScope());
      context.eval(be.getScript());
    }
  }

  private static String getSmartChannelContext(SmartContext context) throws Exception {
    try {
      String channelContext = context.eval(context.conf(CHANNEL_SCRIPT_KEY));
      return channelContext;
    } catch (Throwable e) {
      return ConstUtils.EMPTY;
    }
  }

  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (request.getCharacterEncoding() == null)
      request.setCharacterEncoding(ConstUtils.UTF_8);
    SmartContext ctx = new SmartContext(request, response);
    return restHandleRequest(ctx);
  }

  public ModelAndView restHandleRequest(SmartContext ctx) throws Exception {
    try {
      if ("yes".equals(ctx.getRequest().getSession().getAttribute("smartLogout"))) {
        ctx.getRequest().getSession().removeAttribute("smartLogout");
        WebUtils.setClientData(ctx.getResponse(), SmartContext.SMART_USER_ID, null);
        ctx.tryLogout();
        ctx.redirect(ctx.getController().getLoginRef());
        return null;
      }
      if (handleRequest(ctx) && !ctx.isRedirected()) {
        return new ModelAndView(ctx.getController().getViewName(), ctx);
      } else
        return null;
    } catch (Exception e) {
      ctx.setException(e);
      try {
        return new ModelAndView(ctx.getController().getViewName(), ctx);
      } catch (SmartControllerNotFoundException notfound) {
        return null;
      }
    }
  }

  private boolean handleRequest(SmartContext ctx) throws Exception {
    try {
      if (jdbcManager == null)
        return execute(ctx);
      else {
        return (Boolean) jdbcManager.execute(new TransactionalService(this, ctx));
      }
    } catch (SmartControllerNotFoundException e) {
    } catch (SmartControllerAccessDeniedException e) {
      ctx.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    return false;
  }

  public boolean execute(SmartContext context) throws Exception {
    boolean executed = false;

    SmartController controller = context.getController();

    if (controller.isDemoLocked(context)) {
      if (controller.isAjax())
        throw new SmartControllerAccessDeniedException();
      context.redirect("demo");
      return false;
    }
    if (!context.canTryAutoLogin())
      context.tryAutoLogin();

    if (controller.isAjax() && controller.isAuthenticated() && !context.isAuthenticated())
      throw new SmartControllerAccessDeniedException();
    if (context.tryLogin(false) && context.isStoredRequest())
      context.redirect(context.getStoredRequest().getRef());
    else if (controller.isAuthenticated() && !context.isAuthenticated()) {
      if (!context.isLogoutRequest())
        context.storeRequest();
      context.redirect(controller.getLoginRef());
    } else {
      if (context.isAuthenticated() && context.isStoredRequest())
        context.loadRequest();
      executeController(context, controller);
      executed = true;
    }
    context.tryLogout();
    return executed && !context.isRedirected();
  }

  private void executeController(SmartContext context, SmartController controller) throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("[ service");
      }
      long lo = System.currentTimeMillis();
      SmartAction cacheAction = controller.getCacheAction();
      if (cacheAction != null) {
        executeAction(context, cacheAction, false);
        if (SmartCache.isCached(context))
          return;
      }
      for (SmartAction action : controller.getActions()) {
        executeAction(context, action, false);
      }
      for (SmartMethod method : controller.getMethods()) {
        if (log.isDebugEnabled()) {
          log.debug("[ method " + method.getName());
        }
        executeScripts(context, method.getInits());
        if (method.getName().equals(context.eval(context.conf(ACTION_SCRIPT_KEY)))) {
          executeAction(context, method, isReset(context));
        } else if (method.getName().equals(context.eval(context.conf(FORM_SCRIPT_KEY)))) {
          executeLoads(context, method);
          boolean canContinue = true;
          if (canContinue)
            canContinue = executeRules(context, method, canContinue);
          if (canContinue || (method.getElses().size() > 0))
            executeBinds(context, method, true, false, isReset(context));
        }
        if (log.isDebugEnabled()) {
          log.debug("method ]");
        }
      }
      lo = (System.currentTimeMillis() - lo);
      if (log.isDebugEnabled()) {
        log.debug("exec in " + lo + " millis");
        log.debug("service ]");
      }
    } catch (Exception e) {
      throw e;
    } finally {
    }
  }

  public void setJdbcManager(JdbcManager jdbcManager) {
    this.jdbcManager = jdbcManager;
  }

  public class TransactionalService implements TransactionCallback {

    private SmartSwitcher smartSwicher;
    private SmartContext smartContext;

    public TransactionalService(SmartSwitcher smartSwitcher, SmartContext smartContext) {
      this.smartSwicher = smartSwitcher;
      this.smartContext = smartContext;
    }

    public Object doInTransaction(TransactionStatus status) {
      try {
        return smartSwicher.execute(smartContext);
      } catch (SmartControllerAccessDeniedException e) {
        throw e;
      } catch (Exception e) {
        if (log.isErrorEnabled()) {
          log.error(e);
        }
        throw new RuntimeException(e);
      }
    }

  }
}
