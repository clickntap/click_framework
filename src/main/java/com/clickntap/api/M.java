package com.clickntap.api;

import java.lang.reflect.Method;

public class M {
	public static Object invoke(Object o, String name, Object value) throws Exception {
		Method method = null;
		for (Method aMethod : o.getClass().getMethods()) {
			if (aMethod.getName().contentEquals(name)) {
				method = aMethod;
				break;
			}
		}
		Object result = null;
		try {
			result = method.invoke(o);
		} catch (Exception e1) {
			result = method.invoke(o, value);
		}
		return result;
	}

	public static boolean has(Object o, String name) throws Exception {
		for (Method aMethod : o.getClass().getMethods()) {
			if (aMethod.getName().contentEquals(name)) {
				return true;
			}
		}
		return false;
	}

	public static Object invoke(Object o, String name) throws Exception {
		return M.invoke(o, name, null);
	}
}
