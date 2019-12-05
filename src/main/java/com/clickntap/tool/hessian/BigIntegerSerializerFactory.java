package com.clickntap.tool.hessian;

import java.math.BigInteger;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;

public class BigIntegerSerializerFactory extends AbstractSerializerFactory {
	public Serializer getSerializer(Class cl) throws HessianProtocolException {
		if (BigInteger.class.isAssignableFrom(cl)) {
			return new BigIntegerSerializer();
		}
		return null;
	}

	public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
		if (BigInteger.class.isAssignableFrom(cl)) {
			return new BigIntegerDeSerializer();
		}
		return null;
	}
}