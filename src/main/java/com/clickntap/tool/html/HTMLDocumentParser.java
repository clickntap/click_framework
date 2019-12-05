package com.clickntap.tool.html;

import javax.swing.text.html.parser.DTD;

public class HTMLDocumentParser extends javax.swing.text.html.parser.DocumentParser {
	public static final String TAGCLOSER = "/>";
	public static final String XML_TAGNAME = "?xml";
	public static final String EXPECTED_TAGNAME_ERROR_KEY = "expected.tagname";
	public boolean expectedTagName = false;

	public HTMLDocumentParser(DTD dtd) {
		super(dtd);
	}

	protected void error(String errorKey) {
		if (errorKey.equals(EXPECTED_TAGNAME_ERROR_KEY))
			expectedTagName = true;
		else
			super.error(errorKey);
	}

	protected void handleText(char data[]) {
		if (expectedTagName) {
			expectedTagName = false;
			int x0, x1;
			String s = new String(data);
			if ((x0 = s.indexOf(XML_TAGNAME)) != -1 && (x1 = s.indexOf(TAGCLOSER, x0)) != -1) {
				s = (x1 + 2 >= s.length()) ? s.substring(0, x0) : s.substring(0, x0) + s.substring(x1 + 2);
				super.handleText(s.toCharArray());
				return;
			} else
				super.error(EXPECTED_TAGNAME_ERROR_KEY);
		}
		super.handleText(data);
	}
}