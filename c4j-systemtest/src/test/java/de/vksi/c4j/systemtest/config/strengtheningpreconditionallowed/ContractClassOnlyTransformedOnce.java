package de.vksi.c4j.systemtest.config.strengtheningpreconditionallowed;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class ContractClassOnlyTransformedOnce {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testContractClassTransformationThrowingException() {
		// loading ContractClass before TargetClass
		// this will throw a strengthening pre-condition exception
		ContractClass.class.getName();
		new TargetClass().method(0);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass {
		public void method(int arg) {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > -1;
			}
		}
	}
}
