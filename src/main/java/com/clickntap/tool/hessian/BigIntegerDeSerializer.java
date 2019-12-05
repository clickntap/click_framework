package com.clickntap.tool.hessian;

import java.io.IOException;
import java.math.BigInteger;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;

public class BigIntegerDeSerializer extends AbstractDeserializer {

	public Class getType() {
		return BigInteger.class;
	}

	public Object readMap(AbstractHessianInput in) throws IOException {
		int ref = in.addRef(null);

		String initValue = null;

		while (!in.isEnd()) {
			String key = in.readString();

			if (key.equals("value"))
				initValue = in.readString();
			else
				in.readString();
		}

		in.readMapEnd();

		Object value = new BigInteger(initValue);

		in.setRef(ref, value);

		return value;
	}

	public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
		int ref = in.addRef(null);

		String initValue = null;

		for (String key : fieldNames) {
			if (key.equals("value"))
				initValue = in.readString();
			else
				in.readObject();
		}

		Object value = new BigInteger(initValue);

		in.setRef(ref, value);

		return value;
	}

}
