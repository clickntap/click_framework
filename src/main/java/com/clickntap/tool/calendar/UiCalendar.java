package com.clickntap.tool.calendar;

import java.util.HashMap;
import java.util.Map;

import com.clickntap.hub.BO;
import com.clickntap.smart.SmartContext;
import com.clickntap.tool.types.Datetime;

public class UiCalendar extends BO {
	private String uicalendarChannel;
	private Datetime uicalendarMonth;
	private Datetime uicalendarDate;
	private Datetime uicalendarDatetime;
	private Map<String, UiCalendarInstance> uicalendarMap;
	private SmartContext ctx;
	transient private int index;

	public UiCalendar() {
		uicalendarMap = new HashMap<String, UiCalendarInstance>();
	}

	public Datetime getUicalendarMonth() {
		if (uicalendarMonth == null)
			setUicalendarMonth(new Datetime());
		return uicalendarMonth;
	}

	public void setUicalendarMonth(Datetime uicalendarMonth) {
		this.uicalendarMonth = uicalendarMonth;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public SmartContext getCtx() {
		return ctx;
	}

	public void setCtx(SmartContext ctx) {
		this.ctx = ctx;
	}

	public UiCalendarInstance getInstance() {
		if (!uicalendarMap.containsKey(getUicalendarChannel())) {
			UiCalendarInstance instance = new UiCalendarInstance();
			UiCalendarManager manager = (UiCalendarManager) ctx.getBean("uiCalendar");
			instance.setConf(manager.getConf(getUicalendarChannel(), ctx));
			uicalendarMap.put(getUicalendarChannel(), instance);
		}
		return uicalendarMap.get(getUicalendarChannel());
	}

	public String getUicalendarChannel() {
		return uicalendarChannel;
	}

	public void setUicalendarChannel(String uicalendarChannel) {
		this.uicalendarChannel = uicalendarChannel;
	}

	public Datetime getUicalendarDate() {
		return uicalendarDate;
	}

	public void setUicalendarDate(Datetime uicalendarDate) {
		this.uicalendarDate = uicalendarDate;
	}

	public Datetime getUicalendarDatetime() {
		return uicalendarDatetime;
	}

	public void setUicalendarDatetime(Datetime uicalendarDatetime) {
		this.uicalendarDatetime = uicalendarDatetime;
	}

	public void load(String channel) throws Exception {
		setUicalendarChannel(channel);
		getConf().load(ctx);
	}

	public void save() throws Exception {
		getConf().save(ctx);
	}

	public Datetime getUicalendarUndoStart() {
		return getInstance().getUicalendarUndoStart();
	}

	public void setUicalendarUndoStart(Datetime dateStart) {
		getInstance().setUicalendarUndoStart(ctx, dateStart);
	}

	public Datetime getUicalendarUndoEnd() {
		return getInstance().getUicalendarUndoEnd();
	}

	public void setUicalendarUndoEnd(Datetime dateEnd) {
		getInstance().setUicalendarUndoEnd(ctx, dateEnd);
	}

	public String getUicalendarSelectMode() {
		return getInstance().getUicalendarSelectMode();
	}

	public void setUicalendarSelectMode(String selectMode) {
		getInstance().setUicalendarSelectMode(selectMode);
	}

	public void modeStart() throws Exception {
		update();
		getInstance().setUicalendarSelectMode("start");
	}

	public void modeEnd() throws Exception {
		update();
		getInstance().setUicalendarSelectMode("end");
	}

	public Datetime getUicalendarDateStart() {
		return getInstance().getUicalendarDateStart();
	}

	public void setUicalendarDateStart(Datetime dateStart) {
		getInstance().setUicalendarDateStart(ctx, dateStart);
	}

	public Boolean isDayInRange(Datetime day) {
		return getInstance().isDayInRange(day);
	}

	public Datetime dateStartFromMonday(Number n) {
		return getInstance().dateStartFromMonday(n);
	}

	public Datetime dateStartForMonth(Number n) {
		Datetime t = new Datetime(getUicalendarMonth());

		t.setDayOfMonth(1);

		while (t.getDayOfWeek() != 2) {
			t.setDayOfWeek(t.getDayOfWeek() - 1);
		}

		t.setDate(t.getDate() + n.intValue());

		return t;
	}

	public Datetime dateEndFromMonday(Number n) {
		return getInstance().dateEndFromMonday(n);
	}

	public Datetime getUicalendarDateEnd() {
		return getInstance().getUicalendarDateEnd();
	}

	public void setUicalendarDateEnd(Datetime dateEnd) {
		getInstance().setUicalendarDateEnd(ctx, dateEnd);
	}

	public void today() throws Exception {
		getInstance().today(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void thisWeek() throws Exception {
		getInstance().thisWeek(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void thisMonth() throws Exception {
		getInstance().thisMonth(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void last7days() throws Exception {
		getInstance().last7days(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void last30days() throws Exception {
		getInstance().last30days(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void tomorrow() throws Exception {
		getInstance().tomorrow(ctx);
		setUicalendarMonth(getInstance().getUicalendarDateStart());
	}

	public void setBirthday() throws Exception {
		setUicalendarDateStart(uicalendarDate);
		setUicalendarDateEnd(uicalendarDate);
		update();
		getInstance().setUicalendarSelectMode("start");
	}

	public void undo() throws Exception {
		setUicalendarDateStart(getUicalendarUndoStart());
		setUicalendarDateEnd(getUicalendarUndoEnd());
		update();
	}

	public Number update() throws Exception {
		getInstance().setUicalendarDate(ctx, getUicalendarDate());
		getInstance().setUicalendarDatetime(ctx, getUicalendarDatetime());
		getConf().save(ctx);
		setUicalendarDate(null);
		setUicalendarDatetime(null);
		if ("start".equals(getInstance().getUicalendarSelectMode())) {
			getInstance().setUicalendarSelectMode("end");
		} else {
			getInstance().setUicalendarSelectMode("start");
		}
		return 0;
	}

	public UICalendarConf getConf() {
		return getInstance().getConf();
	}

}
