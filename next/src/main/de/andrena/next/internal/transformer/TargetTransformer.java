package de.andrena.next.internal.transformer;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import de.andrena.next.Target;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.compiler.AssignmentExp;
import de.andrena.next.internal.compiler.ConstructorExp;
import de.andrena.next.internal.compiler.EmptyExp;
import de.andrena.next.internal.compiler.NestedExp;
import de.andrena.next.internal.compiler.StandaloneExp;
import de.andrena.next.internal.compiler.StaticCallExp;
import de.andrena.next.internal.editor.TargetAccessEditor;
import de.andrena.next.internal.evaluator.Evaluator;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class TargetTransformer extends AbstractContractClassTransformer {

	private static final String TARGET_FIELD_PREFIX = "target$";
	private RootTransformer rootTransformer;

	public TargetTransformer(RootTransformer rootTransformer) {
		this.rootTransformer = rootTransformer;
	}

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		Map<CtField, CtField> targetFieldMap = createWeakFields(contractClass);
		TargetAccessEditor targetAccessEditor = new TargetAccessEditor(targetFieldMap);
		for (CtBehavior contractBehavior : contractClass.getDeclaredBehaviors()) {
			contractBehavior.instrument(targetAccessEditor);
		}
		initWeakFields(contractClass, targetFieldMap.values());
	}

	private void initWeakFields(CtClass contractClass, Collection<CtField> weakFields) throws NotFoundException,
			CannotCompileException {
		CtConstructor defaultConstructor = contractClass.getDeclaredConstructor(new CtClass[0]);
		StandaloneExp initExp = new EmptyExp();
		for (CtField weakField : weakFields) {
			ConstructorExp weakConstructorCall = new ConstructorExp(WeakReference.class, new StaticCallExp(
					Evaluator.getCurrentTarget));
			initExp = initExp.append(new AssignmentExp(NestedExp.field(weakField), weakConstructorCall));
		}
		initExp.insertBefore(defaultConstructor);
	}

	private Map<CtField, CtField> createWeakFields(CtClass contractClass) throws NotFoundException,
			CannotCompileException {
		Map<CtField, CtField> targetFieldMap = new HashMap<CtField, CtField>();
		CtClass weakReferenceClass = rootTransformer.getPool().get(WeakReference.class.getName());
		for (CtField targetField : getTargetFields(contractClass)) {
			CtField weakTargetField = new CtField(weakReferenceClass, TARGET_FIELD_PREFIX, contractClass);
			contractClass.addField(weakTargetField);
			targetFieldMap.put(targetField, weakTargetField);
		}
		return targetFieldMap;
	}

	private Set<CtField> getTargetFields(CtClass contractClass) {
		Set<CtField> targetFields = new HashSet<CtField>();
		for (CtField field : contractClass.getDeclaredFields()) {
			if (field.hasAnnotation(Target.class)) {
				targetFields.add(field);
			}
		}
		return targetFields;
	}

}