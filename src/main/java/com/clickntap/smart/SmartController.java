package com.clickntap.smart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;
import com.clickntap.utils.XMLUtils;

public class SmartController {

	private Boolean authenticated;
	private String loginRef;
	private String errorRef;
	private SmartController parent;
	private List<SmartController> children;
	private String demo;
	private String name;
	private String viewName;
	private SmartAction cacheAction;
	private List<SmartAction> actions;
	private List<SmartMethod> methods;
	private boolean ajax;

	public SmartController(Resource controllerDir, Element element, SmartController parent) throws Exception {
		this.parent = parent;
		this.authenticated = false;
		this.loginRef = ConstUtils.EMPTY;
		this.errorRef = ConstUtils.EMPTY;
		if (element.attributeValue("viewName") != null)
			viewName = element.attributeValue("viewName");
		else if (parent != null)
			viewName = parent.getViewName();
		if (element.attributeValue("demo") != null)
			demo = element.attributeValue("demo");
		else if (parent != null)
			demo = parent.getDemo();
		if (element.attributeValue("authenticated") != null)
			authenticated = element.attributeValue("authenticated").equals("true");
		else if (parent != null)
			authenticated = parent.isAuthenticated();
		if (element.attributeValue("loginRef") != null)
			loginRef = element.attributeValue("loginRef");
		else if (parent != null)
			loginRef = parent.getLoginRef();
		if (element.attributeValue("errorRef") != null)
			errorRef = element.attributeValue("errorRef");
		else if (parent != null)
			errorRef = parent.getErrorRef();
		name = element.attributeValue("name");
		ajax = Boolean.valueOf(element.attributeValue("ajax"));
		children = new ArrayList<SmartController>();
		for (Element child : (List<Element>) element.elements("app"))
			children.add(new SmartController(controllerDir, child, this));
		String ref = getRef();
		if (!ConstUtils.EMPTY.equals(ref)) {
			try {
				Element root = null;
				File file = getFile(controllerDir, ref);
				if (file.exists())
					root = XMLUtils.copyFrom(file).getRootElement();
				else
					root = DocumentHelper.createDocument().addElement("smart-controller");
				this.actions = new ArrayList<SmartAction>();
				this.methods = new ArrayList<SmartMethod>();
				initController(controllerDir, root);
			} catch (Exception e) {
				throw new SmartControllerDescriptorException(e);
			}
		}
	}

	public String getErrorRef() {
		return errorRef;
	}

	public List<SmartAction> getActions() {
		return actions;
	}

	public List<SmartMethod> getMethods() {
		return methods;
	}

	public boolean isAjax() {
		return ajax;
	}

	private void initController(Resource controllerDir, Element root) throws Exception {
		for (Element include : (List<Element>) root.elements("include"))
			initController(controllerDir, XMLUtils.copyFrom(getFile(controllerDir, include.attributeValue("ref"))).getRootElement());
		Element cacheActionElement = root.element("cache-action");
		if (cacheActionElement != null)
			cacheAction = new SmartAction(cacheActionElement);
		List<Element> actionElements = root.elements("action");
		if (actionElements != null)
			for (Element actionElement : actionElements)
				this.actions.add(new SmartAction(actionElement));
		List<Element> methodElements = root.elements("method");
		if (methodElements != null)
			for (Element methodElement : methodElements) {
				String[] names = StringUtils.commaDelimitedListToStringArray(methodElement.attributeValue("name"));
				for (String name : names)
					this.methods.add(new SmartMethod(name, methodElement));
			}
	}

	public SmartAction getCacheAction() {
		return cacheAction;
	}

	private File getFile(Resource controllerDir, String ref) throws IOException {
		File file = new File(IOUtils.toString(controllerDir.getFile()) + ConstUtils.SLASH + ref + ConstUtils.EXTENSION_DOTXML);
		return file;
	}

	public String getRef() {
		if (getParent() != null) {
			String ref = getParent().getRef();
			if (ConstUtils.EMPTY.equals(ref))
				return name;
			else
				return getParent().getRef() + ConstUtils.SLASH + name;
		}
		return ConstUtils.EMPTY;
	}

	public Boolean isAuthenticated() {
		return authenticated;
	}

	public String getLoginRef() {
		return loginRef;
	}

	public SmartController getParent() {
		return parent;
	}

	public List<SmartController> getChildren() {
		return children;
	}

	public List<SmartController> getPath() {
		List<SmartController> path = null;
		if (parent == null)
			path = new ArrayList<SmartController>();
		else
			path = parent.getPath();
		path.add(this);
		return path;
	}

	public SmartController getController(String ref) {
		int x = ref.indexOf(ConstUtils.SLASH);
		return x > 0 ? getChildByName(ref.substring(0, x)).getController(ref.substring(x + 1)) : getChildByName(ref);
	}

	private SmartController getChildByName(String ref) {
		if (ref.equals(ConstUtils.DOTDOT))
			return getParent();
		for (SmartController controller : getChildren())
			if (ref.equals(controller.getName()))
				return controller;
		return null;
	}

	public boolean isDemoLocked(SmartContext ctx) {
		String sessionDemo;
		try {
			sessionDemo = ctx.getSession().get("demo").toString();
		} catch (Exception e) {
			sessionDemo = ConstUtils.EMPTY;
		}
		return !getRef().equals("demo") && getDemo() != null && !getDemo().equals(sessionDemo);
	}

	public String getDemo() {
		return demo;
	}

	public String getName() {
		return name;
	}

	public String getViewName() {
		return viewName;
	}
}
