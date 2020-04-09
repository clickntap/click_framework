package com.clickntap.api;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import com.clickntap.utils.ConstUtils;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import freemarker.template.utility.StringUtil;

public class ApiUtils {

	public static final String JSON_CONTENT_TYPE_UTF_8 = "application/json; charset=UTF-8";
	public static final String JAVASCRIPT_CONTENT_TYPE_UTF_8 = "application/javascript; charset=UTF-8";
	public static final String CSS_CONTENT_TYPE_UTF_8 = "text/css; charset=UTF-8";
	public static final String TEXT_CONTENT_TYPE_UTF_8 = "text/plain; charset=UTF-8";

	public static List<String> path(HttpServletRequest request, String folder) {
		String uri = request.getRequestURI();
		return path(uri, folder);
	}

	public static List<String> path(String uri, String folder) {
		String folderPath = new StringBuffer().append('/').append(folder).append('/').toString();
		if (uri.indexOf(folderPath) > 0) {
			uri = uri.substring(uri.indexOf(folderPath));
		}
		if (uri.indexOf(folderPath) == 0) {
			return Arrays.asList(StringUtil.split(uri.substring(folderPath.length()), '/'));
		}
		return null;
	}

	public static List<String> path(HttpServletRequest request) {
		return path(request, "api");
	}

	public static ModelAndView out(HttpServletRequest request, HttpServletResponse response, JSONObject json) throws Exception {
		String callback = request.getParameter("callback");
		if (callback == null) {
			return out(response, json.toString(), JSON_CONTENT_TYPE_UTF_8);
		} else {
			return out(response, new StringBuffer(callback).append('(').append(json.toString()).append(')').toString(), JAVASCRIPT_CONTENT_TYPE_UTF_8);
		}
	}

	public static ModelAndView out(HttpServletResponse response, String code, String contentType) throws Exception {
		response.setContentType(contentType);
		response.getOutputStream().write(code.getBytes(ConstUtils.UTF_8));
		return null;
	}

	public static String jsCompile(String js) {
		Compiler compiler = new Compiler();
		CompilerOptions options = new CompilerOptions();
		options.setEmitUseStrict(false);
		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		SourceFile source = SourceFile.fromCode("js", js);
		compiler.compile(Collections.<SourceFile>emptyList(), Collections.singletonList(source), options);
		String code = compiler.toSource();
		return code;

	}

	public static String codeFormat(String f) {
		return codeFormat(f, '{', '}');
	}

	public static String codeFormat(String f, char bracketOn, char bracketOff) {
		StringBuffer code = new StringBuffer();
		String[] lines = StringUtil.split(f, '\n');
		int indent = 0;
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			StringBuffer formattedLine = new StringBuffer();
			int c1 = 0;
			int c2 = 0;
			int i1 = 0;
			int i2 = 0;
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) == bracketOn) {
					i1 = i;
					c1++;
				}
				if (line.charAt(i) == bracketOff) {
					i2 = i;
					c2++;
				}
			}
			if (c2 > c1) {
				indent += (2 * (c1 - c2));
			}
			if (c2 == c1 && c1 == 1 && i1 > i2) {
				indent -= 2;
			}
			for (int i = 0; i < indent; i++) {
				formattedLine.append(' ');
			}
			if (c2 == c1 && c1 == 1 && i1 > i2) {
				indent += 2;
			}
			formattedLine.append(line).append('\n');
			if (c1 > c2) {
				indent += (2 * (c1 - c2));
			}
			if (formattedLine.charAt(0) == bracketOff) {
				formattedLine.append('\n');
			}
			code.append(formattedLine);
		}
		return code.toString();
	}

	public static String toCamelCase(String value, boolean startWithLowerCase) {
		String[] strings = StringUtil.split(value.toLowerCase(), '_');
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			if ((i == 0 && !startWithLowerCase) || i > 0) {
				sb.append(StringUtil.capitalize(strings[i]));
			} else {
				sb.append(strings[i]);
			}
		}
		return sb.toString();
	}

	public static String toCamelCase(String value) {
		return toCamelCase(value, true);
	}

	public static String lastModified(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String lastModified = simpleDateFormat.format(date);
		return lastModified;
	}

	public static void main(String args[]) {
		String js = "var variable = 0; function hello(a,b) {}; hello();";
		System.out.println(ApiUtils.jsCompile(js));
	}

}
