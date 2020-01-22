package com.clickntap.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class M {
	public static Object invoke(Object o, String name, Object... value) throws Exception {
		return invoke(new ArrayList<Method>(), o, name, value);
	}

	public static Object invoke(List<Method> methods, Object o, String name, Object... value) throws Exception {
		Method method = null;
		for (Method aMethod : o.getClass().getMethods()) {
			if (!methods.contains(aMethod) && aMethod.getName().contentEquals(name)) {
				method = aMethod;
				methods.add(method);
				break;
			}
		}
		if (method == null) {
			return null;
		}
		Object result = null;
		try {
			result = method.invoke(o);
		} catch (Exception e1) {
			try {
				result = method.invoke(o, value);
			} catch (Exception e2) {
				return invoke(methods, o, name, value);
			}
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
		return M.invoke(o, name, (Object[]) null);
	}
}
