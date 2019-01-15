[#macro javaClass type][#assign class]
[#if type?contains('int') || type?contains('double')]Number[/#if]
[#if type?contains('date')]Datetime[/#if]
[#if type?contains('varchar')]String[/#if]
[#if type?contains('text')]String[/#if]
[/#assign]${class?replace('\n','')}[/#macro]

[#assign entities = this.projectElement.element("entities")]
[#if entities??]

[#assign prefix = entities.attributeValue("prefix")]
[#list entities.elements("g") as g]
[#assign package = g.attributeValue("name")]

[#assign java]
package ${this.projectPackage}.bo.${package};

import java.util.List;
import com.clickntap.api.BOFilter;
import ${this.projectPackage}.bo.BOApp;
import com.clickntap.tool.types.Datetime;

public class ${package?capitalize}Selector extends BOFilter {

  public ${package?capitalize}Selector() throws Exception {
    super();
  }

  public ${package?capitalize}Selector(BOApp app) throws Exception {
    this();
    setApp(app);
  }

  [#list g.elements("entity") as entity]

  public ${entity.attributeValue("name")} get${entity.attributeValue("name")}(Number id) throws Exception {
    return getApp().getBO(${entity.attributeValue("name")}.class, id);
  }

  public List<${entity.attributeValue("name")}> search${entity.attributeValue("name")}s(String query) throws Exception {
    ${package?capitalize}Selector filter = new ${package?capitalize}Selector();
    filter.setQuery(query);
    return getApp().getBOListByFilter(${entity.attributeValue("name")}.class, filter, "${entity.attributeValue("name")?lower_case}s");
  }
  [/#list]

  [#assign auth]
  [#list g.elements("entity") as entity]
  [#assign n = 0]
  [#list entity.elements("field") as field]
  [#if field.attributeValue("name") == "username"] [#assign n = n + 1] [/#if]
  [#if field.attributeValue("name") == "password"] [#assign n = n + 1] [/#if]
  [/#list]
  [#if n == 2]
  public ${entity.attributeValue("name")} auth${entity.attributeValue("name")}(String usernameOrEmail, String password) throws Exception {
    ${entity.attributeValue("name")} filter = new ${entity.attributeValue("name")}();
    filter.setUsername(usernameOrEmail);
    filter.setPassword(password);
    return getApp().getBO(filter, "auth");
  }
  [/#if]
  [/#list]
  [/#assign]

  ${auth?trim}

  [#assign range]
  [#list g.elements("entity") as entity]
  [#assign n = 0]
  [#list entity.elements("field") as field]
  [#if field.attributeValue("name") == "start_date"] [#assign n = n + 1] [/#if]
  [#if field.attributeValue("name") == "end_date"] [#assign n = n + 1] [/#if]
  [/#list]
  [#if n == 2]
  public List<${entity.attributeValue("name")}> get${entity.attributeValue("name")}s(Datetime startDate, Datetime endDate) throws Exception {
    ${package?capitalize}Selector filter = new ${package?capitalize}Selector();
    filter.setStartDate(startDate);
    filter.setEndDate(endDate);
    return getApp().getBOListByFilter(${entity.attributeValue("name")}.class, filter, "range-${entity.attributeValue("name")?lower_case}s");
  }
  [/#if]
  [/#list]
  [/#assign]

  ${range?trim}
}
[/#assign]
${this.save(java,"src/main/java/"+this.projectPackage.replace(".","/")+"/bo/"+package+"/"+package?capitalize+"Selector.java")!}

[#assign java]
package ${this.projectPackage}.bo;
import com.clickntap.tool.script.ScriptEngine;
import java.util.List;

[#list entities.elements("g") as og]
[#assign opackage = og.attributeValue("name")]
import ${this.projectPackage}.bo.${opackage}.${opackage?capitalize}Selector;
[/#list]
import ${this.projectPackage}.bo.BOSorter;

public class BOApp extends com.clickntap.api.BOApp {
  [#list entities.elements("g") as og]
  [#assign opackage = og.attributeValue("name")]
  private ${opackage?capitalize}Selector ${opackage};
  [/#list]
  public void init() throws Exception {
    super.init();
    [#list entities.elements("g") as og]
    [#assign opackage = og.attributeValue("name")]
    ${opackage} = new ${opackage?capitalize}Selector(this);
    [/#list]
  }
  [#list entities.elements("g") as og]
  [#assign opackage = og.attributeValue("name")]
  public ${opackage?capitalize}Selector get${opackage?capitalize}() {
    return ${opackage};
  }
  [/#list]
}

[/#assign]
${this.save(java,"src/main/java/"+this.projectPackage.replace(".","/")+"/bo/BOApp.java")!}

[#assign java]
package ${this.projectPackage}.bo;
[#list entities.elements("g") as og]
[#assign opackage = og.attributeValue("name")]
[#list og.elements("entity") as oentity]
import ${this.projectPackage}.bo.${opackage}.${oentity.attributeValue("name")};
[/#list]
[/#list]
import com.clickntap.api.BO;
import java.util.List;

public class BOSession extends com.clickntap.hub.AppSession {
  [#list entities.elements("g") as og]
  [#assign opackage = og.attributeValue("name")]
  [#list og.elements("entity") as oentity]
  private Number selected${oentity.attributeValue("name")}Id;
  private String query${oentity.attributeValue("name")};
  [/#list]
  [/#list]
  [#list entities.elements("g") as og]
  [#assign opackage = og.attributeValue("name")]
  [#list og.elements("entity") as oentity]
  public void ${this.setter("selected")}${oentity.attributeValue("name")}Id(Number selected${oentity.attributeValue("name")}Id) {
    this.selected${oentity.attributeValue("name")}Id = selected${oentity.attributeValue("name")}Id;
  };
  public Number ${this.getter("selected")}${oentity.attributeValue("name")}Id() {
    return selected${oentity.attributeValue("name")}Id;
  };
  public void ${this.setter("query")}${oentity.attributeValue("name")}(String query${oentity.attributeValue("name")}) {
    this.query${oentity.attributeValue("name")} = query${oentity.attributeValue("name")};
  };
  public String ${this.getter("query")}${oentity.attributeValue("name")}() {
    return query${oentity.attributeValue("name")};
  };
  public ${oentity.attributeValue("name")} getSelected${oentity.attributeValue("name")}() throws Exception {
    return getApp().getBO(${oentity.attributeValue("name")}.class, getSelected${oentity.attributeValue("name")}Id());
  };
  [/#list]
  [/#list]

  public void sort(List<BO> items, String propertyName, boolean ascending) throws Exception {
    ((BOApp) getApp()).sort(items, propertyName, ascending);
  }

  public void sort(List<BO> items, String propertyName) throws Exception {
    ((BOApp) getApp()).sort(items, propertyName, true);
  }
}

[/#assign]
${this.save(java,"src/main/java/"+this.projectPackage.replace(".","/")+"/bo/BOSession.java")!}


[#list g.elements("entity") as entity]
[#assign java]
package ${this.projectPackage}.bo.${package};

import com.clickntap.api.BO;
import com.clickntap.tool.types.Datetime;
import java.util.HashMap;
import com.clickntap.utils.ConstUtils;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.clickntap.utils.SecurityUtils;
import org.json.JSONObject;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.tool.mail.Mail;
import javax.servlet.http.HttpServletRequest;
[#list entity.elements("field") as field]
[#if (field.attributeValue("references")!"") != ""]
import ${this.projectPackage}.bo.${field.attributeValue("references")};
[/#if]
[/#list]
[#list entities.elements("g") as og]
[#assign opackage = og.attributeValue("name")]
[#list og.elements("entity") as oentity]
[#list oentity.elements("field") as ofield]
[#if ofield.attributeValue("references")??]
[#if ofield.attributeValue("references") == package+"."+entity.attributeValue("name")]
import ${this.projectPackage}.bo.${opackage}.${oentity.attributeValue("name")};
[/#if]
[/#if]
[/#list]
[/#list]
[/#list]


public class ${entity.attributeValue("name")} extends BO {

  [#list entity.elements("field") as field][#if field.attributeValue("name") != "id" && field.attributeValue("name") != "last_modified" && field.attributeValue("name") != "creation_date"]
  [#if field.attributeValue("type")?contains("date") && field.attributeValue("name")?ends_with("date")]
  private String ${this.name(field.attributeValue("name")+"_only_time")};
  private String ${this.name(field.attributeValue("name")+"_only_date")};
  [#else]
  private [@javaClass field.attributeValue("type")/] ${this.name(field.attributeValue("name"))};
  [#if field.attributeValue("name") == "password"]
  private String confirmPassword;
  private String oldPassword;
  private String newPassword;
  private String confirmNewPassword;
  [/#if]
  [/#if]
  [/#if][/#list]

  public ${entity.attributeValue("name")}() {
  	super();
  }

  public ${entity.attributeValue("name")}(HttpServletRequest request) throws Exception {
	  super(request);
  }

  public void setEdit${entity.attributeValue("name")}Id(Number id) {
    setId(id);
  }

  public Number getEdit${entity.attributeValue("name")}Id() {
    return getId();
  }

  [#list entities.elements("g") as og]
  [#assign opackage = og.attributeValue("name")]
  [#list og.elements("entity") as oentity]
  [#list oentity.elements("field") as ofield]
  [#if ofield.attributeValue("references")??]
  [#if ofield.attributeValue("references") == package+"."+entity.attributeValue("name")]
  [#if opackage+"."+oentity.attributeValue("name") == package+"."+entity.attributeValue("name")]

  public List<${oentity.attributeValue("name")}> getPath() throws Exception {
    List<${oentity.attributeValue("name")}> path = new ArrayList<${oentity.attributeValue("name")}>();
    if (!isRoot()) {
      path.addAll(getParent().getPath());
    }
    path.add(this);
    return path;
  }

  public boolean isRoot() throws Exception {
    return getParent() == null;
  }

  public List<${oentity.attributeValue("name")}> getChildren() throws Exception {
    return getApp().getBOListByFilter(${oentity.attributeValue("name")}.class, this, "children");
  }

  [#else]

  [#if ofield.attributeValue("method-name")??]
  public List<${oentity.attributeValue("name")}> get${ofield.attributeValue("method-name")}() throws Exception {
    return getApp().getBOListByFilter(${oentity.attributeValue("name")}.class, this, "${ofield.attributeValue("method-name")?lower_case}");
  }
  [#else]
  public List<${oentity.attributeValue("name")}> get${oentity.attributeValue("name")}s() throws Exception {
    return getApp().getBOListByFilter(${oentity.attributeValue("name")}.class, this, "${oentity.attributeValue("name")?lower_case}s");
  }
  [/#if]

  [#if ofield.attributeValue("limited")??]
  public List<${oentity.attributeValue("name")}> getLast${oentity.attributeValue("name")}s() throws Exception {
    return getApp().getBOListByFilter(${oentity.attributeValue("name")}.class, this, "${oentity.attributeValue("name")?lower_case}s-limited");
  }
  [/#if]


  [/#if]
  [/#if]
  [/#if]
  [/#list]
  [/#list]
  [/#list]

  public Number delete() throws Exception {
    [#list entities.elements("g") as og]
    [#assign opackage = og.attributeValue("name")]
    [#list og.elements("entity") as oentity]
    [#list oentity.elements("field") as ofield]
    [#if ofield.attributeValue("references")??]
    [#if ofield.attributeValue("references") == package+"."+entity.attributeValue("name")]
    [#if opackage+"."+oentity.attributeValue("name") == package+"."+entity.attributeValue("name")]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:getChildren()) {
      ${name?lower_case}.delete();
    }
    [#else]
    [#if ofield.attributeValue("method-name")??]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:get${ofield.attributeValue("method-name")}()) {
      ${name?lower_case}.delete();
    }
    [#else]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:get${name}s()) {
      ${name?lower_case}.delete();
    }
    [/#if]
    [/#if]
    [/#if]
    [/#if]
    [/#list]
    [/#list]
    [/#list]
    return super.delete();
  }
  public int deleteWeight() throws Exception {
    int weight = 0;
    [#list entities.elements("g") as og]
    [#assign opackage = og.attributeValue("name")]
    [#list og.elements("entity") as oentity]
    [#list oentity.elements("field") as ofield]
    [#if ofield.attributeValue("references")??]
    [#if ofield.attributeValue("references") == package+"."+entity.attributeValue("name")]
    [#if opackage+"."+oentity.attributeValue("name") == package+"."+entity.attributeValue("name")]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:getChildren()) {
      weight++;
      weight+=${name?lower_case}.deleteWeight();
    }
    [#else]
    [#if ofield.attributeValue("method-name")??]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:get${ofield.attributeValue("method-name")}()) {
      weight++;
      weight+=${name?lower_case}.deleteWeight();
    }
    [#else]
    [#assign name = oentity.attributeValue("name")]
    for(${name} ${name?lower_case}:get${name}s()) {
      weight++;
      weight+=${name?lower_case}.deleteWeight();
    }
    [/#if]
    [/#if]
    [/#if]
    [/#if]
    [/#list]
    [/#list]
    [/#list]
    return weight;
  }

  [#list entity.elements("field") as field][#if field.attributeValue("name") != "id" && field.attributeValue("name") != "last_modified" && field.attributeValue("name") != "creation_date"]
  [#if field.attributeValue("type")?contains("date") && field.attributeValue("name")?ends_with("date")]
  public String ${this.getter(field.attributeValue("name")+"_only_time")}() {
    return ${this.name(field.attributeValue("name")+"_only_time")};
  };
  public String ${this.getter(field.attributeValue("name")+"_only_date")}() {
    return ${this.name(field.attributeValue("name")+"_only_date")};
  };
  public [@javaClass field.attributeValue("type")/] ${this.getter(field.attributeValue("name"))}() {
    try {
      return new Datetime(${this.name(field.attributeValue("name")+"_only_date")}+" "+${this.name(field.attributeValue("name")+"_only_time")});
    } catch (Exception e) {
      return null;
    }
  };
  [#else]


  public [@javaClass field.attributeValue("type")/] ${this.getter(field.attributeValue("name"))}() {
    return ${this.name(field.attributeValue("name"))};
  };
  [#if field.attributeValue("name")?ends_with("json_as_string")]
  public JSONObject ${this.getter(field.attributeValue("name")?replace("json_as_string","json"))}() {
    return new JSONObject(${this.getter(field.attributeValue("name"))}());
  };
  [/#if]
  [#if field.attributeValue("name") == "password"]
  public String getConfirmPassword() {
    return confirmPassword;
  }
  public String getNewPassword() {
    return newPassword;
  }
  public String getNewPasswordMD5() throws Exception {
    return SecurityUtils.md5(getNewPassword());
  }
    public String getConfirmNewPassword() {
    return confirmNewPassword;
  }
  public String getOldPassword() {
    return oldPassword;
  }
  public String getOldPasswordMD5() throws Exception {
    return SecurityUtils.md5(getOldPassword());
  }
public String getPasswordMD5() throws Exception {
    return SecurityUtils.md5(getPassword());
  }
  [/#if]

  [/#if]
  [#if (field.attributeValue("references")!"") != ""]
  public ${field.attributeValue("references")?split('.')[1]} ${this.getter(field.attributeValue("name"))?replace("Id","")}() throws Exception {
    return getApp().getBO(${field.attributeValue("references")?split('.')[1]}.class,${this.getter(field.attributeValue("name"))}());
  };
  [/#if]
  [#if field.attributeValue("type")?contains("date") && field.attributeValue("name")?ends_with("date")]
  public void ${this.setter(field.attributeValue("name")+"_only_time")}(String ${this.name(field.attributeValue("name")+"_only_time")}) {
    this.${this.name(field.attributeValue("name")+"_only_time")} = ${this.name(field.attributeValue("name")+"_only_time")};
  };
  public void ${this.setter(field.attributeValue("name")+"_only_date")}(String ${this.name(field.attributeValue("name")+"_only_date")}) {
    this.${this.name(field.attributeValue("name")+"_only_date")} = ${this.name(field.attributeValue("name")+"_only_date")};
  };
  public void ${this.setter(field.attributeValue("name"))}([@javaClass field.attributeValue("type")/] ${this.name(field.attributeValue("name"))}) {
    if(${this.name(field.attributeValue("name"))} == null) {
      this.${this.name(field.attributeValue("name")+"_only_time")} = null;
      this.${this.name(field.attributeValue("name")+"_only_date")} = null;
    } else {
      this.${this.name(field.attributeValue("name")+"_only_time")} = ${this.name(field.attributeValue("name"))}.format("HH:mm");
      this.${this.name(field.attributeValue("name")+"_only_date")} = ${this.name(field.attributeValue("name"))}.format("yyyy-MM-dd");
    }
  };
  [#else]
  public void ${this.setter(field.attributeValue("name"))}([@javaClass field.attributeValue("type")/] ${this.name(field.attributeValue("name"))}) {
    this.${this.name(field.attributeValue("name"))} = ${this.name(field.attributeValue("name"))};
  };
  [#if field.attributeValue("name") == "password"]
   public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
  public void setConfirmNewPassword(String confirmNewPassword) {
    this.confirmNewPassword = confirmNewPassword;
  }
  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }
  [/#if]
  [#if field.attributeValue("name") == "email"]
  	public void forgotPassword(Map<String, Object> ctx) throws Exception {
		if (getEmail() != null && !"".equals(getEmail())) {
			Mail mail = getApp().getMailer().newMail("forgot-password");
			mail.addTo(getEmail());
			getApp().getMailer().setup(mail, ctx);
			mail.send();
		}
	}
  [/#if]

  [/#if]

  [/#if][/#list]

  public JSONObject json(boolean recursive) {
    JSONObject json = super.json(recursive);
    [#list entity.elements("json") as json]
    [#if !(json.attributeValue("recursive")??)]if(recursive) {[/#if]
    [#if json.attributeValue("type")?? && json.attributeValue("type") == "list"]
      try {
        List<JSONObject> items = new ArrayList<JSONObject>();
          for(${json.attributeValue("class")} item: ${json.attributeValue("method")}()) {
            [#if json.attributeValue("name") == "path"]
            if (item.getId().longValue() != this.getId().longValue()) {
              items.add(item.json(false));
            }
            [#else]
            items.add(item.json(false));
            [/#if]

          }
          json.put("${json.attributeValue("name")}", items);
       }catch(Exception e) {
      }
    [#else]
    try {
      [#if json.attributeValue("value")?contains(".ftl")]
      Map<String, Object> ctx = new HashMap<String, Object>();
			ctx.put(ConstUtils.THIS, this);
			String value = getApp().getBoEngine().eval(ctx, "${json.attributeValue("value")}");
      json.put("${json.attributeValue("name")}", value);
      [#else]
	      [#if json.attributeValue("name") == "t"]
	      Long t = (Long)BeanUtils.getValue(this, "${json.attributeValue("value")}");
	      json.put("${json.attributeValue("name")}", t);
	      [#else]
	      Object o = BeanUtils.getValue(this, "${json.attributeValue("value")}");
	      json.put("${json.attributeValue("name")}", (o instanceof BO)?((BO)o).json(false):o);
	      [/#if]
      [/#if]
    }catch(Exception e) {
    }
    [/#if]
    [#if !(json.attributeValue("recursive")??)]}[/#if]
    [/#list]
    return json;
  }

}
[/#assign]
${this.save(java,"src/main/java/"+this.projectPackage.replace(".","/")+"/bo/"+package+"/"+entity.attributeValue("name")+".java")!}

[/#list]
[/#list]


[/#if]
