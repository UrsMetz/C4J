package de.andrena.c4j.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;

import org.junit.Before;
import org.junit.Test;

import de.andrena.c4j.ClassInvariant;
import de.andrena.c4j.Contract;
import de.andrena.c4j.internal.RootTransformer;
import de.andrena.c4j.internal.transformer.ContractBehaviorTransformer;
import de.andrena.c4j.internal.util.ContractRegistry.ContractInfo;

public class AffectedBehaviorLocatorTest {
	private AffectedBehaviorLocator locator;

	private CtClass contractClass;
	private CtClass targetClass;
	private ContractInfo contractInfo;
	private CtClass indirectClass;
	private CtClass targetInterface;
	private CtClass contractClassForTargetInterface;
	private ContractInfo contractInfoForTargetInterface;

	@Before
	public void before() throws Exception {
		locator = new AffectedBehaviorLocator();
		ClassPool pool = ClassPool.getDefault();
		contractClass = pool.get(ContractClass.class.getName());
		targetClass = pool.get(TargetClass.class.getName());
		indirectClass = pool.get(IndirectClass.class.getName());
		ContractRegistry contractRegistry = new ContractRegistry();
		contractInfo = contractRegistry.registerContract(targetClass, contractClass);
		targetInterface = pool.get(TargetInterface.class.getName());
		contractClassForTargetInterface = pool.get(TargetInterfaceContract.class.getName());
		contractInfoForTargetInterface = contractRegistry.registerContract(targetInterface,
				contractClassForTargetInterface);
	}

	@Test
	public void testGetAffectedBehaviorForClassInvariant() throws Exception {
		assertNull(locator.getAffectedBehavior(null, null, contractClass.getDeclaredMethod("invariant")));
	}

	@Test(expected = CannotCompileException.class)
	public void testGetAffectedBehaviorForNonMethodOrConstructor() throws Exception {
		CtBehavior contractBehavior = mock(CtBehavior.class);
		when(contractBehavior.getName()).thenReturn("contractBehavior");
		locator.getAffectedBehavior(null, null, contractBehavior);
	}

	@Test
	public void testGetAffectedMethodForOtherMethod() throws Exception {
		assertNull(locator.getAffectedMethod(contractInfo, targetClass, contractClass.getDeclaredMethod("otherMethod")));
	}

	@Test
	public void testGetAffectedMethodForContractMethod() throws Exception {
		assertEquals(targetClass.getDeclaredMethod("contractMethod"),
				locator.getAffectedMethod(contractInfo, targetClass, contractClass.getDeclaredMethod("contractMethod")));
	}

	@Test
	public void testGetAffectedMethodForIndirectContractMethod() throws Exception {
		RootTransformer.INSTANCE.init("");
		CtMethod affectedMethod = locator.getAffectedMethod(contractInfo, indirectClass,
				contractClass.getDeclaredMethod("contractMethod"));
		assertEquals(indirectClass.getDeclaredMethod("contractMethod"), affectedMethod);
	}

	@Test
	public void testGetAffectedConstructorDuplicateFound() throws Exception {
		assertNull(locator.getAffectedConstructor(contractInfo, targetClass,
				contractClass.getDeclaredConstructor(new CtClass[0])));
	}

	@Test
	public void testGetAffectedConstructor() throws Exception {
		assertEquals(
				targetClass.getDeclaredConstructor(new CtClass[] { CtClass.doubleType }),
				locator.getAffectedConstructor(contractInfo, targetClass,
						contractClass.getDeclaredConstructor(new CtClass[] { CtClass.doubleType })));
	}

	@Test
	public void testGetAffectedConstructorForSynthetic() throws Exception {
		assertEquals(targetClass.getDeclaredConstructor(new CtClass[0]), locator.getAffectedConstructor(contractInfo,
				targetClass, contractClass.getDeclaredMethod(ContractBehaviorTransformer.CONSTRUCTOR_REPLACEMENT_NAME,
						new CtClass[0])));
	}

	@Test
	public void testGetAffectedConstructorNotFound() throws Exception {
		assertNull(locator.getAffectedConstructor(contractInfo, targetClass,
				contractClass.getDeclaredConstructor(new CtClass[] { CtClass.intType })));
	}

	@Test
	public void testGetAffectedConstructorForTargetInterface() throws Exception {
		assertNull(locator.getAffectedConstructor(contractInfoForTargetInterface, targetInterface,
				contractClass.getDeclaredConstructor(new CtClass[0])));
	}

	public static class IndirectClass extends TargetClass {
	}

	public static class TargetClass {
		public TargetClass() {
		}

		public TargetClass(double value) {
		}

		public void contractMethod() {
		}
	}

	public static class ContractClass extends TargetClass {
		public ContractClass() {
		}

		public ContractClass(int value) {
		}

		public ContractClass(double value) {
		}

		@ClassInvariant
		public void invariant() {
		}

		public void constructor$() {
		}

		@Override
		public void contractMethod() {
		}

		public void otherMethod() {
		}
	}

	@Contract(TargetInterfaceContract.class)
	public interface TargetInterface {
	}

	public static class TargetInterfaceContract implements TargetInterface {
	}
}
