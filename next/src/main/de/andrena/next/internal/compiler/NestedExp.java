package de.andrena.next.internal.compiler;

import java.util.List;

import javassist.CtClass;
import de.andrena.next.internal.compiler.StandaloneExp.CodeStandaloneExp;

public abstract class NestedExp extends Exp {
	public static final NestedExp THIS = new CodeNestedExp("this");
	public static final NestedExp NULL = new CodeNestedExp("null");
	public static final NestedExp RETURN_VALUE = new CodeNestedExp("$_");

	public static NestedExp arg(int num) {
		return new CodeNestedExp("$" + num);
	}

	public static NestedExp field(String name) {
		return new CodeNestedExp("this." + name);
	}

	/**
	 * Not supported by Javassist yet.
	 */
	public static NestedExp field(String name, CtClass parentClass) {
		return new CodeNestedExp(parentClass.getName().replace('$', '.') + ".this." + name);
	}

	public static NestedExp method(String name, NestedExp... params) {
		CodeNestedExp exp = new CodeNestedExp(name);
		exp.append(exp.getCodeForParams(params));
		return exp;
	}

	public StandaloneExp toStandalone() {
		return CodeStandaloneExp.fromNested(getCode());
	}

	protected String getCodeForParams(NestedExp... params) {
		return "(" + getCodeForValues(params) + ")";
	}

	protected String getCodeForValues(NestedExp... values) {
		boolean firstValue = true;
		String valueCode = "";
		for (Exp value : values) {
			if (!firstValue) {
				valueCode += ", ";
			}
			firstValue = false;
			valueCode += value.getCode();
		}
		return valueCode;
	}

	public NestedExp appendCall(String method, NestedExp... params) {
		return new CodeNestedExp(getCode() + "." + method + getCodeForParams(params));
	}

	public NestedExp appendCall(String method, List<NestedExp> callParams) {
		return appendCall(method, callParams.toArray(new NestedExp[0]));
	}

	protected static class CodeNestedExp extends NestedExp {
		private String code;

		public CodeNestedExp(String code) {
			this.code = code;
		}

		public void append(String code) {
			this.code += code;
		}

		@Override
		protected String getCode() {
			return code;
		}
	}

}
