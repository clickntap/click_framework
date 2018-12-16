package com.clickntap.tool.hessian;

import java.util.List;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.SerializerFactory;

public class SpringExtensibleSerializerFactory extends SerializerFactory {

	public void setSerializerFactories(List<AbstractSerializerFactory> factories) {
		for (AbstractSerializerFactory factory : factories) {
			addFactory(factory);
		}
	}

}