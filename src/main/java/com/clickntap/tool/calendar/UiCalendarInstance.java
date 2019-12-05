package com.clickntap.tool.calendar;

import java.util.Calendar;

import com.clickntap.hub.BO;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.types.Datetime;

public class UiCalendarInstance extends BO {
	private String uicalendarSelectMode;
	private String uicalendarChannel;
	private Datetime uicalendarDateStart;
	private Datetime uicalendarDateEnd;
	private Datetime uicalendarUndoStart;
	private Datetime uicalendarUndoEnd;
	private UICalendarConf conf;

	public UICalendarConf getConf() {
		return conf;
	}

	public void setConf(UICalendarConf conf) {
		this.conf = conf;
	}

	public String getUicalendarChannel() {
		return uicalendarChannel;
	}

	public void setUicalendarChannel(String uicalendarChannel) {
		this.uicalendarChannel = uicalendarChannel;
	}

	public String getUicalendarSelectMode() {
		return uicalendarSelectMode;
	}

	public void setUicalendarSelectMode(String selectMode) {
		this.uicalendarSelectMode = selectMode;
	}

	public void setUicalendarDate(SmartContext ctx, Datetime uicalendarDate) {
		if (uicalendarDate != null) {
			if ("start".equals(getUicalendarSelectMode())) {
				int h = this.uicalendarDateStart.getHourOfDay();
				int m = this.uicalendarDateStart.getMinute();
				setUicalendarDateStart(ctx, uicalendarDate);
				this.uicalendarDateStart.setHourOfDay(h);
				this.uicalendarDateStart.setMinute(m);
			}
			if ("end".equals(getUicalendarSelectMode())) {
				int h = this.uicalendarDateEnd.getHourOfDay();
				int m = this.uicalendarDateEnd.getMinute();
				setUicalendarDateEnd(ctx, uicalendarDate);
				this.uicalendarDateEnd.setHourOfDay(h);
				this.uicalendarDateEnd.setMinute(m);
			}
		}
	}

	public void setUicalendarDatetime(SmartContext ctx, Datetime uicalendarDatetime) {
		if (uicalendarDatetime != null) {
			if ("start".equals(getUicalendarSelectMode())) {
				setUicalendarDateStart(ctx, uicalendarDatetime);
			}
			if ("end".equals(getUicalendarSelectMode())) {
				setUicalendarDateEnd(ctx, uicalendarDatetime);
			}
		}
	}

	public UiCalendarInstance() {
		modeStart();
	}

	public void modeStart() {
		uicalendarSelectMode = "start";
	}

	public void modeEnd() {
		uicalendarSelectMode = "end";
	}

	public Datetime getUicalendarDateStart() {
		uicalendarDateStart.setFirstDayOfWeek(Calendar.MONDAY);
		return uicalendarDateStart;
	}

	public Boolean isDayInRange(Datetime day) {
		if (day.dayEnd().getTimeInMillis() < getUicalendarDateStart().dayEnd().getTimeInMillis()) {
			return false;
		}
		if (day.dayEnd().getTimeInMillis() > getUicalendarDateEnd().dayEnd().getTimeInMillis()) {
			return false;
		}
		return true;
	}

	public Datetime dateStartFromMonday(Number n) {
		Datetime t = new Datetime(getUicalendarDateStart());
		t.setDayOfMonth(1);
		while (t.getDayOfWeek() != 2) {
			t.setDayOfWeek(t.getDayOfWeek() - 1);
		}
		t.setFirstDayOfWeek(Calendar.MONDAY);
		t.setDate(t.getDate() + n.intValue());
		return t;
	}

	public Datetime dateEndFromMonday(Number n) {
		Datetime t = new Datetime(getUicalendarDateEnd());
		while (t.getDayOfWeek() != 2) {
			t.setDayOfWeek(t.getDayOfWeek() - 1);
		}
		t.setFirstDayOfWeek(Calendar.MONDAY);
		t.setDate(t.getDate() + n.intValue());
		return t;
	}

	public void setUicalendarDateStart(SmartContext ctx, Datetime dateStart) {
		if (dateStart != null) {
			this.uicalendarDateStart = new Datetime(dateStart);
			try {
				if (dateStart.getTimeInMillis() > uicalendarDateEnd.getTimeInMillis()) {
					// setInterval(null, dateStart, 0, 0);

					int h = uicalendarDateEnd.getHourOfDay();
					int m = uicalendarDateEnd.getMinute();

					this.uicalendarDateEnd = new Datetime(uicalendarDateStart);
					this.uicalendarDateEnd.setHourOfDay(h);
					this.uicalendarDateEnd.setMinute(m);
					return;
				}
			} catch (Exception e) {
			}
		}
	}

