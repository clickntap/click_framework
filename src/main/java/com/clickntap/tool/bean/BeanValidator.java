package com.clickntap.tool.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.clickntap.tool.script.ScriptEngine;

public class BeanValidator implements Validator {
  private BeanInfo beanInfo;

  private ScriptEngine viewEngine;

  private String validationGroup;

  private BeanManager beanManager;

  public BeanValidator(BeanInfo beanInfo, ScriptEngine viewEngine, String validationGroup, BeanManager beanManager) {
    this.beanInfo = beanInfo;
    this.viewEngine = viewEngine;
    this.validationGroup = validationGroup;
    this.beanManager = beanManager;
  }

  public boolean supports(Class clazz) {
    return true;
  }

  public void validate(Object target, Errors errors) {
    Map<String, Object> context = new HashMap<String, Object>();
    try {
      context.put("target", target);
      context.put("this", new BeanErrors(target, errors, beanManager));

      List<String> scriptList = beanInfo.getValidationInfo().getValidationScriptList(validationGroup);
      for (String script : scriptList)
        viewEngine.evalScript(context, script);

    } catch (Exception e) {
      errors.reject("validation", e.getMessage());
    }
  }

}
