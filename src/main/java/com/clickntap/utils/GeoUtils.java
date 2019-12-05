package com.clickntap.utils;

public class GeoUtils {

	public static void main(String args[]) {
		System.out.println(GeoUtils.toDecimalLat(-10.633889));
		System.out.println(GeoUtils.toFloat(GeoUtils.toDecimalLat(-10.633889)));
		System.out.println(GeoUtils.toDecimalLat(10.633889));
		System.out.println(GeoUtils.toFloat(GeoUtils.toDecimalLat(10.633889)));
		System.out.println(GeoUtils.toDecimalLng(-10.633889));
		System.out.println(GeoUtils.toFloat(GeoUtils.toDecimalLng(-10.633889)));
		System.out.println(GeoUtils.toDecimalLng(10.633889));
		System.out.println(GeoUtils.toFloat(GeoUtils.toDecimalLng(10.633889)));

	}

	public static Float toFloat(String decimal) {
		try {
			try {
				return Float.parseFloat(decimal);
			} catch (Exception e) {
				int i = 0;
				decimal = decimal.trim().toLowerCase();
				while ((Character.isDigit(decimal.charAt(i)) || decimal.charAt(i) == '.' || decimal.charAt(i) == '-') && i < decimal.length()) {
					i++;
				}
				Number n1 = Float.parseFloat(decimal.substring(0, i));
				int j;
				while (!Character.isDigit(decimal.charAt(i)) && i < decimal.length()) {
					i++;
				}
				j = i;
				while ((Character.isDigit(decimal.charAt(i)) || decimal.charAt(i) == '.' || decimal.charAt(i) == '-') && i < decimal.length()) {
					i++;
				}
				Number n2 = Float.parseFloat(decimal.substring(j, i));
				while (!Character.isDigit(decimal.charAt(i)) && i < decimal.length()) {
					i++;
				}
				j = i;
				while ((Character.isDigit(decimal.charAt(i)) || decimal.charAt(i) == '.' || decimal.charAt(i) == '-') && i < decimal.length()) {
					i++;
				}
				Number n3 = Float.parseFloat(decimal.substring(j, i));
				if (decimal.substring(i).equalsIgnoreCase("s") || decimal.substring(i).equalsIgnoreCase("w"))
					return -(n1.intValue() + n2.floatValue() / 60 + n3.floatValue() / 3600);
				else
					return (n1.intValue() + n2.floatValue() / 60 + n3.floatValue() / 3600);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static String toDecimalLat(Number n) {
		int deg = n.intValue();
		int min = (int) ((n.floatValue() - deg) * 60);
		int sec = (int) ((n.floatValue() - deg) * 3.6);
		return Math.abs(deg) + "° " + Math.abs(min) + "' " + Math.abs(sec) + "" + ((n.intValue() < 0) ? "S" : "N");
	}

	public static String toDecimalLng(Number n) {
		int deg = n.intValue();
		int min = (int) ((n.floatValue() - deg) * 60);
		int sec = (int) ((n.floatValue() - deg) * 3.6);
		return Math.abs(deg) + "° " + Math.abs(min) + "' " + Math.abs(sec) + "" + ((n.intValue() < 0) ? "W" : "E");
	}
}
