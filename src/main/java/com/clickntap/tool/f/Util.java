package com.clickntap.tool.f;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;

import com.clickntap.api.CryptoUtils;
import com.clickntap.api.SecureUtils;
import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.IOUtils;
import com.clickntap.utils.LessUtils;
import com.clickntap.utils.XMLUtils;

import freemarker.template.utility.StringUtil;

public class Util {

	private Resource file;
	private Map<String, Object> resourceFiles;
	private CryptoUtils crypto;

	public Util(Resource file, CryptoUtils crypto) {
		this.file = file;
		this.crypto = crypto;
		resourceFiles = new HashMap<String, Object>();
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

	public String darken(String color, String percentage) {
		return lessValue("darken(" + color + ", " + percentage + ")");
	}

	public String lighten(String color, String percentage) {
		return lessValue("lighten(" + color + ", " + percentage + ")");
	}

	public String alpha(String color, String percentage) {
		return lessValue("alpha(" + color + ", " + percentage + ")");
	}

	public String lessValue(String css) {
		try {
			css = less("c{a:" + css + "}");
			int x0 = css.indexOf(":");
			int x1 = css.indexOf("}");
			return css.substring(x0 + 1, x1).trim();
		} catch (Exception e) {
			return css;
		}
	}

	public String less(String css) {
		try {
			return LessUtils.eval(css);
		} catch (Exception e) {
			return css;
		}
	}

	public String urlAsBase64(String url) throws Exception {
		InputStream in = new URL(url).openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		out.close();
		in.close();
		return SecureUtils.base64enc(out.toByteArray());
	}

	public String base64encode(String string) throws Exception {
		return SecureUtils.base64enc(string.getBytes(ConstUtils.UTF_8));
	}

	public String base64decode(String string) throws Exception {
		return new String(SecureUtils.base64dec(string), ConstUtils.UTF_8);
	}

	public String encrypt(String s) throws Exception {
		return crypto.encrypt(s);
	}

	public String decrypt(String s) throws Exception {
		return crypto.decrypt(s);
	}

	public String flag(String icon) throws Exception {
		String value = "";
		String flags = null;
		if (resourceFiles.containsKey("flags")) {
			flags = (String) resourceFiles.get("flags");
		} else {
			String resource = FileUtils.readFileToString(new File(file.getFile().getParentFile().getParentFile().getParentFile().getAbsolutePath() + "/css/flags.css"), ConstUtils.UTF_8);
			resourceFiles.put("flags", flags = resource);
		}
		int x0 = flags.indexOf("flag-" + icon + "{");

		int x1;
		x0 = flags.indexOf("base64,", x0) + 7;
		x1 = flags.indexOf(")}", x0);
		value = new String(SecureUtils.base64dec(flags.substring(x0, x1)), ConstUtils.UTF_8);
		value = StringUtil.replace(value, "</svg>", "<circle style=\"stroke:#F0F0F0;stroke-width:16\" cx=\"256\" cy=\"256\" r=\"256\"/></svg>");
		return value;
	}

	public String fa(String group, String icon, Number height, String colorPrimary, String colorSecondary) throws Exception {
		String value = "";
		Element root = null;
		if (resourceFiles.containsKey(group)) {
			root = (Element) resourceFiles.get(group);
		} else {
			Document doc = XMLUtils.copyFrom(file.getFile().getParentFile().getParentFile().getAbsolutePath() + "/svg/" + group + ".svg");
			resourceFiles.put(group, root = doc.getRootElement());
		}
		for (Element element : root.elements()) {
			if (icon == null) {
				String id = element.attributeValue("id");
				if (id != null && !id.contains("font-awesome")) {
					value += "," + id;
				}
			} else {
				if (icon.equalsIgnoreCase(element.attributeValue("id"))) {
					for (Element path : element.elements("path")) {
						if (path.attributeValue("d").isEmpty()) {
							path.detach();
						}
					}
					value = element.asXML().replace("symbol", "svg");
					if (value.contains("fa-primary")) {
						value = value.replace("class=\"fa-primary\"", "fill=\"" + colorPrimary + "\"");
						if (colorSecondary != null) {
							value = value.replace("class=\"fa-secondary\"", "fill=\"" + colorSecondary + "\"");
						}
					} else {
						if (colorSecondary != null) {
							value = value.replace("<path ", "<path stroke-width=\"1\" stroke=\"" + colorSecondary + "\" fill=\"" + colorPrimary + "\" ");
						} else {
							value = value.replace("<path ", "<path fill=\"" + colorPrimary + "\" ");
						}
					}
					int x1 = value.indexOf("viewBox");
					int x2 = value.indexOf("\"", x1 + 9);
					String viewBox = value.substring(x1 + 9, x2);
					int w = Integer.parseInt(StringUtil.split(viewBox, ' ')[2]);
					int h = Integer.parseInt(StringUtil.split(viewBox, ' ')[3]);
					value = value.substring(0, 5) + " width=\"" + (height.floatValue() * w / h) + "\" height=\"" + height + "\" " + value.substring(x1);
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
