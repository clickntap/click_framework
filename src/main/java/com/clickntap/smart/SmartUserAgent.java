package com.clickntap.smart;

public class SmartUserAgent {
	private String channel;
	private String header;
	private String ip;
	private String host;

	public SmartUserAgent(SmartContext smartContext) {
		header = smartContext.getRequest().getHeader("User-Agent");
		ip = smartContext.getRequest().getRemoteAddr();
		host = smartContext.getRequest().getRemoteHost();
		String ua = header.toLowerCase();
		channel = "web";
		if (ua.indexOf("windows ce") >= 0)
			channel = "mobile";
		if (ua.indexOf("iphone os") >= 0)
			channel = "mobile";
		if (ua.indexOf("mobile") >= 0)
			channel = "mobile";
		if (ua.indexOf("series") >= 0)
			channel = "mobile";
		if (ua.indexOf("armv") >= 0)
			channel = "mobile";
		if (ua.indexOf("vodafone") >= 0)
			channel = "mobile";
		if (ua.indexOf("android") >= 0)
			channel = "mobile";
	}

	public String getChannel() {
		return channel;
	}

	public String getHeader() {
		return header;
	}

	public String getIp() {
		return ip;
	}

	public String getHost() {
		return host;
	}
}
