package com.clickntap.tool.html;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.ParserDelegator;

public class HTMLParserDelegator extends ParserDelegator {
	private static final String HTML32 = "html32";

	public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
		new HTMLDocumentParser(DTD.getDTD(HTML32)).parse(r, cb, ignoreCharSet);
	}
}