package com.clickntap.utils;

public class ConstUtils {
	public static final String TEXT_HTML_CONTENT_TYPE = "text/html; charset=UTF-8";
	public static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain; charset=UTF-8";
	public static final String EMPTY = "";
	public static final Character BACKSLASH_CHAR = '\\';
	public static final Character SLASH_CHAR = '/';
	public static final Character DOT_CHAR = '.';
	public static final Character SPACE_CHAR = ' ';
	public static final Character QUESTION_MARK_CHAR = '?';
	public static final Character SEMICOLON_CHAR = ';';
	public static final Character COLON_CHAR = ':';
	public static final Character NEWLINE_CHAR = '\n';
	public static final Character RETURN_CHAR = '\r';
	public static final Character MINUS_CHAR = '-';
	public static final Character COMMA_CHAR = ',';
	public static final Character QUOT_CHAR = '"';
	public static final String EXTENSION_XML = "xml";
	public static final String EXTENSION_HTM = "htm";
	public static final String EXTENSION_BIN = "bin";
	public static final String EXTENSION_TMP = "tmp";
	public static final String THIS = "this";

	public static final String BACKSLASH = Character.toString(BACKSLASH_CHAR);
	public static final String SLASH = Character.toString(SLASH_CHAR);
	public static final String DOT = Character.toString(DOT_CHAR);
	public static final String QUESTION_MARK = Character.toString(QUESTION_MARK_CHAR);

	public static final String DOTDOT = DOT + DOT;
	public static final String EXTENSION_DOTXML = DOT + EXTENSION_XML;
	public static final String EXTENSION_DOTHTM = DOT + EXTENSION_HTM;
	public static final String EXTENSION_DOTBIN = DOT + EXTENSION_BIN;
	public static final String EXTENSION_DOTTMP = DOT + EXTENSION_TMP;
	public static final String GT = ">";
	public static final String LT = "<";
	public static final String NULL = "null";
	public static final String SPACE = Character.toString(SPACE_CHAR);
	public static final String SEMICOLON = Character.toString(SEMICOLON_CHAR);
	public static final String COLON = Character.toString(COLON_CHAR);
	public static final String NEWLINE = Character.toString(NEWLINE_CHAR);
	public static final String RETURN = Character.toString(RETURN_CHAR);
	public static final String UTF_8 = "UTF-8";
	public static final String MINUS = Character.toString(MINUS_CHAR);
	public static final String COMMA = Character.toString(COMMA_CHAR);
	public static final String QUOT = Character.toString(QUOT_CHAR);
	public static final String LOCALE_EN = "en";

	public static boolean isEmpty(Object o) {
		return o == null || o.toString().trim().equals(EMPTY);
	}
}
