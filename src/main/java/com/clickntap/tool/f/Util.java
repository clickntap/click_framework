package com.clickntap.tool.f;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;

import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.LessUtils;
import com.clickntap.utils.XMLUtils;

import freemarker.template.utility.StringUtil;

public class Util {

	private Resource file;
	private Map<String, Element> faFiles;

	public Util(Resource file) {
		this.file = file;
		faFiles = new HashMap<String, Element>();
	}

	public String formatDate(String d, String format, String language) {
		try {
			return formatDate(new Datetime(d), format, language);
		} catch (Exception e) {
			return ConstUtils.EMPTY;
		}
	}

	public String formatDate(Datetime d, String format, String language) {
		try {
			return d.format(format, language);
		} catch (Exception e) {
			return ConstUtils.EMPTY;
		}
	}

	public String less(String css) {
		try {
			return LessUtils.eval(css);
		} catch (Exception e) {
			return ConstUtils.EMPTY;
		}
	}

	public String fa(String group, String icon, Number width, String colorPrimary, String colorSecondary) throws Exception {
		String value = "";
		Element root = null;
		if (faFiles.containsKey(group)) {
			root = faFiles.get(group);
		} else {
			Document doc = XMLUtils.copyFrom(file.getFile().getParentFile().getParentFile().getAbsolutePath() + "/svg/" + group + ".svg");
			faFiles.put(group, root = doc.getRootElement());
		}
		for (Element element : root.elements()) {
			if (icon == null) {
				String id = element.attributeValue("id");
				if (id != null) {
					value += "," + id;
				}
			} else {
				if (icon.equalsIgnoreCase(element.attributeValue("id"))) {
					value = element.asXML().replace("symbol", "svg");
					if (value.contains("fa-primary")) {
						value = value.replace("class=\"fa-primary\"", "fill=\"" + colorPrimary + "\"");
						if (colorSecondary != null) {
							value = value.replace("class=\"fa-secondary\"", "fill=\"" + colorSecondary + "\"");
						}
					} else {
						value = value.replace("<path ", "<path fill=\"" + colorPrimary + "\" ");
					}
					int x1 = value.indexOf("viewBox");
					int x2 = value.indexOf("\"", x1 + 9);
					String viewBox = value.substring(x1 + 9, x2);
					int w = Integer.parseInt(StringUtil.split(viewBox, ' ')[2]);
					int h = Integer.parseInt(StringUtil.split(viewBox, ' ')[3]);
					if (w > h) {
						value = value.substring(0, 5) + " width=\"" + width + "\" height=\"" + (width.floatValue() * h / w) + "\" " + value.substring(x1);
					} else {
						value = value.substring(0, 5) + " width=\"" + (width.floatValue() * w / h) + "\" height=\"" + width + "\" " + value.substring(x1);
					}
				}
			}
		}
		if (icon == null) {
			value = value.substring(1);
		}
		return value;
	}

	public String fa(String group, String icon, Number width, String colorPrimary) throws Exception {
		return fa(group, icon, width, colorPrimary, null);
	}

	public String fa(String group) throws Exception {
		return fa(group, null, 0, null, null);
	}

}
