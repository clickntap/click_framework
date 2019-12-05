package com.clickntap.tool.html;

import java.io.IOException;
import java.io.Reader;

public class EntityPreserveReader extends java.io.FilterReader {
	private static final char AMP = '&';
	private static final char[] AMP_ENTITY = { ';', 'p', 'm', 'a' };
	private int x0 = -1, x1 = -1;

	public EntityPreserveReader(Reader r) {
		super(r);
	}

	public int read() throws IOException {
		if (x0 == -1) {
			int c = in.read();
			if (c == AMP)
				x0 = 3;
			return c;
		} else
			return AMP_ENTITY[x0--];
	}

	public int read(char buffer[], int offset, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			int c = read();
			if (c == -1)
				return i;
			else
				buffer[i + offset] = (char) c;
		}
		return length;
	}

	public boolean ready() throws IOException {
		if (x0 != -1)
			return true;
		return in.ready();
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public void mark(int limit) throws IOException {
		x1 = x0;
		in.mark(limit);
	}

	public void reset() throws IOException {
		x0 = x1;
		in.reset();
	}
}