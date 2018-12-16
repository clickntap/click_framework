package com.clickntap.tool.html;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.text.html.parser.ParserDelegator;

public class HTMLParser {
	private static final String BODY_START_TAG = "<body>";
	private static final String BODY_END_TAG = "</body>";

	public static String parse(String html, HTMLFilter filter) throws Exception {
		html = parse(new StringReader(html), new StringWriter(), filter);
		html = html.replace("&amp;amp;", "&");
		return html;
	}

	public static String getText(String html) throws Exception {
		return parse(new StringReader(html), new StringWriter());
	}

	public static String parseBody(String html, HTMLFilter filter) throws Exception {
		if (html.indexOf(BODY_START_TAG) < 0)
			html = parse(new StringReader(BODY_START_TAG + html + BODY_END_TAG), new StringWriter(), filter);
		else
			html = parse(new StringReader(html), new StringWriter(), filter);
		int x1 = html.indexOf(BODY_START_TAG);
		int x2 = html.indexOf(BODY_END_TAG);
		return x1 > 0 && x2 > 0 ? html.substring(x1 + BODY_START_TAG.length(), x2).trim() : html.trim();
	}

	public static String parse(Reader r, Writer w, HTMLFilter filter) throws Exception {
		ParserDelegator parser = new HTMLParserDelegator();
		parser.parse(new EntityPreserveReader(r), new HTMLParserCallBack(w, filter), true);
		return w.toString().trim();
	}

	public static String parse(Reader r, Writer w) throws Exception {
		ParserDelegator parser = new HTMLParserDelegator();
		parser.parse(new EntityPreserveReader(r), new TextParserCallBack(w), true);
		return w.toString().trim();
	}
}