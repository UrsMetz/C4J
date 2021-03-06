package de.vksi.c4j.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import de.vksi.c4j.InitializeContract;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.internal.compiler.NestedExp;
import de.vksi.c4j.internal.util.BehaviorFilter;
import de.vksi.c4j.internal.util.ReflectionHelper;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public class ContractBehaviorTransformer extends AbstractContractClassTransformer {
	public static final String CONSTRUCTOR_REPLACEMENT_NAME = "constructor$";
	public static final String CLASS_INITIALIZER_REPLACEMENT_NAME = "classInitializer$";

	private ReflectionHelper reflectionHelper = new ReflectionHelper();

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (contractClass.equals(contractInfo.getContractClass())) {
			if (!(contractInfo.getTargetClass().isInterface())) {
				replaceConstructors(contractClass);
				contractClass.addConstructor(getContractConstructor(contractClass));
				checkMatchingStaticMethods(contractInfo);
			}
			makeAllBehaviorsAccessible(contractClass);
			renameStaticInitializer(contractClass, contractInfo.getTargetClass());
		}
	}

	private void checkMatchingStaticMethods(ContractInfo contractInfo) {
		for (CtMethod contractMethod : reflectionHelper.getDeclaredMethods(contractInfo.getContractClass(),
				BehaviorFilter.STATIC, BehaviorFilter.VISIBLE)) {
			try {
				contractInfo.getTargetClass().getMethod(contractMethod.getName(), contractMethod.getSignature());
			} catch (NotFoundException e) {
				throw new UsageError("Couldn't find matching target method for static contract method "
						+ contractMethod.getLongName() + ".");
			}
		}
	}

	private void renameStaticInitializer(CtClass contractClass, CtClass targetClass) throws CannotCompileException,
			NotFoundException {
		if (contractClass.getClassInitializer() != null && targetClass.getClassInitializer() != null) {
			contractClass.addMethod(contractClass.getClassInitializer().toMethod(CLASS_INITIALIZER_REPLACEMENT_NAME,
					contractClass));
			contractClass.removeConstructor(contractClass.getClassInitializer());
		}
	}

	private CtConstructor getContractConstructor(CtClass contractClass) throws CannotCompileException,
			NotFoundException {
		CtConstructor contractConstructor = CtNewConstructor.defaultConstructor(contractClass);
		for (CtMethod method : contractClass.getDeclaredMethods()) {
			if (method.hasAnnotation(InitializeContract.class)) {
				appendInitializeContractMethod(contractConstructor, method);
			}
		}
		return contractConstructor;
	}

	private void appendInitializeContractMethod(CtConstructor contractConstructor, CtMethod method)
			throws NotFoundException, CannotCompileException {
		if (method.getParameterTypes().length > 0) {
			logger.warn("Ignoring @InitializeContract method " + method.getLongName() + " as it expects parameters.");
		} else {
			NestedExp.THIS.appendCall(method.getName()).toStandalone().insertAfter(contractConstructor);
		}
	}

	private void makeAllBehaviorsAccessible(CtClass contractClass) {
		for (CtBehavior behavior : contractClass.getDeclaredBehaviors()) {
			behavior.setModifiers(Modifier.setPublic(behavior.getModifiers()));
		}
	}

	private void replaceConstructors(CtClass contractClass) throws CannotCompileException, NotFoundException {
		// getConstructors() excludes the static initializer
		for (CtConstructor constructor : contractClass.getConstructors()) {
			contractClass.addMethod(constructor.toMethod(CONSTRUCTOR_REPLACEMENT_NAME, contractClass));
		}
		if (contractClass.getSuperclass() != null) {
			CtClass oldSuperclass = contractClass.getSuperclass();
			contractClass.getClassFile().setSuperclass(null);
			addFieldsFromSuperclass(contractClass, oldSuperclass);
		}
		for (CtConstructor constructor : contractClass.getConstructors()) {
			contractClass.removeConstructor(constructor);
		}
	}

	private void addFieldsFromSuperclass(CtClass contractClass, CtClass superclass) throws CannotCompileException,
			NotFoundException {
		for (CtField superclassField : superclass.getFields()) {
			if (!hasField(contractClass, superclassField)) {
				contractClass.addField(new CtField(superclassField, contractClass));
			}
		}
		if (superclass.getSuperclass() != null) {
			addFieldsFromSuperclass(contractClass, superclass.getSuperclass());
		}
	}

	private boolean hasField(CtClass contractClass, CtField superclassField) throws NotFoundException {
		try {
			contractClass.getField(superclassField.getName());
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}
}
