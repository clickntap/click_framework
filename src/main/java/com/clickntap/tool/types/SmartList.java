package com.clickntap.tool.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.clickntap.smart.SmartContext;

public class SmartList {
	private int count;
	private int offset;
	private int maxSize;
	private List items;

	public SmartList(SmartContext ctx, int maxSize, int leftOffset, Collection collection) {
		super();
		this.maxSize = maxSize;
		try {
			this.offset = Integer.parseInt(ctx.param("scroll"));
		} catch (Throwable e) {
			this.offset = 0;
		}
		this.offset -= leftOffset;
		this.count = collection.size();
		this.items = new ArrayList();
		this.items.addAll(collection);
	}

	public List getItems() {
		return items;
	}

	public Object elementAt(int i) {
		try {
			if (i >= offset && i < offset + maxSize)
				return items.get(i);
		} catch (Exception e) {
		}
		return null;
	}

	public int getCount() {
		return count;
	}

	public int getOffset() {
		return offset;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getRightSize() {
		int size = count - (offset + maxSize);
		return size > 0 ? size : 0;
	}

}