	public Datetime getUicalendarDateEnd() {
		uicalendarDateEnd.setFirstDayOfWeek(Calendar.MONDAY);
		return uicalendarDateEnd;
	}

	public void setUicalendarDateEnd(SmartContext ctx, Datetime dateEnd) {
		if (dateEnd != null) {
			this.uicalendarDateEnd = new Datetime(dateEnd);
			try {
				if (dateEnd.getTimeInMillis() < uicalendarDateStart.getTimeInMillis()) {
					int h = uicalendarDateEnd.getHourOfDay();
					int m = uicalendarDateEnd.getMinute();
					this.uicalendarDateEnd = new Datetime(uicalendarDateStart);
					// setInterval(null, dateEnd, 0, 0);
					this.uicalendarDateEnd.setHourOfDay(h);
					this.uicalendarDateEnd.setMinute(m);
					return;
				}
			} catch (Exception e) {
			}
		}
	}

	public Datetime getUicalendarUndoStart() {
		return uicalendarUndoStart;
	}

	public void setUicalendarUndoStart(SmartContext ctx, Datetime dateStart) {
		if (dateStart != null) {
			this.uicalendarDateStart = new Datetime(dateStart);
			try {
				if (dateStart.getTimeInMillis() > uicalendarDateEnd.getTimeInMillis()) {
					setInterval(null, dateStart, 0, 0);
					return;
				}
			} catch (Exception e) {
			}
		}
	}

	public Datetime getUicalendarUndoEnd() {
		return uicalendarUndoEnd;
	}

	public void setUicalendarUndoEnd(SmartContext ctx, Datetime dateEnd) {
		if (dateEnd != null) {
			this.uicalendarDateEnd = new Datetime(dateEnd);
			try {
				if (dateEnd.getTimeInMillis() < uicalendarDateStart.getTimeInMillis()) {
					setInterval(null, dateEnd, 0, 0);
					return;
				}
			} catch (Exception e) {
			}
		}
	}

	public void today(SmartContext ctx) throws Exception {
		setInterval(ctx, 0, 0);
	}

	public void thisWeek(SmartContext ctx) throws Exception {
		Datetime t = new Datetime();
		int ds = 0;
		while (t.getDayOfWeek() != 2) {
			t.setDayOfWeek(t.getDayOfWeek() - 1);
			ds--;
		}
		t = new Datetime();
		int de = 0;
		while (t.getDayOfWeek() != 1) {
			t.setDayOfWeek(t.getDayOfWeek() + 1);
			de++;
		}
		setInterval(ctx, ds, de);
	}

	public void thisMonth(SmartContext ctx) throws Exception {
		Datetime t = new Datetime();
		int ds = 0;
		while (t.getDayOfMonth() != 1) {
			t.setDayOfMonth(t.getDayOfMonth() - 1);
			ds--;
		}
		t = new Datetime();
		int de = 0;
		int m = t.getMonth();
		while (m == t.getMonth()) {
			t.setDayOfMonth(t.getDayOfMonth() + 1);
			de++;
		}
		de--;
		setInterval(ctx, ds, de);
	}

	public void last7days(SmartContext ctx) throws Exception {
		setInterval(ctx, -7, 0);
	}

	public void last30days(SmartContext ctx) throws Exception {
		setInterval(ctx, -30, 0);
	}

	public void tomorrow(SmartContext ctx) throws Exception {
		setInterval(ctx, 1, 1);
	}

	public void setInterval(SmartContext ctx, int ds, int de) throws Exception {
		setInterval(ctx, new Datetime(), ds, de);
	}

	public void setInterval(SmartContext ctx, Datetime t, int ds, int de) throws Exception {
		uicalendarDateStart = startTime(t);
		uicalendarDateStart.add(Datetime.DATE, ds);
		uicalendarDateEnd = endTime(t);
		uicalendarDateEnd.add(Datetime.DATE, de);
		if (ctx != null)
			getConf().save(ctx);
	}

	private Datetime startTime(Datetime t) {
		t = new Datetime(t.format("yyyy-MM-dd ") + getConf().getDefaultStartTime());
		return t;
	}

	private Datetime endTime(Datetime t) {
		t = new Datetime(t.format("yyyy-MM-dd ") + getConf().getDefaultEndTime());
		return t;
	}

}
