package com.clickntap.tool.hessian;

import java.io.IOException;
import java.math.BigInteger;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;

public class BigIntegerSerializer extends AbstractSerializer {
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {

		if (obj == null)
			out.writeNull();
		else {
			Class cl = obj.getClass();

			if (out.addRef(obj))
				return;

			int ref = out.writeObjectBegin(cl.getName());

			BigInteger bi = (BigInteger) obj;

			if (ref < -1) {
				out.writeString("value");
				out.writeString(bi.toString());
				out.writeMapEnd();
			} else {
				if (ref == -1) {
					out.writeInt(1);
					out.writeString("value");
					out.writeObjectBegin(cl.getName());
				}

				out.writeString(bi.toString());
			}
		}
	}
}