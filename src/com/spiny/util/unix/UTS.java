package com.spiny.util.unix;

import java.util.Calendar;

public final class UTS {
	
	private static final int SECOND_HOUR_RATIO = 3600;
	
	private int seconds;
	
	public int toSeconds() {
		return seconds;
	}
	
	public int toHours() {
		return seconds / UTS.SECOND_HOUR_RATIO;
	}
	
	public UTS sub(UTS other) {
		return new UTS(seconds - other.seconds);
	}
	
	public UTS add(UTS other) {
		return new UTS(seconds + other.seconds);
	}
	
	private UTS(int seconds) {
		this.seconds = seconds;
	}
	
	public static UTS fromSeconds(int seconds) {
		return new UTS(seconds);
	}
	
	public static UTS fromHours(int hours) {
		return new UTS(hours * UTS.SECOND_HOUR_RATIO);
	}
	
	public static UTS fromCalender(Calendar c) {
		return new UTS((int) c.getTimeInMillis() / 1000);
	}
	
	public static UTS currentTime() {
		return new UTS((int) (System.currentTimeMillis() / 1000));
	}
	
}
