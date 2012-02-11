package de.andrena.next.internal.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ContractBehaviorTransformer extends AbstractContractClassTransformer {
	public static final String CONSTRUCTOR_REPLACEMENT_NAME = "constructor$";

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		if (contractClass.equals(contractInfo.getContractClass())) {
			if (!(contractInfo.getTargetClass().isInterface())) {
				replaceConstructors(contractClass);
				contractClass.addConstructor(CtNewConstructor.defaultConstructor(contractClass));
			}
			makeAllBehaviorsAccessible(contractClass);
		}
	}

	private void makeAllBehaviorsAccessible(CtClass contractClass) {
		for (CtBehavior behavior : contractClass.getDeclaredBehaviors()) {
			behavior.setModifiers(Modifier.setPublic(behavior.getModifiers()));
		}
	}

	private void replaceConstructors(CtClass contractClass) throws CannotCompileException, NotFoundException {
		for (CtConstructor constructor : contractClass.getConstructors()) {
			if (constructor.isClassInitializer()) {
				System.out.println("WARNING: REMOVING CLASS INITIALIZER!!!");
			}
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