package de.andrena.next.internal.compiler;

import java.util.ArrayList;
import java.util.List;

import de.andrena.next.internal.util.ObjectConverter;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ArrayExp extends NestedExp {

	private String code;

	public ArrayExp(Class<?> arrayClass, List<NestedExp> values) {
		this(arrayClass, values.toArray(new NestedExp[0]));
	}

	public ArrayExp(Class<?> arrayClass, NestedExp... values) {
		if (values.length == 0) {
			this.code = "new " + arrayClass.getName() + "[0]";
		} else {
			this.code = "new " + arrayClass.getName() + "[] { " + getCodeForValues(values) + " }";
		}
	}

	public static ArrayExp forParamTypes(CtMethod method) throws NotFoundException {
		List<NestedExp> paramTypes = new ArrayList<NestedExp>();
		for (CtClass paramClass : method.getParameterTypes()) {
			paramTypes.add(new ValueExp(paramClass));
		}
		return new ArrayExp(Class.class, paramTypes);
	}

	public static ArrayExp forArgs(CtMethod method) throws NotFoundException {
		List<NestedExp> args = new ArrayList<NestedExp>();
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			args.add(new StaticCallExp(ObjectConverter.toObject, NestedExp.arg(i + 1)));
		}
		return new ArrayExp(Object.class, args);
	}

	@Override
	protected String getCode() {
		return code;
	}

}