package com.clickntap.tool.f;

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

	public Util(Resource file) {
		this.file = file;
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
			if (css.indexOf('{') < 0) {
				css = "c{a:" + css + "}";
			}
			String value = LessUtils.eval(css);
			if (value.indexOf('{') < 0) {
				return value;
			} else {
				int x0 = value.indexOf("a:");
				int x1 = value.indexOf(";");
				return value.substring(x0 + 2, x1);
			}
		} catch (Exception e) {
			return css;
		}
	}

	public String fa(String group, String icon, Number width, String colorPrimary, String colorSecondary) throws Exception {
		String xml = "";
		Document doc = XMLUtils.copyFrom(file.getFile().getParentFile().getParentFile().getAbsolutePath() + "/svg/" + group + ".svg");
		for (Element element : doc.getRootElement().elements()) {
			if (icon.equalsIgnoreCase(element.attributeValue("id"))) {
				xml = element.asXML().replace("symbol", "svg");
				if (xml.contains("fa-primary")) {
					xml = xml.replace("class=\"fa-primary\"", "fill=\"" + colorPrimary + "\"");
					if (colorSecondary != null) {
						xml = xml.replace("class=\"fa-secondary\"", "fill=\"" + colorSecondary + "\"");
					}
				} else {
					xml = xml.replace("<path ", "<path fill=\"" + colorPrimary + "\" ");
				}
				int x1 = xml.indexOf("viewBox");
				int x2 = xml.indexOf("\"", x1 + 9);
				String viewBox = xml.substring(x1 + 9, x2);
				int w = Integer.parseInt(StringUtil.split(viewBox, ' ')[2]);
				int h = Integer.parseInt(StringUtil.split(viewBox, ' ')[3]);
				xml = xml.substring(0, 5) + " width=\"" + width + "\" height=\"" + (width.floatValue() * h / w) + "\" " + xml.substring(x1);
			}
		}
		System.out.println(xml);
		return xml;
	}

	public String fa(String group, String icon, Number width, String colorPrimary) throws Exception {
		return fa(group, icon, width, colorPrimary, null);
	}

}
