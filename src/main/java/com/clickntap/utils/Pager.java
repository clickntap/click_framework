package com.clickntap.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pager implements Serializable {

	private int pageNumber;

	private int numberOfLinks;

	private int numberOfRows;

	private List<?> page;

	private int numberOfPages;

	private int size;

	private int fromRow;

	private int toRow;

	public Pager(int numberOfLinks, int numberOfRows) {
		this.pageNumber = 1;
		this.numberOfLinks = numberOfLinks > 0 ? numberOfLinks : 5;
		this.numberOfRows = numberOfRows > 0 ? numberOfRows : 5;
	}

	public Pager selectPage(List<?> list, int pageNumber) throws Exception {
		this.pageNumber = pageNumber > 0 ? pageNumber : 1;
		size = (list != null ? list.size() : 0);
		numberOfPages = size / numberOfRows;
		if (size % numberOfRows > 0)
			numberOfPages++;
		if (pageNumber > numberOfPages)
			pageNumber = numberOfPages;
		fromRow = ((pageNumber - 1) * numberOfRows);
		toRow = fromRow + numberOfRows;
		if (toRow >= size)
			toRow = size;
		if (fromRow < 0)
			fromRow = 0;
		if (list != null)
			page = list.subList(fromRow, toRow);
		return this;
	}

	public List<Number> getLinks() {
		List<Number> links = new ArrayList<Number>();

		int s, e;
		int m = (numberOfLinks / 2);

		if (pageNumber > numberOfPages - m) {
			e = numberOfPages;
			s = e - numberOfLinks + 1;
			if (s < 1)
				s = 1;
		} else {
			s = pageNumber - m;
			if (s < 1)
				s = 1;
			e = s + numberOfLinks - 1;
			if (e > numberOfPages)
				e = numberOfPages;
		}

		for (int i = s; i <= e; i++)
			links.add(i);

		return links;
	}

	public List<?> getPage() {
		return page;
	}

	public int getLastPage() {
		return getNumberOfPages();
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public int getFromRow() {
		return fromRow;
	}

	public int getToRow() {
		return toRow - 1;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public boolean isFirst() {
		return pageNumber == 1;
	}

	public boolean isLast() {
		return pageNumber == numberOfPages || size == 0;
	}

	public int getPrevPage() {
		return (!isFirst() ? pageNumber - 1 : 1);
	}

	public int getNextPage() {
		return (!isLast() ? pageNumber + 1 : numberOfPages);
	}

	public int getSize() {
		return size;
	}

	public void refreshList(List<?> list) throws Exception {
		selectPage(list, getPageNumber());
	}

}
