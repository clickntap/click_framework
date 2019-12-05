package com.clickntap.tool.types;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Datetime extends GregorianCalendar implements Serializable {

	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final int ONESECONDINMILLIS = 1000;
	public static final int ONEMINUTEINMILLIS = ONESECONDINMILLIS * 60;
	public static final int ONEHOURINMILLIS = ONEMINUTEINMILLIS * 60;
	public static final int ONEDAYINMILLIS = ONEHOURINMILLIS * 24;
	public static final int ONEWEEKINMILLIS = ONEDAYINMILLIS * 7;
	public static final int ONEMONTHINMILLIS = ONEDAYINMILLIS * 30;
	public static final int ONEYEARINMILLIS = ONEDAYINMILLIS * 365;
	public static final int ONESECOND = 1;
	public static final int SEVENTYOFFSETINMILLIS = 70 * ONEYEARINMILLIS + 70 / 4 * ONEDAYINMILLIS;
	public static final int ONEMINUTEINSECONDS = ONESECOND * 60;
	public static final int ONEHOURINSECONDS = ONEMINUTEINSECONDS * 60;
	public static final int ONEDAYINSECONDS = ONEHOURINSECONDS * 24;
	public static final int ONEWEEKINSECONDS = ONEDAYINSECONDS * 7;
	public static final int ONEMONTHINSECONDS = ONEDAYINSECONDS * 30;
	public static final int ONEYEARINSECONDS = ONEDAYINSECONDS * 365;
	public static final int SEVENTYOFFSETINSECONDS = 70 * ONEYEARINSECONDS + 70 / 4 * ONEDAYINSECONDS;
	private static final String NOW = "now";
	private static final String TODAY = "today";
	private static final String TOMORROW = "tomorrow";
	private static final String MONDAY = "monday";
	private static final String TUESDAY = "tuesday";
	private static final String WEDNESDAY = "wednesday";
	private static final String THURSDAY = "thursday";
	private static final String FRIDAY = "friday";
	private static final String SATURDAY = "saturday";
	private static final String SUNDAY = "sunday";
	private static final String NEXTWEEK = "next week";
	private static final String NEXTMONTH = "next month";
	private static final String NEXTYEAR = "next year";
	private String format = "yyyy-MM-dd HH:mm";

	public Datetime() {
	}

	public Datetime(String datetime) {
		set(datetime, true);
	}

	public Datetime(String datetime, boolean startsWithYear) {
		set(datetime, startsWithYear);
	}

	public Datetime(Date datetime) {
		set(format(datetime, Datetime.DEFAULT_FORMAT), true);
	}

	public Datetime(long datetime) {
		this(new Date(datetime));
	}

	public Datetime(Datetime datetime) {
		this(datetime.getTime());
	}

	public static Datetime parseRssDate(String s) throws ParseException {
		return parseIETFDate(s);
	}

	public static Datetime parseIETFDate(String s) throws ParseException {
		return parseDate(s, "EEE, d MMM yyyy HH:mm:ss Z");
	}

	public static Datetime parseDate(String s, String format) throws ParseException {
		return new Datetime(new SimpleDateFormat(format).parse(s));
	}

	public String toIETFDate() throws ParseException {
		return format("EEE, d MMM yyyy HH:mm:ss Z");
	}

	public Datetime clone() {
		return new Datetime(this);
	}

	public void set(String datetime) {
		set(datetime, true);
	}

	public void set(String datetime, boolean startsWithYear) {
		long now = new Date().getTime();
		if (NOW.equals(datetime) || TODAY.equals(datetime))
			return;
		if (TOMORROW.equals(datetime)) {
			set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) + 1);
			return;
		}
		if (NEXTWEEK.equals(datetime)) {
			set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) + 7);
			return;
		}
		if (NEXTMONTH.equals(datetime)) {
			set(Calendar.MONTH, get(Calendar.MONTH) + 1);
			return;
		}
		if (NEXTYEAR.equals(datetime)) {
			set(Calendar.YEAR, get(Calendar.YEAR) + 1);
			return;
		}
		if (MONDAY.equals(datetime)) {
			setNextDay(now, Calendar.MONDAY);
			return;
		}
		if (TUESDAY.equals(datetime)) {
			setNextDay(now, Calendar.TUESDAY);
			return;
		}
		if (WEDNESDAY.equals(datetime)) {
			setNextDay(now, Calendar.WEDNESDAY);
			return;
		}
		if (THURSDAY.equals(datetime)) {
			setNextDay(now, Calendar.THURSDAY);
			return;
		}
		if (FRIDAY.equals(datetime)) {
			setNextDay(now, Calendar.FRIDAY);
			return;
		}
		if (SATURDAY.equals(datetime)) {
			setNextDay(now, Calendar.SATURDAY);
			return;
		}
		if (SUNDAY.equals(datetime)) {
			setNextDay(now, Calendar.SUNDAY);
			return;
		}
		int len = datetime.length();
		if (startsWithYear) {
			if (len < 4)
				throw new IllegalArgumentException();
			set(Calendar.YEAR, Integer.parseInt(datetime.substring(0, 4)));
			if (len >= 7)
				set(Calendar.MONTH, Integer.parseInt(datetime.substring(5, 7)) + Calendar.JANUARY - 1);
			else
				set(Calendar.MONTH, Calendar.JANUARY);
			if (len >= 10)
				set(Calendar.DAY_OF_MONTH, Integer.parseInt(datetime.substring(8, 10)));
			else
				set(Calendar.DAY_OF_MONTH, 1);
		} else {
			if (len < 10)
				throw new IllegalArgumentException();
			set(Calendar.DAY_OF_MONTH, Integer.parseInt(datetime.substring(0, 2)));
			set(Calendar.MONTH, Integer.parseInt(datetime.substring(3, 5)) + Calendar.JANUARY - 1);
			set(Calendar.YEAR, Integer.parseInt(datetime.substring(6, 10)));
		}
		if (len >= 13)
			set(Calendar.HOUR_OF_DAY, Integer.parseInt(datetime.substring(11, 13)));
		else
			set(Calendar.HOUR_OF_DAY, 0);
		if (len >= 16)
			set(Calendar.MINUTE, Integer.parseInt(datetime.substring(14, 16)));
		else
			set(Calendar.MINUTE, 0);
		if (len >= 19)
			set(Calendar.SECOND, Integer.parseInt(datetime.substring(17, 19)));
		else
			set(Calendar.SECOND, 0);
		if (len >= 23)
			set(Calendar.MILLISECOND, Integer.parseInt(datetime.substring(20, 23)));
		else
			set(Calendar.MILLISECOND, 0);
	}

	private void setNextDay(long now, int dayOfWeek) {
		set(Calendar.DAY_OF_WEEK, dayOfWeek);
		if (getTimeInMillis() < now)
			set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) + 7);
	}

	public String format(Date datetime, String pattern, String lang) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, new Locale(lang));
		return formatter.format(datetime);
	}

	public String format(String format, String lang) {
		return format(getTime(), format, lang);
	}

	public String format(Date datetime, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(datetime);
	}

	public String format(String format) {
		return format(getTime(), format);
	}

	public String toString() {
		return format("yyyy-MM-dd HH:mm");
	}

	public int getEra() {
		return get(Calendar.ERA);
	}

	public int getYear() {
		return get(Calendar.YEAR);
	}

	public void setYear(int n) {
		set(Calendar.YEAR, n);
	}

	public int getMonth() {
		return get(Calendar.MONTH);
	}

	public void setMonth(int n) {
		set(Calendar.MONTH, n);
	}

	public int getWeekOfYear() {
		return get(Calendar.WEEK_OF_YEAR);
	}

	public void setWeekOfYear(int n) {
		set(Calendar.WEEK_OF_YEAR, n);
	}

	public int getWeekOfMonth() {
		return get(Calendar.WEEK_OF_MONTH);
	}

	public int getDate() {
		return get(Calendar.DATE);
	}

	public void setDate(int n) {
		set(Calendar.DATE, n);
	}

	public Datetime tomorrow() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() + 1);
		return t;
	}

	public Datetime nextDay() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() + 1);
		return t;
	}

	public Datetime prevDay() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() - 1);
		return t;
	}

	public Datetime nextYear() {
		Datetime t = new Datetime(this);
		t.setYear(getYear() + 1);
		return t;
	}

	public Datetime prevYear() {
		Datetime t = new Datetime(this);
		t.setYear(getYear() - 1);
		return t;
	}

	public Datetime nextMonth() {
		Datetime t = new Datetime(this);
		int startMonth = t.getMonth();
		while (startMonth == t.getMonth()) {
			t.setDate(t.getDate() + 1);
		}
		return t;
	}

	public Datetime prevMonth() {
		Datetime t = new Datetime(this);
		int startMonth = t.getMonth();
		while (startMonth == t.getMonth()) {
			t.setDate(t.getDate() - 1);
		}
		return t;
	}

	public Datetime nextWeek() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() + 7);
		return t;
	}

	public Datetime prevWeek() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() - 7);
		return t;
	}

	public Datetime dayStart() {
		return new Datetime(this.format("yyyy-MM-dd 00:00:00.000"));
	}

	public Datetime dayEnd() {
		return new Datetime(this.format("yyyy-MM-dd 23:59:59.999"));
	}

	public Datetime monthStart() {
		return new Datetime(this.format("yyyy-MM-01 00:00:00.000"));
	}

	public Datetime monthEnd() {
		return new Datetime(this.format("yyyy-MM-" + getMaxDayOfMonth() + " 23:59:59.999"));
	}

	public Datetime yesterday() {
		Datetime t = new Datetime(this);
		t.setDayOfYear(getDayOfYear() - 1);
		return t;
	}

	public int getDayOfMonth() {
		return get(Calendar.DAY_OF_MONTH);
	}

	public void setDayOfMonth(int n) {
		set(Calendar.DAY_OF_MONTH, n);
	}

	public int getDayOfYear() {
		return get(Calendar.DAY_OF_YEAR);
	}

	public void setDayOfYear(int n) {
		set(Calendar.DAY_OF_YEAR, n);
	}

	public int getDayOfWeek() {
		return get(Calendar.DAY_OF_WEEK);
	}

	public void setDayOfWeek(int n) {
		set(Calendar.DAY_OF_WEEK, n);
	}

	public int getDayOfWeekInMonth() {
		return get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}

	public int getAmPm() {
		return get(Calendar.AM_PM);
	}

	public int getHour() {
		return get(Calendar.HOUR);
	}

	public int getHourOfDay() {
		return get(Calendar.HOUR_OF_DAY);
	}

	public void setHourOfDay(int n) {
		set(Calendar.HOUR_OF_DAY, n);
	}

	public int getMinute() {
		return get(Calendar.MINUTE);
	}

	public void setMinute(int n) {
		set(Calendar.MINUTE, n);
	}

	public int getSecond() {
		return get(Calendar.SECOND);
	}

	public void setSecond(int n) {
		set(Calendar.SECOND, n);
	}

	public int getMillisecond() {
		return get(Calendar.MILLISECOND);
	}

	public int getZoneOffset() {
		return get(Calendar.ZONE_OFFSET) / (60 * 60 * 1000);
	}

	public int getDstOffset() {
		return get(Calendar.DST_OFFSET) / (60 * 60 * 1000);
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isEquals(Datetime startDate) {
		try {
			if (toString().equals(startDate.toString()))
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public int getMaxDayOfMonth() {
		Datetime d = new Datetime(this);
		d.setDayOfMonth(1);
		int dayOfMonth = 1;
		int m = d.getMonth();
		while (m == d.getMonth()) {
			dayOfMonth = d.getDayOfMonth();
			d.setDayOfMonth(d.getDayOfMonth() + 1);
		}
		return dayOfMonth;
	}

}
