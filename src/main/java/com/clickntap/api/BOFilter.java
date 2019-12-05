package com.clickntap.api;

import com.clickntap.tool.types.Datetime;

public class BOFilter extends BO {

	private String query;
	private Datetime startDate;
	private Datetime endDate;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Datetime getStartDate() {
		return startDate;
	}

	public void setStartDate(Datetime startDate) {
		this.startDate = startDate;
	}

	public Datetime getEndDate() {
		return endDate;
	}

	public void setEndDate(Datetime endDate) {
		this.endDate = endDate;
	}
}
