package com.clickntap.tool.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import com.clickntap.utils.ConstUtils;

import freemarker.template.utility.StringUtil;

public class HTMLParserCallBack extends ParserCallback {
	private static final char EOL = '\n';
	private static final String EQUALS = "=";
	private static final String GT = ">";
	private static final String LT = "<";
	private static final String ENDTAG = "endtag";
	private static final String QUOT = "\"";
	private Writer writer;
	private HTMLFilter filter;

	public HTMLParserCallBack(Writer writer, HTMLFilter filter) {
		this.writer = writer;
		this.filter = filter;
	}

	public void handleComment(char[] comment, int index) {
	}

	public void handleStartTag(Tag tag, MutableAttributeSet attributeSet, int index) {
		try {
			HTMLTag htmlTag = filter.getTag(tag.toString());
			if (htmlTag.isEnabled()) {
				if (htmlTag.isEolBeforeStartTag())
					writer.write(EOL);
				writer.write(LT);
				writer.write(tag.toString());
				handleAttributeSet(tag, attributeSet);
				writer.write(GT);
				if (htmlTag.isEolAfterStartTag())
					writer.write(EOL);
			} else if (tag.breaksFlow())
				writer.write(ConstUtils.SPACE);
		} catch (Exception e) {
		}
	}

	public void handleEndTag(Tag tag, int index) {
		try {
			HTMLTag htmlTag = filter.getTag(tag.toString());
			if (htmlTag.isEnabled()) {
				if (htmlTag.isEolBeforeEndTag())
					writer.write(EOL);
				writer.write(LT);
				writer.write(ConstUtils.SLASH);
				writer.write(tag.toString());
				writer.write(GT);
				if (htmlTag.isEolAfterEndTag())
					writer.write(EOL);
			} else if (tag.breaksFlow())
				writer.write(ConstUtils.SPACE);
		} catch (Exception e) {
		}
	}

	private void handleAttribute(Tag tag, Object name, Object value) throws Exception {
		HTMLAttribute htmlAttribute = filter.getTag(tag.toString()).getAttribute(name);
		if (htmlAttribute.isStyle()) {
			StringTokenizer tokenizer = new StringTokenizer(value.toString(), ConstUtils.SEMICOLON);
			StringBuffer sb = new StringBuffer();
			while (tokenizer.hasMoreElements())
				sb.append(htmlAttribute.parseStyle(tokenizer.nextElement().toString()));
			if (sb.length() > 0)
				writeAttribute(name, sb.toString());
		} else if (htmlAttribute.isEnabled()) {
			if (htmlAttribute.isColor())
				writeAttribute(name, htmlAttribute.parseColor(value.toString()));
			else
				writeAttribute(name, value.toString());
		}
	}

	private void writeAttribute(Object name, String value) throws IOException {
		writer.write(ConstUtils.SPACE);
		writer.write(name.toString());
		writer.write(EQUALS);
		writer.write(QUOT);
		writer.write(StringUtil.XHTMLEnc(value));
		writer.write(QUOT);
	}

	public void handleText(char[] text, int index) {
		try {
			writer.write(text);
		} catch (Exception e) {
		}
	}

	public void handleSimpleTag(Tag tag, MutableAttributeSet attributeSet, int index) {
		try {
			if (!isEndTag(attributeSet))
				handleStartTag(tag, attributeSet, index);
			else
				handleEndTag(tag, index);
		} catch (Exception e) {
		}
	}

	private boolean isEndTag(MutableAttributeSet attributeSet) {
		try {
			Enumeration en = attributeSet.getAttributeNames();
			while (en.hasMoreElements())
				if (en.nextElement().toString().equals(ENDTAG))
					return true;
		} catch (Exception e) {
		}
		return false;
	}

	private void handleAttributeSet(Tag tag, MutableAttributeSet attributeSet) {
		try {
			if (attributeSet.getAttributeCount() > 0) {
				Enumeration en = attributeSet.getAttributeNames();
				Object attributeName;
				while (en.hasMoreElements()) {
					attributeName = en.nextElement();
					if (!attributeName.toString().equals(ENDTAG))
						handleAttribute(tag, attributeName, attributeSet.getAttribute(attributeName));
				}
			}
		} catch (Exception e) {
		}
	}
}