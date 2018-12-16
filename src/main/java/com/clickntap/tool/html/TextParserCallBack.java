package com.clickntap.tool.html;

import java.io.Writer;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import com.clickntap.utils.ConstUtils;

public class TextParserCallBack extends ParserCallback {
	private Writer writer;

	public TextParserCallBack(Writer writer) {
		this.writer = writer;
	}

	public void handleStartTag(Tag tag, MutableAttributeSet attributeSet, int index) {
		try {
			if (tag.breaksFlow())
				writer.write(ConstUtils.SPACE);
		} catch (Exception e) {
		}
	}

	public void handleEndTag(Tag tag, int index) {
		try {
			if (tag.breaksFlow())
				writer.write(ConstUtils.SPACE);
		} catch (Exception e) {
		}
	}

	public void handleText(char[] text, int index) {
		try {
			writer.write(text);
		} catch (Exception e) {
		}
	}

	public void handleSimpleTag(Tag tag, MutableAttributeSet attributeSet, int index) {
		try {
			if (tag.breaksFlow())
				writer.write(ConstUtils.SPACE);
		} catch (Exception e) {
		}
	}
}